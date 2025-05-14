package dev.aronba.toni.context.testClasses;

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
