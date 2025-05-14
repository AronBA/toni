package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.ApplicationContext;
import dev.aronba.toni.context.ComponentPostProcessor;
import dev.aronba.toni.context.PostProcessingException;

public class BrokenPostprocessor implements ComponentPostProcessor {
  @Override
  public void postProcess(Class<?> type, Object instance, ApplicationContext applicationContext)
      throws PostProcessingException {}
}
