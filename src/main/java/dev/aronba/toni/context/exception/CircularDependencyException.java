package dev.aronba.toni.context.exception;

public class CircularDependencyException extends RuntimeException {
  public CircularDependencyException() {
    super();
  }

  public CircularDependencyException(String s) {
    super(s);
  }
}
