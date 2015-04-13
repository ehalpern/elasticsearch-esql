package org.twine.elasticsearch.esql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eric on 4/12/15.
 */
public class EsqlUtil
{
  public static String normalizeEsql(String sql) {
    String compressed = compressWhitespace(sql);
    String sqlPattern = "(SELECT .+ FROM .+ WHERE )(.+:((?!GROUP BY|ORDER BY|LIMIT).)+)((GROUP BY|ORDER BY|LIMIT).+)?";
    Pattern r = Pattern.compile(sqlPattern);
    Matcher m = r.matcher(compressed);
    if (m.find()) {
      String quotedQueryString = EsqlUtil.toSqlString(m.group(2));
      String where = String.format("_q = query(%s)", quotedQueryString);
      String rest = m.group(4) == null ? "" : m.group(4);
      String newSql = String.format("%s%s %s", m.group(1), where, rest);
      return newSql;
    } else {
      return sql;
    }
  }

  public static String toSqlString(String esQueryString) {
    String sqlString =
      "'"
      + esQueryString.trim().replaceAll("(['\"])", "\\$1")
      + "'";
    return sqlString;
  }

  public static String fromSqlString(String sqlString) {
    String noQuotes = sqlString.replaceAll("^\\s*'(.+)'\\s*$", "$1");
    String esQueryString = noQuotes.replaceAll("\\\\(['\"])", "$1");
    return esQueryString;
  }

  public static String compressWhitespace(String s) {
    return s.trim().replaceAll("\\s+", " ");
  }
}
