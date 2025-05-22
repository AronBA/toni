package dev.aronba.toni.context.processor;

import dev.aronba.toni.context.annotation.PostConstruct;
import dev.aronba.toni.context.annotation.PostProcessor;
import dev.aronba.toni.context.core.ApplicationContext;
import dev.aronba.toni.context.exception.PostProcessingException;
import java.lang.reflect.Method;

@PostProcessor
public class PostConstructPostProcessor implements ComponentPostProcessor {
  @Override
  public void postProcess(Class<?> type, Object instance, ApplicationContext applicationContext) {

    for (Method method : type.getDeclaredMethods()) {
      if (method.isAnnotationPresent(PostConstruct.class)) {
        try {
          method.setAccessible(true);
          method.invoke(instance);
        } catch (Exception e) {
          String message = "An error occurred in " + type.getName() + " in the PostConstruct: " + e;
          throw new PostProcessingException(message);
        }
      }
    }
  }
}
