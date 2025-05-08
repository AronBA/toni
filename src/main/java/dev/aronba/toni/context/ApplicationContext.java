package dev.aronba.toni.context;

public interface ApplicationContext {
  void register(Class<?>... classes) throws CircularDependencyException;

  <T> T get(Class<T> clazz);
}
