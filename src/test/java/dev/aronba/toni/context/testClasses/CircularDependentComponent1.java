package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.annotation.Component;

@Component
public class CircularDependentComponent1 {
  private final CircularDependentComponent1 circularDependentComponent;

  public CircularDependentComponent1(CircularDependentComponent1 circularDependentComponent) {
    this.circularDependentComponent = circularDependentComponent;
  }
}
