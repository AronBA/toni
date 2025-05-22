package dev.aronba.toni.context.testClasses.perfomance;

import dev.aronba.toni.context.annotation.Component;

@Component
public class C {
  public C(B b, A a) {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignore) {
    }
  }
}
