package dev.aronba.toni.context;

import java.lang.reflect.Constructor;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicApplicationContext implements ApplicationContext {
  private final Logger logger = LoggerFactory.getLogger(BasicApplicationContext.class);
  private final Map<Class<?>, Object> instances;
  private final Map<Class<?>, List<List<Class<?>>>> dependencyGraph;
  private final List<ComponentPostProcessor> componentPostProcessors;

  public BasicApplicationContext() {
    instances = new HashMap<>();
    dependencyGraph = new HashMap<>();
    componentPostProcessors = new ArrayList<>();
  }

  @Override
  public void register(Class<?>... classes) throws CircularDependencyException {
    buildDependencyGraph(classes);
    instantiateComponents();
    runPostProcessors();
  }

  private void buildDependencyGraph(Class<?>... classes) {

    for (Class<?> clazz : classes) {
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

  private void instantiateComponents() throws CircularDependencyException {
    List<Class<?>> sortedClasses = sortGraph();

    for (Class<?> clazz : sortedClasses) {
      createInstance(clazz);
    }

    logger.info("successfully created {} instances", instances.size());
  }

  private void createInstance(Class<?> clazz) {
    if (instances.containsKey(clazz)) return;
    Constructor<?>[] constructors = clazz.getConstructors();
    for (Constructor<?> constructor : constructors) {
      try {
        List<Object> args = new ArrayList<>();
        for (Class<?> param : constructor.getParameterTypes()) {
          Object dependency = get(param);
          if (dependency == null) throw new NullPointerException("Instance of dependency is null");
          args.add(dependency);
        }
        Object instance = constructor.newInstance(args.toArray());
        instances.put(clazz, instance);
        addPostProcessor(clazz, instance);
        logger.debug("created instance of: {}", clazz.getName());
        return;
      } catch (Exception e) {
        logger.warn(
            "failed to create instance of {}, will try another constructor: {}",
            clazz.getName(),
            e.getMessage());
      }
    }
    logger.error("failed constructing instance of {} ", clazz.getName());
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
