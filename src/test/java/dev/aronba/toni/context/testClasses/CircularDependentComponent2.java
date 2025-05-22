package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.annotation.Component;

@Component
public class CircularDependentComponent2 {
  private final CircularDependentComponent3 circularDependentComponent3;

  public CircularDependentComponent2(CircularDependentComponent3 circularDependentComponent3) {
    this.circularDependentComponent3 = circularDependentComponent3;
  }
}
