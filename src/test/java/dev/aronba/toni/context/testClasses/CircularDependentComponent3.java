package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.annotation.Component;

@Component
public class CircularDependentComponent3 {
  private final CircularDependentComponent2 circularDependentComponent2;

  public CircularDependentComponent3(CircularDependentComponent2 circularDependentComponent2) {
    this.circularDependentComponent2 = circularDependentComponent2;
  }
}
