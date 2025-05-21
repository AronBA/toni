package dev.aronba.toni.context;

import java.lang.reflect.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicApplicationContext implements ApplicationContext {
  private static final Logger logger = LoggerFactory.getLogger(BasicApplicationContext.class);

  private final Map<Class<?>, Object> instances = new HashMap<>();
  private final Map<Class<?>, List<List<Class<?>>>> dependencyGraph = new HashMap<>();
  private final List<ComponentPostProcessor> componentPostProcessors = new ArrayList<>();
  private final Map<Class<?>, List<Class<?>>> interfaceToImplementationsMap = new HashMap<>();

  @Override
  public <T> T get(Class<T> clazz) {
    if (!clazz.isAnnotationPresent(Component.class)) return null;
    Lifetime classLifetime = clazz.getAnnotation(Component.class).value();

    Object instance = null;
    if (Objects.requireNonNull(classLifetime) == Lifetime.SINGELTON) {
      instance = clazz.cast(instances.get(clazz));
    } else if (classLifetime == Lifetime.PROTOTYPE && instances.containsKey(clazz)) {
      instance = clazz.cast(createNewInstance(clazz));
    }
    if (instance == null) return null;
    return clazz.cast(instance);
  }

  private Object createNewInstance(Class<?> clazz) {
    try {
      return clazz.cast(instantiateClass(clazz));
    } catch (InstatitationException
        | NoImplementationFoundException
        | UnsatisfiedDependencyException e) {
      return null;
    }
  }

  @Override
  public void register(Class<?>... classes)
      throws UnsatisfiedDependencyException,
          NoImplementationFoundException,
          InstatitationException {
    buildDependencyGraph(classes);
    instantiateComponents();
    runPostProcessors();
  }

  private void buildDependencyGraph(Class<?>... classes) {
    for (Class<?> clazz : classes) {
      if (clazz.isInterface()) {
        interfaceToImplementationsMap.putIfAbsent(clazz, new ArrayList<>());
        dependencyGraph.put(clazz, List.of());
        continue;
      }

      for (Class<?> iface : clazz.getInterfaces()) {
        interfaceToImplementationsMap.computeIfAbsent(iface, _ -> new ArrayList<>()).add(clazz);
      }

      List<List<Class<?>>> validConstructors = findValidConstructors(clazz.getConstructors());
      if (validConstructors.isEmpty()) {
        logger.warn("No valid constructor found for class: {}", clazz.getName());
        continue;
      }

      validConstructors.sort(Comparator.comparingInt(List::size));
      logger.debug(
          "Found {} valid constructors for class: {}", validConstructors.size(), clazz.getName());
      dependencyGraph.put(clazz, validConstructors);
    }
  }

  private List<List<Class<?>>> findValidConstructors(Constructor<?>[] constructors) {
    List<List<Class<?>>> valid = new ArrayList<>();
    for (Constructor<?> constructor : constructors) {
      if (isConstructorValid(constructor)) {
        addParameters(valid, constructor);
      }
    }
    return valid;
  }

  private void addParameters(List<List<Class<?>>> valid, Constructor constructor) {
    valid.add(List.of(constructor.getParameterTypes()));
  }

  private boolean isConstructorValid(Constructor<?> constructor) {
    for (int i = 0; i < constructor.getParameters().length; i++) {
      Class<?> param = constructor.getParameterTypes()[i];
      if (param.equals(Optional.class)) {
        Type genericType = constructor.getGenericParameterTypes()[i];
        if (genericType instanceof ParameterizedType parameterizedType) {
          Class<?> type = (Class<?>) parameterizedType.getActualTypeArguments()[0];
          return type.isAnnotationPresent(Component.class);
        }
      }
      if (!param.isAnnotationPresent(Component.class)) {
        logger.warn("Constructor with invalid parameters found: {}", constructor);
        return false;
      }
    }
    return true;
  }

  private void instantiateComponents()
      throws UnsatisfiedDependencyException,
          NoImplementationFoundException,
          InstatitationException {
    for (Class<?> clazz : sortDependencyGraph()) {
      if (!clazz.isInterface() && !instances.containsKey(clazz)) {
        instantiateClass(clazz);
      }
    }
    logger.info("Successfully created {} instances", instances.size());
  }

  private Object instantiateClass(Class<?> clazz)
      throws UnsatisfiedDependencyException,
          NoImplementationFoundException,
          InstatitationException {
    for (Constructor<?> constructor : clazz.getConstructors()) {
      try {
        return createInstanceFromConstructor(clazz, constructor);
      } catch (Exception e) {
        logger.warn(
            "Constructor failed: {} ({} left)",
            constructor.getName(),
            clazz.getConstructors().length - 1);
        if (constructor == clazz.getConstructors()[clazz.getConstructors().length - 1]) {
          logger.error("All constructors failed for class: {}", clazz.getName());
          throw e;
        }
      }
    }
    logger.warn("something went bad");
    return null;
  }

  private Object createInstanceFromConstructor(Class<?> clazz, Constructor<?> constructor)
      throws UnsatisfiedDependencyException,
          NoImplementationFoundException,
          InstatitationException {
    try {
      List<Object> args = getInstantiatedDependencies(constructor);
      Object instance = constructor.newInstance(args.toArray());
      instances.put(clazz, instance);
      registerPostProcessorIfApplicable(clazz, instance);
      logger.debug("Created instance of {}", clazz.getName());
      return instance;
    } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
      throw new InstatitationException(e);
    }
  }

  private List<Object> getInstantiatedDependencies(Constructor<?> constructor)
      throws NoImplementationFoundException, UnsatisfiedDependencyException {
    List<Object> dependencies = new ArrayList<>();
    for (Parameter parameter : constructor.getParameters()) {
      Object dependency;

      if (parameter.getType().equals(Optional.class)) {

        Type[] genericInterfaces = parameter.getType().getGenericInterfaces();
        Type type = genericInterfaces[0];

        Object ob =
            type.getClass().isInterface()
                ? resolveInterfaceDependency(parameter)
                : get(type.getClass());
        dependency = Optional.ofNullable(ob);
        // todo -> I dunno if this shit works check later
      } else {
        dependency =
            parameter.getType().isInterface()
                ? resolveInterfaceDependency(parameter)
                : get(parameter.getType());
      }

      if (dependency == null) {
        throw new UnsatisfiedDependencyException("Missing dependency: " + parameter.getName());
      }

      dependencies.add(dependency);
    }
    return dependencies;
  }

  private Object resolveInterfaceDependency(Parameter parameter)
      throws NoImplementationFoundException, UnsatisfiedDependencyException {
    List<Class<?>> implementations = interfaceToImplementationsMap.get(parameter.getType());
    if (implementations == null || implementations.isEmpty()) {
      throw new NoImplementationFoundException(
          "No implementation found for: " + parameter.getType().getName());
    }

    Class<?> selected = selectImplementation(parameter, implementations);
    Object dependency = get(selected);

    if (dependency == null) {
      throw new UnsatisfiedDependencyException("Unresolved dependency: " + parameter.getName());
    }

    return dependency;
  }

  private Class<?> selectImplementation(Parameter param, List<Class<?>> candidates) {
    if (param.isAnnotationPresent(Use.class)) {
      String requiredName = param.getAnnotation(Use.class).implementationName();
      for (Class<?> candidate : candidates) {
        if (candidate.getSimpleName().equals(requiredName)) {
          return candidate;
        }
      }
      logger.warn("No matching implementation named '{}' for {}", requiredName, param);
    }
    return candidates.getFirst();
  }

  private void registerPostProcessorIfApplicable(Class<?> clazz, Object instance) {
    if (clazz.isAnnotationPresent(PostProcessor.class)
        && instance instanceof ComponentPostProcessor processor) {
      componentPostProcessors.add(processor);
    }
  }

  private void runPostProcessors() {
    for (var entry : instances.entrySet()) {
      for (var processor : componentPostProcessors) {
        processor.postProcess(entry.getKey(), entry.getValue(), this);
      }
    }
  }

  private List<Class<?>> sortDependencyGraph()
      throws CircularDependencyException, NoImplementationFoundException {
    List<Class<?>> sorted = new ArrayList<>();
    Set<Class<?>> visited = new HashSet<>();
    Set<Class<?>> visiting = new HashSet<>();

    for (Class<?> clazz : dependencyGraph.keySet()) {
      visit(clazz, sorted, visited, visiting);
    }
    return sorted;
  }

  private void visit(
      Class<?> clazz, List<Class<?>> sorted, Set<Class<?>> visited, Set<Class<?>> visiting)
      throws CircularDependencyException, NoImplementationFoundException {

    if (visited.contains(clazz)) return;
    if (visiting.contains(clazz)) throw new CircularDependencyException();

    visiting.add(clazz);

    for (List<Class<?>> constructorDeps : dependencyGraph.getOrDefault(clazz, List.of())) {
      for (Class<?> dependency : constructorDeps) {
        if (dependency.isInterface()) {
          handleInterface(dependency, sorted, visited, visiting);
        } else if (dependency.equals(Optional.class)) {
          handleOptionals(dependency, sorted, visited, visiting);
        } else {
          visit(dependency, sorted, visited, visiting);
        }
      }
    }

    visiting.remove(clazz);
    visited.add(clazz);
    sorted.add(clazz);
  }

  private void handleOptionals(
      Class<?> dependency, List<Class<?>> sorted, Set<Class<?>> visited, Set<Class<?>> visiting)
      throws NoImplementationFoundException {
    visit(dependency, sorted, visited, visiting);
    // todo -> handle optionals, get Realtype instead of RawType
  }

  private void handleInterface(
      Class<?> dependency, List<Class<?>> sorted, Set<Class<?>> visited, Set<Class<?>> visiting)
      throws NoImplementationFoundException {
    List<Class<?>> impls = interfaceToImplementationsMap.get(dependency);
    if (impls == null || impls.isEmpty()) {
      throw new NoImplementationFoundException(
          "No implementation found for: " + dependency.getName());
    }
    for (Class<?> impl : impls) {
      visit(impl, sorted, visited, visiting);
    }
  }
}
