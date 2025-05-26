package dev.aronba.toni.context.internal;

import dev.aronba.toni.context.annotation.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyGraphBuilder {
  private static final Logger logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);

  private final Map<Class<?>, List<List<Dependency>>> dependencyGraph = new HashMap<>();
  private final Map<Class<?>, List<Class<?>>> interfaceToImpls = new HashMap<>();

  public void buildGraph(Class<?>... classes) {
    for (Class<?> clazz : classes) {
      if (clazz.isInterface()) {
        interfaceToImpls.putIfAbsent(clazz, new ArrayList<>());
        dependencyGraph.put(clazz, List.of());
        continue;
      }

      for (Class<?> iface : clazz.getInterfaces()) {
        interfaceToImpls.computeIfAbsent(iface, _ -> new ArrayList<>()).add(clazz);
      }

      List<List<Dependency>> validConstructors = findValidConstructors(clazz.getConstructors());
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

  private List<List<Dependency>> findValidConstructors(Constructor<?>[] constructors) {
    List<List<Dependency>> valid = new ArrayList<>();
    for (Constructor<?> constructor : constructors) {
      if (isConstructorValid(constructor)) {
        List<Dependency> dependencies = new ArrayList<>();
        for (int i = 0; i < constructor.getGenericParameterTypes().length; i++) {
          Type type = constructor.getGenericParameterTypes()[i];
          if (type instanceof ParameterizedType parameterizedType) {
            dependencies.add(GenericDependency.of(parameterizedType));
          } else {
            dependencies.add(Dependency.of((Class<?>) type));
          }
        }
        valid.add(dependencies);
      }
    }
    return valid;
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

  public Map<Class<?>, List<List<Dependency>>> getDependencyGraph() {
    return dependencyGraph;
  }

  public Map<Class<?>, List<Class<?>>> getInterfaceToImpls() {
    return interfaceToImpls;
  }
}
