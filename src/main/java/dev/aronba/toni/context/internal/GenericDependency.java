package dev.aronba.toni.context.internal;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public class GenericDependency extends Dependency {
  private final ParameterizedType parameterizedType;

  protected GenericDependency(Class<?> clazz, ParameterizedType parameterizedType) {
    super(clazz);
    this.parameterizedType = parameterizedType;
  }

  public ParameterizedType getParameterizedType() {
    return parameterizedType;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    GenericDependency that = (GenericDependency) o;
    return Objects.equals(parameterizedType, that.parameterizedType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), parameterizedType);
  }
}
