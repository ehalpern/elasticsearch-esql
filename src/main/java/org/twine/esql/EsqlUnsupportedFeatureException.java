package org.twine.esql;

/**
 * Created by eric on 4/22/15.
 */
public class EsqlUnsupportedFeatureException extends EsqlInputException
{
  public EsqlUnsupportedFeatureException(String message) {
    super(message);
  }

  public EsqlUnsupportedFeatureException(String message, Throwable cause) {
    super(message, cause);
  }
}
