package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.annotation.Component;

@Component
public class ServiceInterfaceWithDependenciesImpl implements ServiceInterface {

  private final EmptyComponent emptyComponent;

  public ServiceInterfaceWithDependenciesImpl(EmptyComponent emptyComponent) {
    this.emptyComponent = emptyComponent;
  }

  @Override
  public int provideService() {
    return 20;
  }
}
