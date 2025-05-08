package dev.aronba.toni.web;

import dev.aronba.toni.context.Component;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RouteRegistry {
  private final Logger logger = LoggerFactory.getLogger(RouteRegistry.class);
  private final Map<RouteKey, RouteDefinition> routes = new HashMap<>();

  public void register(RouteDefinition routeDefinition) {
    RouteKey key = RouteKey.of(routeDefinition);

    if (routes.containsKey(key)) {
      throw new RuntimeException("The route " + routeDefinition.path() + " is already mapped");
    }
    routes.put(key, routeDefinition);
    logger.debug("registered route : {}", key);
  }

  public RouteDefinition get(RouteKey routeKey) {
    return this.routes.get(routeKey);
  }
}
