package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.Component;
import dev.aronba.toni.context.PostConstruct;

@Component
public class ComplexComponent {
  private SimpleComponent simpleComponent;
  private EmptyComponent emptyComponent;
  private String componentName;
  private String componentDescription;

  // constructor with dependencies that can't be satisfied by the IOC container
  public ComplexComponent(String componentDescription, String componentName) {
    this.componentName = componentName;
    this.componentDescription = componentDescription;
  }

  // constructor with dependencies that can be satisfied by the IOC container
  public ComplexComponent(SimpleComponent simpleComponent, EmptyComponent emptyComponent) {
    this.simpleComponent = simpleComponent;
    this.emptyComponent = emptyComponent;
  }

  // constructor with dependencies that can't be satisfied by the IOC container and is longer
  public ComplexComponent(
      EmptyComponent emptyComponent, String componentName, String componentDescription) {
    this.emptyComponent = emptyComponent;
    this.componentDescription = componentDescription;
    this.componentName = componentName;
  }

  @PostConstruct
  void init() {
    this.componentDescription = "Hello from PostConstruct";
  }

  public String getComponentDescription() {
    return componentDescription;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public SimpleComponent getSimpleComponent() {
    return simpleComponent;
  }

  public EmptyComponent getEmptyComponent() {
    return emptyComponent;
  }
}
