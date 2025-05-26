package dev.aronba.toni.context.core;

import dev.aronba.toni.context.exception.InstatitationException;
import dev.aronba.toni.context.exception.NoImplementationFoundException;
import dev.aronba.toni.context.exception.UnsatisfiedDependencyException;

public interface ApplicationContext {
  void register(Class<?>... classes)
      throws UnsatisfiedDependencyException, NoImplementationFoundException, InstatitationException;

  <T> T get(Class<T> clazz);
}
