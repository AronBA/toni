package dev.aronba.toni.context.internal;

import dev.aronba.toni.context.exception.CircularDependencyException;
import dev.aronba.toni.context.exception.NoImplementationFoundException;
import java.lang.reflect.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyGraphSorter {
  private static final Logger logger = LoggerFactory.getLogger(DependencyGraphSorter.class);

  private final Map<Class<?>, List<List<Dependency>>> dependencyGraph;
  private final Map<Class<?>, List<Class<?>>> interfaceToImplementationsMap;

  public DependencyGraphSorter(
      Map<Class<?>, List<List<Dependency>>> dependencyGraph,
      Map<Class<?>, List<Class<?>>> interfaceToImplementationsMap) {
    this.dependencyGraph = dependencyGraph;
    this.interfaceToImplementationsMap = interfaceToImplementationsMap;
  }

  public List<Class<?>> sortDependencyGraph()
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
    for (List<Dependency> constructorDeps : dependencyGraph.getOrDefault(clazz, List.of())) {
      for (Dependency d : constructorDeps) {
        Class<?> depType = d.getClazz();
        if (depType.equals(Optional.class)) {
          handleOptional(d, sorted, visited, visiting);
        } else if (depType.isInterface()) {
          handleInterface(depType, sorted, visited, visiting);
        } else {
          visit(depType, sorted, visited, visiting);
        }
      }
    }

    visiting.remove(clazz);
    visited.add(clazz);
    sorted.add(clazz);
  }

  private void handleOptional(
      Dependency dep, List<Class<?>> sorted, Set<Class<?>> visited, Set<Class<?>> visiting)
      throws NoImplementationFoundException {
    if (dep instanceof GenericDependency gd) {
      Type inner = gd.getParameterizedType().getActualTypeArguments()[0];
      if (inner instanceof Class<?> clazz) {
        visit(clazz, sorted, visited, visiting);
      }
    }
  }

  private void handleInterface(
      Class<?> iface, List<Class<?>> sorted, Set<Class<?>> visited, Set<Class<?>> visiting)
      throws NoImplementationFoundException {
    List<Class<?>> impls = interfaceToImplementationsMap.getOrDefault(iface, List.of());
    if (impls.isEmpty()) {
      throw new NoImplementationFoundException("No implementation for " + iface.getName());
    }

    for (Class<?> impl : impls) {
      visit(impl, sorted, visited, visiting);
    }
  }

  public Map<Class<?>, List<List<Dependency>>> getDependencyGraph() {
    return dependencyGraph;
  }

  public Map<Class<?>, List<Class<?>>> getInterfaceToImplementationsMap() {
    return interfaceToImplementationsMap;
  }
}
