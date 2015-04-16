package org.nlpcn.es4sql;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.Field;
import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.query.ESActionFactory;
import org.nlpcn.es4sql.query.QueryAction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class SearchDao
{
	private final List<String> columns = new ArrayList<String>();
	private final Client client;

	public SearchDao(Client client) {
		this.client = client;
	}

	/**
	 * Prepare action And transform sql into ES ActionRequest
	 */
	public ActionRequestBuilder explain(String sql) throws SQLException
	{
		QueryAction query = ESActionFactory.create(client, sql);
		ActionRequestBuilder builder = query.explain();
		columns.clear();
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
