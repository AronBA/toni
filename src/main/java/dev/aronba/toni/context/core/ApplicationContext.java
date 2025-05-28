package dev.aronba.toni.context.core;

import dev.aronba.toni.context.exception.InstatitationException;
import dev.aronba.toni.context.exception.NoImplementationFoundException;
import dev.aronba.toni.context.exception.UnsatisfiedDependencyException;

public interface ApplicationContext {
  void registerComponents(Class<?>... classes)
      throws UnsatisfiedDependencyException, NoImplementationFoundException, InstatitationException;

  <T> T getComponent(Class<T> clazz);

}
