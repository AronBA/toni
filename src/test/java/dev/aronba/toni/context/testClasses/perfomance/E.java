package dev.aronba.toni.context.testClasses.perfomance;

import dev.aronba.toni.context.annotation.Component;

@Component
public class E {
  public E(D d, C c, B b, A a) {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignore) {
    }
  }
}
