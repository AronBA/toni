package dev.aronba.toni.context;

public interface ApplicationContext {
  void register(Class<?>... classes) throws Exception;

  <T> T get(Class<T> clazz);
}
