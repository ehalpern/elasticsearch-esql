package org.twine.esql;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

/**
 * some bad case 
 * @author ansj
 *
 */
public class BugTest {

	
	@Test
	public void bug1() throws IOException, SQLException {

		SearchRequestBuilder select = (SearchRequestBuilder) MainTestSuite.getSearchDao().explain("select count(*),sum(age) from bank");
		System.out.println(select);
	}
}
