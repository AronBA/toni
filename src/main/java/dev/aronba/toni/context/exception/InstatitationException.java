package dev.aronba.toni.context.exception;

public class InstatitationException extends Exception {
  public InstatitationException(String message) {
    super(message);
  }

  public InstatitationException(ReflectiveOperationException e) {
    super(e);
  }
}
