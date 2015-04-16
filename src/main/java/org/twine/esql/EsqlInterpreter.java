package org.twine.esql;

/**
 * Created by eric on 4/8/15.
 */
public class EsqlInterpreter
{
  public static EsqlCommand interpret(String esql)
    throws EsqlInputException
  {
    return EsqlBuilder.parse(esql);
  }
}
