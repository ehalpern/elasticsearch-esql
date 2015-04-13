package org.nlpcn.es4sql.query;


import org.durid.sql.ast.statement.SQLDeleteStatement;
import org.durid.sql.parser.SQLParserUtils;
import org.durid.sql.parser.SQLStatementParser;
import org.durid.util.JdbcUtils;
import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.Delete;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.parse.SqlParser;
import org.twine.elasticsearch.esql.EsqlCommand;
import org.twine.elasticsearch.esql.EsqlInputException;
import org.twine.elasticsearch.esql.EsqlInterpreter;
import org.twine.elasticsearch.esql.EsqlUtil;

import java.sql.SQLFeatureNotSupportedException;

public class ESActionFactory
{
	/**
	 * Create the compatible Query object
	 * based on the SQL query.
	 *
	 * @param sql The SQL query.
	 * @return Query object.
	 */
	public static QueryAction create(Client client, String sql) throws SqlParseException, SQLFeatureNotSupportedException {
		sql = EsqlUtil.compressWhitespace(sql);
		String firstWord = sql.substring(0, sql.indexOf(' '));
		switch (firstWord.toUpperCase()) {
			case "SELECT":
				EsqlCommand command;
				try {
					command = EsqlInterpreter.interpret(sql);
					//throw new EsqlInputException("");
				} catch (EsqlInputException e) {
					throw e;
					//SQLQueryExpr sqlExpr = (SQLQueryExpr) SQLUtils.toMySqlExpr(sql);
					///select = new SqlParser().parseSelect(sqlExpr);
				}
				return command.actionQuery(client);
			case "DELETE":
				SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcUtils.MYSQL);
				SQLDeleteStatement deleteStatement = parser.parseDeleteStatement();
				Delete delete = new SqlParser().parseDelete(deleteStatement);
				return new DeleteQueryAction(client, delete);

			default:
				throw new SQLFeatureNotSupportedException(String.format("Unsupported query: %s", sql));
		}
	}
}
