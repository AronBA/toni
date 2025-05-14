package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.Component;
import dev.aronba.toni.context.Use;

@Component
public class InterfaceDependentComponentWithUse {

  private final ServiceInterface serviceInterface;

  public int check = 0;

  public InterfaceDependentComponentWithUse(
      @Use(implementationName = "ServiceInterfaceWithDependenciesImpl")
          ServiceInterface serviceInterface) {
    this.serviceInterface = serviceInterface;
  }

  public void run() {
    this.check = serviceInterface.provideService();
  }
}
