package org.nlpcn.es4sql;

import com.google.common.io.Files;
import org.elasticsearch.action.ActionRequestBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.nlpcn.es4sql.query.explain.ExplainManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX;

@Ignore // Disable for now because query json has changed
public class ExplainTest {

	@Test
	public void searchSanity() throws IOException, SQLException, NoSuchMethodException, IllegalAccessException, SQLFeatureNotSupportedException, InvocationTargetException {
		String expectedOutput = Files.toString(new File("src/test/resources/expectedOutput/search_explain.json"), StandardCharsets.UTF_8);
		String result = explain(String.format("SELECT * FROM %s WHERE firstname LIKE 'A%%' AND age > 20 GROUP BY gender", TEST_INDEX));

		assertThat(result, equalTo(expectedOutput));
	}

	@Test
	public void deleteSanity() throws IOException, SQLException, NoSuchMethodException, IllegalAccessException, SQLFeatureNotSupportedException, InvocationTargetException {
		String expectedOutput = Files.toString(new File("src/test/resources/expectedOutput/delete_explain.json"), StandardCharsets.UTF_8);
		String result = explain(String.format("DELETE FROM %s WHERE firstname LIKE 'A%%' AND age > 20", TEST_INDEX));

		assertThat(result, equalTo(expectedOutput));
	}


	private String explain(String sql) throws SQLFeatureNotSupportedException, SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
		SearchDao searchDao = MainTestSuite.getSearchDao();
		ActionRequestBuilder requestBuilder = searchDao.explain(sql);
		return ExplainManager.explain(requestBuilder);
	}
}
