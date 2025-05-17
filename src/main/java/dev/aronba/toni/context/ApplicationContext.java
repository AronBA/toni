package dev.aronba.toni.context;

public interface ApplicationContext {
  void register(Class<?>... classes) throws UnsatisfiedDependencyException, NoImplementationFoundException, InstatitationException;
  <T> T get(Class<T> clazz);
}
