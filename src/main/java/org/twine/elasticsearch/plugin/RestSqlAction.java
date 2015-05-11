package org.twine.elasticsearch.plugin;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.*;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.query.explain.ExplainManager;
import org.twine.esql.EsqlInputException;

import java.io.IOException;
import java.sql.SQLException;

public class RestSqlAction extends BaseRestHandler {
  private static ESLogger LOG = ESLoggerFactory.getLogger(EsqlPlugin.class.getName());


  @Inject
	public RestSqlAction(Settings settings, Client client, RestController restController) {
		super(settings, restController, client);
		restController.registerHandler(RestRequest.Method.POST, "/_esql/_explain", this);
		restController.registerHandler(RestRequest.Method.GET, "/_esql/_explain", this);
		restController.registerHandler(RestRequest.Method.POST, "/_esql", this);
		restController.registerHandler(RestRequest.Method.GET, "/_esql", this);
	}

	@Override
	protected void handleRequest(
		RestRequest request, RestChannel channel, final Client client
	)
		throws IOException, SQLException
	{
		String sql = request.param("sql");

		if (sql == null) {
			sql = request.content().toUtf8();
		}

		try {
			SearchDao searchDao = new SearchDao(client);
			ActionRequestBuilder esRequestBuilder = searchDao.explain(sql);
			ActionRequest esRequest = esRequestBuilder.request();

			if (request.path().endsWith("/_explain")) {
				String jsonExplanation = ExplainManager.explain(esRequestBuilder);
				BytesRestResponse response = new BytesRestResponse(RestStatus.OK, jsonExplanation);
				channel.sendResponse(response);
			} else {
				new RestSqlExecuter(esRequest, channel, client, searchDao.getColumns()).execute();
			}
		} catch (EsqlInputException e) {
			channel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, e.getMessage()));
		} catch (IOException|SQLException|RuntimeException e) {
			LOG.warn("Unexpected exception", e);
			throw e;
		}
	}
}
