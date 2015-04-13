package org.twine.sql.processor;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlProcessor
{
	public static void process(String sql)
		throws JSQLParserException {
		Statement stmt = CCJSqlParserUtil.parse(sql);
		stmt.accept(new StatementProcessor());
	}

	private static String fixEsqlWhere(String sql) {
		String sqlPattern = "(SELECT .+ FROM .+ WHERE )(.+:((?!GROUP BY|ORDER BY|LIMIT).)+)((GROUP BY|ORDER BY|LIMIT).+)?";
		Pattern r = Pattern.compile(sqlPattern);
		Matcher m = r.matcher(sql);
		if (m.find()) {
			String where = String.format("q = query(\"%s\")", m.group(2));
			String rest = m.group(4) == null ? "" : m.group(4);
			String newSql = String.format("%s%s %s", m.group(1), where, rest);
			return newSql;
		} else {
			return sql;
		}
	}

}
