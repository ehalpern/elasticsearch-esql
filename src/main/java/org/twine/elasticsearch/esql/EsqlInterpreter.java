package org.twine.elasticsearch.esql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.twine.sql.processor.StatementProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eric on 4/8/15.
 */
public class EsqlInterpreter
{
  public static EsqlCommand interpret(String esql)
    throws EsqlInputException
  {
    EsqlCommand cmd = new EsqlCommand();
    try {
      Statement statement = CCJSqlParserUtil.parse(fixEsqlWhere(esql));
      statement.accept(new StatementProcessor(cmd.statementBuilder()));
      return cmd;
    } catch (JSQLParserException e) {
      throw new EsqlInputException("Invalid syntax", e);
    }
  }

  private static String fixEsqlWhere(String sql) {
    String sqlPattern = "(SELECT .+ FROM .+ WHERE )(.+:((?!GROUP BY|ORDER BY|LIMIT).)+)((GROUP BY|ORDER BY|LIMIT).+)?";
    Pattern r = Pattern.compile(sqlPattern);
    Matcher m = r.matcher(sql);
    if (m.find()) {
      String where = String.format("q = query('%s')", m.group(2));
      String rest = m.group(4) == null ? "" : m.group(4);
      String newSql = String.format("%s%s %s", m.group(1), where, rest);
      return newSql;
    } else {
      return sql;
    }
  }
}
