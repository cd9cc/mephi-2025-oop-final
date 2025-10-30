package sessionapp.exceptions;

public class BadConfigurationException extends RuntimeException {
  private final Exception internalException;

  public BadConfigurationException(Exception e) {
    super(e.getMessage());
    internalException = e;
  }
}
