package org.nlpcn.es4sql.query.explain;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.support.QuerySourceBuilder;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Transform ActionRequestBuilder into json.
 */
public class ExplainManager {

	public static String explain(ActionRequestBuilder actionRequest)
		throws IOException, SQLFeatureNotSupportedException
	{
		XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();

		if (actionRequest instanceof SearchRequestBuilder) {
			((SearchRequestBuilder) actionRequest).internalBuilder().toXContent(builder, ToXContent.EMPTY_PARAMS);
		} else {
			throw new SQLFeatureNotSupportedException(String.format("Failed to explain class %s", actionRequest.getClass().getName()));
		}

		return builder.string();
	}
}
