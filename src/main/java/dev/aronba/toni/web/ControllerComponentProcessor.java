package dev.aronba.toni.web;

import dev.aronba.toni.context.ApplicationContext;
import dev.aronba.toni.context.Component;
import dev.aronba.toni.context.ComponentPostProcessor;
import dev.aronba.toni.context.PostProcessor;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@PostProcessor
public class ControllerComponentProcessor implements ComponentPostProcessor {

  private final Logger logger = LoggerFactory.getLogger(ControllerComponentProcessor.class);
  private final RouteRegistry routeRegistry;

  public ControllerComponentProcessor(RouteRegistry routeRegistry) {
    this.routeRegistry = routeRegistry;
  }

  @Override
  public void postProcess(Class<?> type, Object instance, ApplicationContext applicationContext) {
    if (type.isAnnotationPresent(Controller.class)) {
      for (Method method : type.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Route.class)) {
          if (method.getReturnType() != HttpResponse.class)
            throw new RuntimeException("Not a valid Route handler -> return a HttpResponse");
          // todo remove this error handling and deal with it later
          if (!Arrays.asList(method.getParameterTypes()).contains(HttpRequest.class))
            throw new RuntimeException("Not a valid Route handler -> take a HttpRequest as input");

          Route route = method.getAnnotation(Route.class);
          RouteDefinition routeDefinition =
              new RouteDefinition(route.path(), route.method(), method, instance);

          routeRegistry.register(routeDefinition);
        }
      }
    }
  }
}
