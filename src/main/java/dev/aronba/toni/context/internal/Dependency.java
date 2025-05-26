package dev.aronba.toni.context.internal;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public abstract class Dependency {
  private final Class<?> clazz;

  protected Dependency(Class<?> clazz) {
    this.clazz = clazz;
  }

  public static Dependency of(Class<?> clazz) {
    return new Dependency(clazz) {};
  }

  public static GenericDependency of(ParameterizedType parameterizedType) {
    return new GenericDependency((Class<?>) parameterizedType.getRawType(), parameterizedType);
  }

  public Class<?> getClazz() {
    return clazz;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Dependency that = (Dependency) o;
    return Objects.equals(clazz, that.clazz);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(clazz);
  }
}
