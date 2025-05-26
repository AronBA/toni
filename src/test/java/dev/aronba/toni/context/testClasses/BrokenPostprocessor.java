package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.core.ApplicationContext;
import dev.aronba.toni.context.exception.PostProcessingException;
import dev.aronba.toni.context.processor.ComponentPostProcessor;

public class BrokenPostprocessor implements ComponentPostProcessor {
  @Override
  public void postProcess(Class<?> type, Object instance, ApplicationContext applicationContext)
      throws PostProcessingException {}
}
