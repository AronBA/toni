package dev.aronba.toni.context;

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
