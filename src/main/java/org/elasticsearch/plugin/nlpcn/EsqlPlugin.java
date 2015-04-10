package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.script.ScriptModule;
import org.elasticsearch.search.aggregations.metrics.first.CombineScript;
import org.elasticsearch.search.aggregations.metrics.first.MapScript;
import org.elasticsearch.search.aggregations.metrics.first.ReduceScript;

public class EsqlPlugin extends AbstractPlugin
{
	private static ESLogger LOG = ESLoggerFactory.getLogger(EsqlPlugin.class.getName());

	@Override
	public String name() {
		return "esql";
	}

	@Override
	public String description() {
		return "Use sql to query elasticsearch.";
	}

	public void onModule(RestModule module)
	{
		LOG.info("Registering request handlers");
		module.addRestAction(RestSqlAction.class);
	}

	/**
	 * Register native scripts used to choose a field value from the first document
	 * encountered during an aggregation
	 */
	public void onModule(ScriptModule module) {
		LOG.info("Registering native scripts");
		module.registerScript("first_map",    MapScript.class);
		module.registerScript("first_combine",CombineScript.class);
		module.registerScript("first_reduce", ReduceScript.class);
	}}
