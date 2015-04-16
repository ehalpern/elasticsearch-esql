package org.nlpcn.es4sql.query;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.Query;
import java.sql.SQLSyntaxErrorException;

/**
 * Abstract class. used to transform Select object (Represents SQL query) to
 * SearchRequestBuilder (Represents ES query)
 */
public abstract class QueryAction {

	protected Query query;

	protected Client client;


	public QueryAction(Client client, Query query) {
		this.client = client;
		this.query = query;
	}


	/**
	 * Prepare the request, and return ES request.
	 * @return ActionRequestBuilder (ES request)
	 * @throws SQLSyntaxErrorException
	 */
	public abstract ActionRequestBuilder explain() throws SQLSyntaxErrorException;

	public Query getQuery() {
		return query;
	}
}
