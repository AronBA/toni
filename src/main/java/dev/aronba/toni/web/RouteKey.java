package dev.aronba.toni.web;

import java.util.Objects;

public class RouteKey {

  private final String path;
  private final String method;

  public RouteKey(String method, String path) {
    this.path = path;
    this.method = method;
  }

  public static RouteKey of(RouteDefinition routeDefinition) {
    return new RouteKey(routeDefinition.httpMethod(), routeDefinition.path());
  }

  public static RouteKey of(String method, String path) {
    return new RouteKey(method, path);
  }

  @Override
  public String toString() {
    return method + " " + path;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    RouteKey routeKey = (RouteKey) o;
    return Objects.equals(path, routeKey.path) && Objects.equals(method, routeKey.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, method);
  }
}
