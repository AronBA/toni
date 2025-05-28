package dev.aronba.toni.context.core;

import dev.aronba.toni.context.annotation.Component;
import dev.aronba.toni.context.exception.InstatitationException;
import dev.aronba.toni.context.exception.NoImplementationFoundException;
import dev.aronba.toni.context.exception.UnsatisfiedDependencyException;
import dev.aronba.toni.context.internal.*;
import dev.aronba.toni.context.processor.ComponentPostProcessor;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicApplicationContext implements ApplicationContext {
  private static final Logger logger = LoggerFactory.getLogger(BasicApplicationContext.class);

  private final Map<Class<?>, Object> instances = new HashMap<>();
  private final List<ComponentPostProcessor> componentPostProcessors = new ArrayList<>();
  private final DependencyGraphBuilder dependencyGraphBuilder;
  private final InstanceFactory instanceFactory;
  private final DependencyGraphSorter dependencyGraphSorter;

  public BasicApplicationContext() {
    this.dependencyGraphBuilder = new DependencyGraphBuilder();
    this.instanceFactory =
        new InstanceFactory(
            instances,
            dependencyGraphBuilder.getDependencyGraph(),
            dependencyGraphBuilder.getInterfaceToImpls(),
            componentPostProcessors,
            this);
    this.dependencyGraphSorter =
        new DependencyGraphSorter(
            dependencyGraphBuilder.getDependencyGraph(),
            dependencyGraphBuilder.getInterfaceToImpls());
  }

  @Override
  public <T> T getComponent(Class<T> clazz) {
    if (!clazz.isAnnotationPresent(Component.class)) return null;
    Lifetime classLifetime = clazz.getAnnotation(Component.class).value();

    Object instance = null;
    if (Objects.requireNonNull(classLifetime) == Lifetime.SINGELTON) {
      instance = clazz.cast(instances.get(clazz));
    } else if (classLifetime == Lifetime.PROTOTYPE && instances.containsKey(clazz)) {
      instance = clazz.cast(this.instanceFactory.createNewInstance(clazz));
    }
    if (instance == null) return null;
    return clazz.cast(instance);
  }

  @Override
  public void registerComponents(Class<?>... classes)
      throws UnsatisfiedDependencyException,
          NoImplementationFoundException,
          InstatitationException {
    this.dependencyGraphBuilder.buildGraph(classes);
    instantiateComponents();
    runPostProcessors();
  }

  private void instantiateComponents()
      throws UnsatisfiedDependencyException,
          NoImplementationFoundException,
          InstatitationException {
    for (Class<?> clazz : this.dependencyGraphSorter.sortDependencyGraph()) {
      if (!clazz.isInterface() && !instances.containsKey(clazz)) {
        instanceFactory.instantiateClass(clazz);
      }
    }
    logger.info("Successfully created {} instances", instances.size());
  }

  private void runPostProcessors() {
    for (var entry : instances.entrySet()) {
      for (var processor : componentPostProcessors) {
        processor.postProcess(entry.getKey(), entry.getValue(), this);
      }
    }
  }
}
