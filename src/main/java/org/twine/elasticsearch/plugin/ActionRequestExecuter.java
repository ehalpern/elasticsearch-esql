package org.twine.elasticsearch.plugin;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.support.RestBuilderListener;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.scripted.ScriptedMetric;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ActionRequestExecuter
{

	private RestChannel channel;
	private Client client;
	private ActionRequest request;
	private List<String> columns;

	public ActionRequestExecuter(
		ActionRequest request, RestChannel channel, final Client client, List<String> columns
	) {
		this.request = request;
		this.channel = channel;
		this.client = client;
		this.columns = columns;
	}

	/**
	 * Execute the ActionRequest and returns the REST response using the channel.
	 */
	public void execute() {
		request.listenerThreaded(false);

		if (request instanceof SearchRequest) {
			//client.search((SearchRequest) request, new RestStatusToXContentListener<SearchResponse>(channel));
			client.search((SearchRequest) request, new EsqlResponseTransformer(channel, columns));
		} else {
			throw new AssertionError(String.format("Unsupported ActionRequest provided: %s", request.getClass().getName()));
		}
	}

	static class EsqlResponseTransformer extends RestBuilderListener<SearchResponse>
	{
		private final List<String> columns;

		public EsqlResponseTransformer(RestChannel channel, List<String> columns) {
			super(channel);
			this.columns = columns;
		}

		public RestResponse buildResponse(SearchResponse searchResponse, XContentBuilder builder)
			throws IOException
		{
			XContentBuilder b = builder.startObject();
			b.field("took", searchResponse.getTook());
			b.field("timed_out", searchResponse.isTimedOut());
			b.field("hits", searchResponse.getHits().getTotalHits());
			b.startObject("_shards");
			b.field("total", searchResponse.getTotalShards());
			b.field("successful", searchResponse.getSuccessfulShards());
			b.field("failures", searchResponse.getFailedShards());
			b.endObject();
			transformHits(searchResponse.getHits(), b);
			transformAggregations(searchResponse.getAggregations(), b);
			b.endObject();
			return new BytesRestResponse(searchResponse.status(), b);
		}

		private XContentBuilder transformHits(SearchHits hits, XContentBuilder builder)
			throws IOException
		{
			XContentBuilder b = builder;
			if (hits != null && hits.totalHits() > 0) {
				b.startArray("rows");
				try {
					for (SearchHit hit : hits.hits()) {
						buildWithFilter(hit.getSource(), columns, b);
					}
				} finally {
					b.endArray();
				}
			}
			return b;
		}

		private XContentBuilder transformAggregations(Aggregations aggs, XContentBuilder builder)
			throws IOException
		{
			XContentBuilder b = builder;
			if (aggs != null) {
				b.startArray("rows");
				try {
					Map<String, Aggregation> map = aggs.asMap();
					for (Map.Entry<String, Aggregation> e : map.entrySet()) {
						Aggregation agg = e.getValue();
						if (agg instanceof MultiBucketsAggregation) {
							assert map.size() == 1;
							MultiBucketsAggregation mba = (MultiBucketsAggregation) agg;
							transformBuckets(e.getKey(), mba, b);
						} else {
							buildWithFilter(getColumnsFromAggregations(aggs), columns, b);
						}
					}
				} finally {
					b.endArray();
				}
			}
			return b;
		}

		private XContentBuilder transformBuckets(String name, MultiBucketsAggregation agg, XContentBuilder builder)
			throws IOException
		{
			XContentBuilder b = builder;
			for (MultiBucketsAggregation.Bucket bucket: agg.getBuckets()) {
				Map<String, Object> colMap = getColumnsFromAggregations(bucket.getAggregations());
				colMap.put(agg.getName(), bucket.getKey());
				buildWithFilter(colMap, columns, b);
			}
			return b;
		}

		private XContentBuilder buildWithFilter(Map<String, Object> map, List<String> filter, XContentBuilder builder)
			throws IOException
		{
			if (filter.isEmpty()) {
				builder.map(map);
			} else {
				builder.startObject();
				for (String key : filter) {
					if (map.containsKey(key)) {
						builder.field(key, map.get(key));
					}
				}
				builder.endObject();
			}
			return builder;
		}

		private Map<String, Object> getColumnsFromAggregations(Aggregations aggs) {
			Map<String, Object> columns = new HashMap();
			for (Map.Entry<String, Aggregation> e : aggs.asMap().entrySet()) {
				Aggregation agg = e.getValue();
				if (agg.getName().equals("fields")) {
					columns.putAll(getFirstValueColumns((ScriptedMetric) agg));
				} else {
					Map.Entry<String, Object> column = getColumnFromMetricAggregation(agg);
					columns.put(column.getKey(), column.getValue());
				}
			}
			return columns;
		}

		private Map.Entry<String, Object> getColumnFromMetricAggregation(Aggregation agg) {
			if (agg instanceof Sum) {
				return new SimpleEntry(agg.getName(), ((Sum)agg).getValue());
			} else if (agg instanceof Min) {
				return new SimpleEntry(agg.getName(), ((Min)agg).getValue());
			} else if (agg instanceof Max) {
				return new SimpleEntry(agg.getName(), ((Max)agg).getValue());
			} else if (agg instanceof Avg) {
				return new SimpleEntry(agg.getName(), ((Avg)agg).getValue());
			} else if (agg instanceof ValueCount) {
				return new SimpleEntry(agg.getName(), ((ValueCount)agg).getValue());
			} else if (agg instanceof Cardinality) {
				return new SimpleEntry(agg.getName(), ((Cardinality)agg).getValue());
			} else {
				throw new UnsupportedOperationException("unexpected aggregation type " + agg.getClass().getName());
			}
		}

		private Map<String, Object> getFirstValueColumns(ScriptedMetric sm)
		{
			return (Map<String,Object>)sm.aggregation();
		}
	}
}
