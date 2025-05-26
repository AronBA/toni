package dev.aronba.toni.context.testClasses.perfomance;

import dev.aronba.toni.context.annotation.Component;

@Component
public class A {
  public A() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignore) {
    }
  }
}
