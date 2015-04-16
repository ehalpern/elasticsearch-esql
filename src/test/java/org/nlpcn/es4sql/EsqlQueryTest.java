package org.nlpcn.es4sql;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX;


public class EsqlQueryTest {

	//@Test
	public void whereQueryStringTest() throws IOException, SQLException, SQLFeatureNotSupportedException{
		String esql = String.format(
			"SELECT * FROM %s/accounts WHERE address:avenue && balance:<40000 LIMIT 10",
			//"SELECT * FROM %s/accounts WHERE address = matchQuery('880 Holmes Lane') LIMIT 10",
			TEST_INDEX);
		SearchHits response = query(esql);
		Assert.assertEquals(4, response.getTotalHits());
	}

	@Test
	public void sumQueryWithFieldsTest() throws IOException, SQLException, SQLFeatureNotSupportedException{
		String esql = String.format(
			"SELECT sum(balance), address FROM %s/accounts WHERE address:avenue",
			TEST_INDEX);
		SearchHits response = query(esql);
		Assert.assertEquals(4, response.getTotalHits());
	}

	//@Test
	public void selectSpecificFields() throws IOException, SQLException, SQLFeatureNotSupportedException {
		String[] arr = new String[] {"age", "account_number"};
		Set expectedSource = new HashSet(Arrays.asList(arr));

		SearchHits response = query(String.format("SELECT age, account_number FROM %s/account", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			Assert.assertEquals(expectedSource, hit.getSource().keySet());
		}
	}


	// TODO field aliases is not supported currently. it might be possible to change field names after the query already executed.
	/*
	@Test
	public void selectAliases() throws IOException, SQLException, SQLFeatureNotSupportedException {
		String[] arr = new String[] {"myage", "myaccount_number"};
		Set expectedSource = new HashSet(Arrays.asList(arr));

		SearchHits response = query(String.format("SELECT age AS myage, account_number AS myaccount_number FROM %s/account", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			Assert.assertEquals(expectedSource, hit.getSource().keySet());
		}
	}
	*/



	// TODO search 'quick fox' still matching 'quick fox brown' this is wrong behavior.
	// in some cases, depends on the analasis, we might want choose better behavior for equallity.
	//@Test
	public void equallityTest_phrase() throws SQLException, SQLFeatureNotSupportedException {
		SearchHits response = query(String.format("SELECT * FROM %s/phrase WHERE phrase = 'quick fox here' LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		// assert the results is correct according to accounts.json data.
		Assert.assertEquals(1, response.getTotalHits());
		Assert.assertEquals("quick fox here", hits[0].getSource().get("phrase"));
	}

	private SearchHits query(String query) throws SQLException, SQLFeatureNotSupportedException, SQLFeatureNotSupportedException {
		SearchDao searchDao = MainTestSuite.getSearchDao();
		SearchRequestBuilder select = (SearchRequestBuilder)searchDao.explain(query);
		return select.get().getHits();
	}
}
