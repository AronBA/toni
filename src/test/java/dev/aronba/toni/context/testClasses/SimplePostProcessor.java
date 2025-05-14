package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.*;

@Component
@PostProcessor
public class SimplePostProcessor implements ComponentPostProcessor {

  @Override
  public void postProcess(Class<?> type, Object instance, ApplicationContext applicationContext)
      throws PostProcessingException {
    if (instance instanceof ComplexComponent complexComponent) {
      complexComponent.setComponentName("Hello from Postprocessor");
    }
  }
}
