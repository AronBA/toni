package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.annotation.Component;
import dev.aronba.toni.context.annotation.Use;

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
