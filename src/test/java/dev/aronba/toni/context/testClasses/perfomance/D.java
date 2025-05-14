package dev.aronba.toni.context.testClasses.perfomance;

import dev.aronba.toni.context.Component;

@Component
public class D {
  public D() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignore) {
    }
  }
}
