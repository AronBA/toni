package dev.aronba.toni.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicApplicationContext implements ApplicationContext {
  private final Logger logger = LoggerFactory.getLogger(BasicApplicationContext.class);
  private final Map<Class<?>, Object> instances;
  private final Map<Class<?>, List<List<Class<?>>>> dependencyGraph;
  private final List<ComponentPostProcessor> componentPostProcessors;
  private final Map<Class<?>, List<Class<?>>> interfaceToImplementationsMap;

  public BasicApplicationContext() {
    instances = new HashMap<>();
    dependencyGraph = new HashMap<>();
    interfaceToImplementationsMap = new HashMap<>();
    componentPostProcessors = new ArrayList<>();
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
        interfaceToImplementationsMap.put(clazz, new ArrayList<>());
        dependencyGraph.put(clazz, List.of());
        continue;
      }
      for (Class<?> iface : clazz.getInterfaces()) {
        if (interfaceToImplementationsMap.containsKey(iface)) {
          interfaceToImplementationsMap.get(iface).add(clazz);
        }
      }

      Constructor<?>[] constructorCandidates = clazz.getConstructors();
      List<List<Class<?>>> constructors = findValidConstructors(constructorCandidates);

      if (constructors.isEmpty()) {
        logger.warn("no valid constructor found for class: {}", clazz.getName());
        continue;
      }
      constructors.sort(Comparator.comparingInt(List::size));

      logger.debug(
          "found {} valid constructor candidates for class: {}",
          constructors.size(),
          clazz.getName());
      this.dependencyGraph.put(clazz, constructors);
    }
  }

  private List<List<Class<?>>> findValidConstructors(Constructor<?>[] constructorCandidates) {

    List<List<Class<?>>> validConstructors = new ArrayList<>();
    for (Constructor<?> constructorCandidate : constructorCandidates) {
      if (isConstrictorValid(constructorCandidate)) {
        Class<?>[] parameters = constructorCandidate.getParameterTypes();
        validConstructors.add(List.of(parameters));
      }
    }
    return validConstructors;
  }

  private boolean isConstrictorValid(Constructor<?> constructor) {
    for (Class<?> parameter : constructor.getParameterTypes()) {
      if (!parameter.isAnnotationPresent(Component.class)) {
        logger.warn("found constructor with invalid parameters");
        return false;
      }
    }
    return true;
  }

  private void runPostProcessors() {
    for (Map.Entry<Class<?>, Object> entry : instances.entrySet()) {
      for (ComponentPostProcessor processor : componentPostProcessors) {
        processor.postProcess(entry.getKey(), entry.getValue(), this);
      }
    }
  }

  @Override
  public <T> T get(Class<T> clazz) {
    return clazz.cast(instances.get(clazz));
  }

  private void instantiateComponents()
      throws UnsatisfiedDependencyException,
          NoImplementationFoundException,
          InstatitationException {
    List<Class<?>> sortedClasses = sortGraph();
    for (Class<?> clazz : sortedClasses) {
      createInstance(clazz);
    }
    logger.info("successfully created {} instances", instances.size());
  }

  private Object findInterfaceImplementation(Class<?> param)
      throws NoImplementationFoundException, UnsatisfiedDependencyException {
    List<Class<?>> possibleImplementations = interfaceToImplementationsMap.get(param);
    if (possibleImplementations == null || possibleImplementations.isEmpty()) {
      throw new NoImplementationFoundException("No implementation found for: " + param.getName());
    }
    Class<?> firstCandidate = possibleImplementations.getFirst(); // Use the first implementation
    Object dependency = get(firstCandidate);
    if (dependency == null) {
      throw new UnsatisfiedDependencyException(
          "Instance of dependency is null for: " + param.getName());
    }
    return dependency;
  }

  private List<Object> instantiatedDependencies(Constructor<?> constructor)
      throws NoImplementationFoundException, UnsatisfiedDependencyException {
    List<Object> args = new ArrayList<>();
    for (Class<?> param : constructor.getParameterTypes()) {
      Object dependency = param.isInterface() ? findInterfaceImplementation(param) : get(param);

      if (dependency == null) {
        throw new UnsatisfiedDependencyException(
            "Instance of dependency is null for: " + param.getName());
      }
      args.add(dependency);
    }

    return args;
  }

  private void instantiateConstructor(Class<?> clazz, Constructor<?> constructor)
      throws UnsatisfiedDependencyException,
          NoImplementationFoundException,
          InstatitationException {
    try {
      List<Object> args = instantiatedDependencies(constructor);
      Object instance = constructor.newInstance(args.toArray());
      instances.put(clazz, instance);
      addPostProcessor(clazz, instance);
      logger.debug("Created instance of: {}", clazz.getName());
    } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
      throw new InstatitationException(e);
    }
  }

  private void createInstance(Class<?> clazz)
      throws UnsatisfiedDependencyException,
          NoImplementationFoundException,
          InstatitationException {
    if (instances.containsKey(clazz)) return;
    if (clazz.isInterface()) {
      for (Class<?> possibleImplementations : interfaceToImplementationsMap.get(clazz)) {
        createInstance(possibleImplementations);
      }
      return;
    }
    Constructor<?>[] constructors = clazz.getConstructors();
    for (int i = 0; i < constructors.length; i++) {
      try {
        instantiateConstructor(clazz, constructors[i]);
        break;
      } catch (UnsatisfiedDependencyException
          | NoImplementationFoundException
          | InstatitationException e) {
        logger.warn(
            "Failed instantiating constructor: {} remaining constructors to try: {}",
            constructors[i].getName(),
            constructors.length - 1);
        if (i == constructors.length - 1) {
          logger.error("Failed constructing instance of {}", clazz.getName());
          throw e;
        }
      }
    }
  }

  private void addPostProcessor(Class<?> clazz, Object instance) {
    if (clazz.isAnnotationPresent(PostProcessor.class)
        && instance instanceof ComponentPostProcessor componentPostProcessor) {
      componentPostProcessors.add(componentPostProcessor);
    }
  }

  private List<Class<?>> sortGraph() throws CircularDependencyException {
    List<Class<?>> sorted = new ArrayList<>();
    Set<Class<?>> visited = new HashSet<>();
    Set<Class<?>> visiting = new HashSet<>();
    for (Class<?> node : dependencyGraph.keySet()) {
      checkDependencies(node, sorted, visited, visiting);
    }
    return sorted;
  }

  private void checkDependencies(
      Class<?> node, List<Class<?>> sorted, Set<Class<?>> visited, Set<Class<?>> visiting)
      throws CircularDependencyException {
    if (visited.contains(node)) return; // if dependency got already checked -> return
    if (visiting.contains(node))
      throw new CircularDependencyException(); // if dependency is depending on its own -> throw
    // exception

    visiting.add(node);

    for (List<Class<?>> constructors :
        dependencyGraph.getOrDefault(
            node,
            List.of())) { // base case for recursion -> if no dependencies exists, loop won't get
      // executed
      for (Class<?> dependency : constructors) {
        checkDependencies(dependency, sorted, visited, visiting);
      }
    }
    visiting.remove(node);
    visited.add(node);
    sorted.add(node);
  }
}
