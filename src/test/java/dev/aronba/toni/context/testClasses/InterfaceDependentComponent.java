package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.Component;

@Component
public class InterfaceDependentComponent {
  private final ServiceInterface serviceInterface;
  public int check = 0;

  public InterfaceDependentComponent(ServiceInterface serviceInterface) {
    this.serviceInterface = serviceInterface;
  }

  public void run() {
    this.check = serviceInterface.provideService();
  }
}
