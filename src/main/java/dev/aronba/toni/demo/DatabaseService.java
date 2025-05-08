package dev.aronba.toni.demo;

import dev.aronba.toni.context.Component;
import dev.aronba.toni.context.PostConstruct;

@Component
public class DatabaseService {

  @PostConstruct
  void init() {

    System.out.println("---------------------------------");
    System.out.println("this is post constrc");
    System.out.println("---------------------------------");
  }
}
