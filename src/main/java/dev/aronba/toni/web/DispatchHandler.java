package dev.aronba.toni.web;

import dev.aronba.toni.context.Component;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DispatchHandler implements HttpHandler {
  private final Logger logger = LoggerFactory.getLogger(DispatchHandler.class);
  private final RouteRegistry routeRegistry;

  public DispatchHandler(RouteRegistry routeRegistry) {
    this.routeRegistry = routeRegistry;
  }

  @Override
  public void handleRequest(HttpServerExchange httpServerExchange) {

    HttpString method = httpServerExchange.getRequestMethod();
    String path = httpServerExchange.getRequestPath();
    httpServerExchange
        .getRequestReceiver()
        .receiveFullString(
            (ex, body) -> {
              RouteDefinition routeDefinition =
                  routeRegistry.get(RouteKey.of(method.toString(), path));
              if (routeDefinition == null) logger.debug("halllo gall");
              HttpRequest request = HttpRequest.builder().body(body).build();
              try {
                HttpResponse httpResponse =
                    (HttpResponse)
                        routeDefinition
                            .responseHandler()
                            .invoke(routeDefinition.controllerInstance(), request);

                ex.setStatusCode(httpResponse.statusCode());
                ex.getResponseSender().send(httpResponse.body());
              } catch (Exception e) {
                logger.error(e.getMessage());
              } finally {
                ex.endExchange();
              }
            });
  }
}
