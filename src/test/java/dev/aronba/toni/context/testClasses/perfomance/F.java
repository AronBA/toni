package dev.aronba.toni.context.testClasses.perfomance;

import dev.aronba.toni.context.annotation.Component;

@Component
public class F {
  public F(E e, D d, C c, B b, A a) {
    try {
      Thread.sleep(300);
    } catch (InterruptedException ignore) {
    }
  }
}
