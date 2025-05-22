package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.annotation.Component;
import dev.aronba.toni.context.core.Lifetime;

@Component(Lifetime.PROTOTYPE)
public class PrototypeComponent {
  private static int classNumber = 0;
  public int id;

  public PrototypeComponent() {
    classNumber++;
    id = classNumber;
  }
}
