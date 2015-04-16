package org.nlpcn.es4sql;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX;


public class EsqlQueryTest
{
	@Test
	public void whereQueryStringTest() throws IOException, SQLException {
		String esql = String.format(
			"SELECT * FROM %s.account WHERE address:\"880 Holmes Lane\" LIMIT 10",
			TEST_INDEX);
		SearchHits response = query(esql);
		Assert.assertEquals(1, response.getTotalHits());
	}

	private SearchHits query(String query) throws SQLException {
		SearchDao searchDao = MainTestSuite.getSearchDao();
		SearchRequestBuilder select = (SearchRequestBuilder)searchDao.explain(query);
		return select.get().getHits();
	}
}
