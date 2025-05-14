package dev.aronba.toni.context;

public class UnsatisfiedDependencyException extends Exception {
  UnsatisfiedDependencyException(String message) {
    super(message);
  }
}
