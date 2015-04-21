package org.twine.esql;

import org.parboiled.support.ParsingResult;
import org.twine.esqs.EsqsParser;

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
    Pattern r = Pattern.compile(sqlPattern, Pattern.CASE_INSENSITIVE);
    Matcher m = r.matcher(compressed);

    if (!m.find()) {
      return sql;
    } else {
      String esqs = m.group(2);
      ParsingResult pr = EsqsParser.parse(esqs);
      if (!pr.matched) {
        return sql;
      } else {
        String quotedQueryString = EsqlUtil.wrapWithSingleQuotes(esqs);
        String where = String.format("_q = query(%s)", quotedQueryString);
        String rest = m.group(4) == null ? "" : m.group(4);
        String newSql = String.format("%s%s %s", m.group(1), where, rest);
        return newSql;
      }
    }
  }

  /**
   * Surround by single quotes and escape all internal quotes
   */
  public static String wrapWithSingleQuotes(String esQueryString) {
    String sqlString =
      "'"
      + esQueryString.trim().replaceAll("(['\"])", "\\\\$1")
      + "'";
    return sqlString;
  }

  /**
   * Remove enclosing single quotes and unescape internal quotes
   */
  public static String unwrapSingleQuotes(String sqlString) {
    String noQuotes = sqlString.replaceAll("^\\s*'(.+)'\\s*$", "$1");
    String esQueryString = noQuotes.replaceAll("\\\\(['\"])", "$1");
    return esQueryString;
  }

  public static String compressWhitespace(String s) {
    return s.trim().replaceAll("\\s+", " ");
  }

  /**
   * Replace single quotes with double quotes.  Add double quotes even
   * if single quotes are missing.
   */
  public static String replaceSingleQuoteWrapperWithDouble(String s) {
    return s.replaceAll("%", "*").replaceAll("'(.+)'", "\"$1\"");
  }

  public static String removeQuotesAndReplaceWildcards(String s) {
    return s.replaceAll("%", "*").replaceAll("'(.+)'", "$1");
  }
}
