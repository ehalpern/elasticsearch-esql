package org.twine.esql;

import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.parser.TokenMgrError;

/**
 * Created by eric on 4/22/15.
 */
public class EsqlSyntaxException extends EsqlInputException
{
  public EsqlSyntaxException(String message) {
    super(message);
  }

  public EsqlSyntaxException(Throwable cause) {
    super(constructMessage(findRootCause(cause)));
  }

  private static Throwable findRootCause(Throwable top) {
    Throwable cause = top;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }

  private static String constructMessage(Throwable t) {
    if (t instanceof ParseException) {

      StringBuffer part = new StringBuffer();
      Token first = ((ParseException)t).currentToken;
      Token cur = first;
      int line = cur.beginLine;
      int col = cur.beginColumn;
      for (; cur != null; cur = cur.next) {
        part.append(cur.image + " ");
      }
      return String.format("Syntax error at line %s, column %s: \"%s\"", line, col, part.toString());
    } else {
      return t.getMessage();
    }
  }
}
