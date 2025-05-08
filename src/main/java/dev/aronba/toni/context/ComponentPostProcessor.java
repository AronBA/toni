package dev.aronba.toni.context;

@PostProcessor
public interface ComponentPostProcessor {
  void postProcess(Class<?> type, Object instance, ApplicationContext applicationContext)
      throws PostProcessingException;
}
