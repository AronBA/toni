package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.annotation.Component;
import dev.aronba.toni.context.annotation.PostProcessor;
import dev.aronba.toni.context.core.ApplicationContext;
import dev.aronba.toni.context.exception.PostProcessingException;
import dev.aronba.toni.context.processor.ComponentPostProcessor;

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
