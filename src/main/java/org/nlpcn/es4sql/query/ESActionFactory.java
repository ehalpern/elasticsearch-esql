package org.nlpcn.es4sql.query;

import org.elasticsearch.client.Client;
import org.twine.esql.EsqlCommand;
import org.twine.esql.EsqlInterpreter;

public class ESActionFactory
{
	/**
	 * Create search query from esql string
	 */
	public static QueryAction create(Client client, String sql)
	{
		//sql = EsqlUtil.compressWhitespace(sql);
		EsqlCommand command = EsqlInterpreter.interpret(sql);
		return command.actionQuery(client);
	}
}
