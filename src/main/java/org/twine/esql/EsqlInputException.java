package org.twine.esql;

import org.elasticsearch.ElasticsearchParseException;

/**
 * Created by eric on 4/9/15.
 */
public class EsqlInputException extends ElasticsearchParseException
{
  public EsqlInputException(String message) {
    super(message);
  }

  public EsqlInputException(String message, Throwable cause) {
    super(message, cause);
  }
}
