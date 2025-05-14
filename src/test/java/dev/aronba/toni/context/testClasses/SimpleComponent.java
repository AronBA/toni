package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.Component;

@Component
public class SimpleComponent {
  private final EmptyComponent emptyComponent;

  public SimpleComponent(EmptyComponent emptyComponent) {
    this.emptyComponent = emptyComponent;
  }

  public EmptyComponent getEmptyComponent() {
    return emptyComponent;
  }
}
