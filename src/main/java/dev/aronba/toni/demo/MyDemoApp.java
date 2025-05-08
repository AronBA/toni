package dev.aronba.toni.demo;

import dev.aronba.toni.ToniApplication;
import dev.aronba.toni.context.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MyDemoApp {
  private final Logger logger = LoggerFactory.getLogger(MyDemoApp.class);

  private final DatabaseService databaseService;

  public MyDemoApp(DatabaseService databaseService) {
    this.databaseService = databaseService;
  }

  public static void main(String[] args) {
    ToniApplication.run(args);
  }
}
