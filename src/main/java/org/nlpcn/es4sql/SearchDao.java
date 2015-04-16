package org.nlpcn.es4sql;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.Field;
import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.query.ESActionFactory;
import org.nlpcn.es4sql.query.QueryAction;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SearchDao {

	private static final Set<String> END_TABLE_MAP = new HashSet<>();

	static {
		END_TABLE_MAP.add("limit");
		END_TABLE_MAP.add("order");
		END_TABLE_MAP.add("where");
		END_TABLE_MAP.add("group");

	}

	private List<String> columns = new ArrayList<String>();

	private Client client = null;


	public SearchDao(Client client) {
		this.client = client;
	}


	/**
	 * Prepare action And transform sql
	 * into ES ActionRequest
	 * @param sql SQL query to execute.
	 * @return ES request
	 * @throws SQLSyntaxErrorException
	 */
	public ActionRequestBuilder explain(String sql)
		throws SQLException
	{

		QueryAction query = ESActionFactory.create(client, sql);
		ActionRequestBuilder builder = query.explain();
		columns = new ArrayList();
		List<Field> fields = ((Select)query.getQuery()).getFields();
		for (Field f: fields) {
			columns.add(f.toString());
		}
		return builder;
	}

	public List<String> getColumns() {
		return columns;
	}

}
