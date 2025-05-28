package dev.aronba.toni.context.internal;

import dev.aronba.toni.context.annotation.PostProcessor;
import dev.aronba.toni.context.annotation.Use;
import dev.aronba.toni.context.core.ApplicationContext;
import dev.aronba.toni.context.exception.InstatitationException;
import dev.aronba.toni.context.exception.NoImplementationFoundException;
import dev.aronba.toni.context.exception.UnsatisfiedDependencyException;
import dev.aronba.toni.context.processor.ComponentPostProcessor;
import java.lang.reflect.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceFactory {
  private static final Logger logger = LoggerFactory.getLogger(InstanceFactory.class);

  private final Map<Class<?>, Object> instances;
  private final Map<Class<?>, List<List<Dependency>>> dependencyGraph;
  private final Map<Class<?>, List<Class<?>>> interfaceToImplementationsMap;
  private final List<ComponentPostProcessor> postProcessors;
  private final ApplicationContext context;

  public InstanceFactory(
      Map<Class<?>, Object> instances,
      Map<Class<?>, List<List<Dependency>>> dependencyGraph,
      Map<Class<?>, List<Class<?>>> interfaceToImplementationsMap,
      List<ComponentPostProcessor> postProcessors,
      ApplicationContext context) {
    this.instances = instances;
    this.dependencyGraph = dependencyGraph;
    this.interfaceToImplementationsMap = interfaceToImplementationsMap;
    this.postProcessors = postProcessors;
    this.context = context;
  }

  public Object createNewInstance(Class<?> clazz) {
    try {
      return clazz.cast(instantiateClass(clazz));
    } catch (InstatitationException
        | NoImplementationFoundException
        | UnsatisfiedDependencyException e) {
      return null;
    }
  }

  public Object instantiateClass(Class<?> clazz)
      throws InstatitationException,
          UnsatisfiedDependencyException,
          NoImplementationFoundException {

    for (Constructor<?> constructor : clazz.getConstructors()) {
      try {
        List<Object> args = resolveConstructorDependencies(constructor);
        Object instance = constructor.newInstance(args.toArray());
        instances.put(clazz, instance);
        registerPostProcessorIfApplicable(clazz, instance);
        logger.debug("Created instance of {}", clazz.getName());
        return instance;
      } catch (InvocationTargetException
          | InstantiationException
          | IllegalAccessException
          | UnsatisfiedDependencyException e) {
        logger.warn("Constructor failed: {}", constructor);
        // Try next constructor
      }
    }
    logger.error("All constructors failed for class: {}", clazz.getName());
    throw new InstatitationException("Could not instantiate: " + clazz.getName());
  }

  private List<Object> resolveConstructorDependencies(Constructor<?> constructor)
      throws UnsatisfiedDependencyException, NoImplementationFoundException {

    List<Object> resolved = new ArrayList<>();

    for (int i = 0; i < constructor.getGenericParameterTypes().length; i++) {
      Type type = constructor.getGenericParameterTypes()[i];
      Parameter parameter = constructor.getParameters()[i];
      Object dependency = resolveType(type, parameter);
      if (dependency == null) {
        throw new UnsatisfiedDependencyException("Could not resolve: " + type.getTypeName());
      }
      //fixme optionals?
      resolved.add(dependency);
    }

    return resolved;
  }

  private Object resolveType(Type type, Parameter parameter) throws NoImplementationFoundException {

    if (type instanceof ParameterizedType paramType) {
      Class<?> raw = (Class<?>) paramType.getRawType();
      if (raw.equals(Optional.class)) {
        Type inner = paramType.getActualTypeArguments()[0];
        if (inner instanceof Class<?> innerClass) {
            Object dep = resolveNonGenericType(innerClass, parameter);
            return Optional.ofNullable(dep);
            //fixme ? optionals
        }
      }
      return null;
    } else if (type instanceof Class<?> cls) {
      return resolveNonGenericType(cls, parameter);
    }
    return null;
  }

  private Object resolveNonGenericType(Class<?> cls, Parameter parameter)
      throws NoImplementationFoundException {
    if (cls.isInterface()) {
      List<Class<?>> impls = interfaceToImplementationsMap.getOrDefault(cls, List.of());
      if (impls.isEmpty()) throw new NoImplementationFoundException("No impl for: " + cls);
      Class<?> selected = selectImplementation(cls, impls, parameter);
      return context.getComponent(selected);
    } else {
      return context.getComponent(cls);
    }
  }

  private Class<?> selectImplementation(Class<?> iface, List<Class<?>> impls, Parameter parameter) {
    if (parameter.isAnnotationPresent(Use.class)) {
      String preferred = parameter.getAnnotation(Use.class).implementationName();
      return impls.stream()
          .filter(c -> c.getSimpleName().equals(preferred))
          .findFirst()
          .orElseGet(
              () -> {
                logger.warn("No @Use match for {} — using first impl", iface);
                return impls.getFirst();
              });
    }
    return impls.getFirst();
  }

  private void registerPostProcessorIfApplicable(Class<?> clazz, Object instance) {
    if (clazz.isAnnotationPresent(PostProcessor.class)
        && instance instanceof ComponentPostProcessor p) {
      postProcessors.add(p);
    }
  }
}
