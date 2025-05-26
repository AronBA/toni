package dev.aronba.toni.context.testClasses.perfomance;

import dev.aronba.toni.context.annotation.Component;

@Component
public class Q {
  Q(R r, F f, C c) {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignore) {
    }
  }
}
