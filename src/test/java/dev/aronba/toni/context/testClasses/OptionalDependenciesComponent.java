package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.annotation.Component;
import java.util.Optional;

@Component
public class OptionalDependenciesComponent {

  public final Optional<SimpleComponent> simpleComponent;

  public OptionalDependenciesComponent(Optional<SimpleComponent> simpleComponent) {
    this.simpleComponent = simpleComponent;
  }
}
