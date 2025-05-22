package dev.aronba.toni.context.processor;

import dev.aronba.toni.context.annotation.PostProcessor;
import dev.aronba.toni.context.core.ApplicationContext;
import dev.aronba.toni.context.exception.PostProcessingException;

@PostProcessor
public interface ComponentPostProcessor {
  void postProcess(Class<?> type, Object instance, ApplicationContext applicationContext)
      throws PostProcessingException;
}
