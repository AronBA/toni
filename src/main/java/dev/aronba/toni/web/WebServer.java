package dev.aronba.toni.web;

import dev.aronba.toni.context.Component;
import dev.aronba.toni.context.PostConstruct;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebServer {
  private static final Logger logger = LoggerFactory.getLogger(WebServer.class);

  private final HttpHandler httpHandler;

  public WebServer(DispatchHandler httpHandler) {
    this.httpHandler = httpHandler;
  }

  @PostConstruct
  public void init() {
    logger.info("initiating WebServer");

    Undertow undertow =
        Undertow.builder().addHttpListener(2003, "localhost").setHandler(this.httpHandler).build();
    undertow.start();
  }
}
