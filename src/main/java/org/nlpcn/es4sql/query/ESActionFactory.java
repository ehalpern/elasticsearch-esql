package org.nlpcn.es4sql.query;


import org.durid.sql.SQLUtils;
import org.durid.sql.ast.expr.SQLQueryExpr;
import org.durid.sql.ast.statement.SQLDeleteStatement;
import org.durid.sql.parser.SQLParserUtils;
import org.durid.sql.parser.SQLStatementParser;
import org.durid.util.JdbcUtils;
import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.Delete;
import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.parse.SqlParser;

import java.sql.SQLFeatureNotSupportedException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ESActionFactory {

	/**
	 * Create the compatible Query object
	 * based on the SQL query.
	 *
	 * @param sql The SQL query.
	 * @return Query object.
	 */
	public static QueryAction create(Client client, String sql) throws SqlParseException, SQLFeatureNotSupportedException {
		sql = fixEsqlWhere(sql);
		String firstWord = sql.substring(0, sql.indexOf(' '));
		switch (firstWord.toUpperCase()) {
			case "SELECT":
				SQLQueryExpr sqlExpr = (SQLQueryExpr) SQLUtils.toMySqlExpr(sql);
				Select select = new SqlParser().parseSelect(sqlExpr);

				if (select.isAgg) {
					return new AggregationQueryAction(client, select);
				} else {
					return new DefaultQueryAction(client, select);
				}
			case "DELETE":
				SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcUtils.MYSQL);
				SQLDeleteStatement deleteStatement = parser.parseDeleteStatement();
				Delete delete = new SqlParser().parseDelete(deleteStatement);
				return new DeleteQueryAction(client, delete);

			default:
				throw new SQLFeatureNotSupportedException(String.format("Unsupported query: %s", sql));
		}
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
