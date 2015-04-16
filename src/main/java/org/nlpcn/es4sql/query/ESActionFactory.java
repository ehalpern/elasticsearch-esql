package org.nlpcn.es4sql.query;


import org.elasticsearch.client.Client;
import org.twine.esql.EsqlCommand;
import org.twine.esql.EsqlInterpreter;

public class ESActionFactory
{
	/**
	 * Create the compatible Query object
	 * based on the SQL query.
	 *
	 * @param sql The SQL query.
	 * @return Query object.
	 */
	public static QueryAction create(Client client, String sql)
	{
		//sql = EsqlUtil.compressWhitespace(sql);
		EsqlCommand command = EsqlInterpreter.interpret(sql);
		return command.actionQuery(client);
	}
}
