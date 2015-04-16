package org.twine.esql;

/**
 * Created by eric on 4/9/15.
 */
public class EsqlInputException extends IllegalArgumentException
{
  public EsqlInputException(String message) {
    super(message);
  }

  public EsqlInputException(String message, Throwable cause) {
    super(message, cause);
  }

  public EsqlInputException(Throwable cause) {
    super("Unexpected", cause);
  }
}
