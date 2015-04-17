package org.nlpcn.es4sql;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.nlpcn.es4sql.TestsConstants.DATE_FORMAT;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX;


public class QueryTest
{
	@Test
	public void searchType() {
		SearchHits response = query(String.format(
			"SELECT * FROM %s.phrase LIMIT 1000", TEST_INDEX
		));
		assertEquals(4, response.getTotalHits());
	}

	@Ignore @Test // not supported yet
	public void multipleFrom() {
		SearchHits response = query(String.format("SELECT * FROM %s.phrase, %s.account LIMIT 2000", TEST_INDEX, TEST_INDEX));
		assertEquals(1004, response.getTotalHits());
	}

	@Ignore @Test
	public void indexWithWildcard() {
		SearchHits response = query("SELECT * FROM elasticsearch-* LIMIT 1000");
		assertThat(response.getTotalHits(), greaterThan(0L));
	}


	@Test
	public void selectSpecificFields() {
		String[] arr = new String[] {"age", "account_number"};
		Set expectedSource = new HashSet(Arrays.asList(arr));

		SearchHits response = query(String.format("SELECT age, account_number FROM %s.account", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			assertEquals(expectedSource, hit.getSource().keySet());
		}
	}


	// TODO field aliases is not supported currently. it might be possible to change field names after the query already executed.
	/*
	@Test
	public void selectAliases() {
		String[] arr = new String[] {"myage", "myaccount_number"};
		Set expectedSource = new HashSet(Arrays.asList(arr));

		SearchHits response = query(String.format("SELECT age AS myage, account_number AS myaccount_number FROM %s/account", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			assertEquals(expectedSource, hit.getSource().keySet());
		}
	}
	*/


	@Test
	public void equality() {
		SearchHits response = query(String.format("select * from %s.account where city = 'Nogal' LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		// assert the results is correct according to accounts.json data.
		assertEquals(1, response.getTotalHits());
		assertEquals("Nogal", hits[0].getSource().get("city"));
	}


	// TODO search 'quick fox' still matching 'quick fox brown' this is wrong behavior.
	// in some cases, depends on the analasis, we might want choose better behavior for equallity.
	@Test
	public void equalityPhrase() {
		SearchHits response = query(String.format("SELECT * FROM %s.phrase WHERE phrase = 'quick fox here' LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		// assert the results is correct according to accounts.json data.
		assertEquals(1, response.getTotalHits());
		assertEquals("quick fox here", hits[0].getSource().get("phrase"));
	}


	@Test
	public void greaterThanTest() {
		int someAge = 25;
		SearchHits response = query(String.format("SELECT * FROM %s WHERE age > %s LIMIT 1000", TEST_INDEX, someAge));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			int age = (int) hit.getSource().get("age");
			assertThat(age, greaterThan(someAge));
		}
	}


	@Test
	public void greaterThanOrEqual() {
		int someAge = 25;
		SearchHits response = query(String.format("SELECT * FROM %s WHERE age >= %s LIMIT 1000", TEST_INDEX, someAge));
		SearchHit[] hits = response.getHits();

		boolean isEqualFound = false;
		for(SearchHit hit : hits) {
			int age = (int) hit.getSource().get("age");
			assertThat(age, greaterThanOrEqualTo(someAge));

			if(age == someAge)
				isEqualFound = true;
		}

		assertTrue(String.format("at least one of the documents need to contains age equal to %s", someAge), isEqualFound);
	}


	@Test
	public void lessThanTest() {
		int someAge = 25;
		SearchHits response = query(String.format("SELECT * FROM %s WHERE age < %s LIMIT 1000", TEST_INDEX, someAge));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			int age = (int) hit.getSource().get("age");
			assertThat(age, lessThan(someAge));
		}
	}


	@Test
	public void lessThanOrEqual() {
		int someAge = 25;
		SearchHits response = query(String.format("SELECT * FROM %s WHERE age <= %s LIMIT 1000", TEST_INDEX, someAge));
		SearchHit[] hits = response.getHits();

		boolean isEqualFound = false;
		for(SearchHit hit : hits) {
			int age = (int) hit.getSource().get("age");
			assertThat(age, lessThanOrEqualTo(someAge));

			if(age == someAge)
				isEqualFound = true;
		}

		assertTrue(String.format("at least one of the documents need to contains age equal to %s", someAge), isEqualFound);
	}


	@Test
	public void or() {
		SearchHits response = query(String.format("SELECT * FROM %s WHERE gender='F' OR gender='M' LIMIT 1000", TEST_INDEX));
		// Assert all documents from accounts.json is returned.
		assertEquals(1000, response.getTotalHits());
	}


	@Test
	public void and() {
		SearchHits response = query(String.format("SELECT * FROM %s WHERE age=32 AND gender='M' LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			assertEquals(32, hit.getSource().get("age"));
			assertEquals("M", hit.getSource().get("gender"));
		}
	}


	@Test
	public void like() {
		SearchHits response = query(String.format("SELECT * FROM %s WHERE firstname LIKE 'amb%%' LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		// assert the results is correct according to accounts.json data.
		assertEquals(1, response.getTotalHits());
		assertEquals("Amber", hits[0].getSource().get("firstname"));
	}


	@Test
	public void limit() {
		SearchHits response = query(String.format("SELECT * FROM %s LIMIT 30", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		// assert the results is correct according to accounts.json data.
		assertEquals(30, hits.length);
	}


	@Test
	public void between() {
		int min = 27;
		int max = 30;
		SearchHits response = query(String.format("SELECT * FROM %s WHERE age BETWEEN %s AND %s LIMIT 1000", TEST_INDEX, min, max));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			int age = (int) hit.getSource().get("age");
			assertThat(age, allOf(greaterThanOrEqualTo(min), lessThanOrEqualTo(max)));
		}
	}


	/*
	TODO when using not between on some field, documents that not contains this
	 field will return as well, That may considered a Wrong behaivor.
	 */
	@Test
	public void notBetween() {
		int min = 20;
		int max = 37;
		SearchHits response = query(String.format("SELECT * FROM %s WHERE age NOT BETWEEN %s AND %s LIMIT 1000", TEST_INDEX, min, max));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			Map<String, Object> source = hit.getSource();

			// ignore document which not contains the age field.
			if(source.containsKey("age")) {
				int age = (int) hit.getSource().get("age");
				assertThat(age, not(allOf(greaterThanOrEqualTo(min), lessThanOrEqualTo(max))));
			}
		}
	}


	@Test
	public void in(){
		SearchHits response = query(String.format("SELECT age FROM %s.phrase WHERE age IN (20, 22) LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			int age = (int) hit.getSource().get("age");
			assertThat(age, isOneOf(20, 22));
		}
	}


	@Test
	public void inTestWithStrings(){
		SearchHits response = query(String.format("SELECT phrase FROM %s.phrase WHERE phrase IN ('quick fox here', 'fox brown') LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		assertEquals(2, response.getTotalHits());
		for(SearchHit hit : hits) {
			String phrase = (String) hit.getSource().get("phrase");
			assertThat(phrase, isOneOf("quick fox here", "fox brown"));
		}
	}


	/* TODO when using not in on some field, documents that not contains this
	field will return as well, That may considered a Wrong behaivor.
	*/
	@Test
	public void notIn() {
		SearchHits response = query(String.format("SELECT age FROM %s WHERE age NOT IN (20, 22) LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			Map<String, Object> source = hit.getSource();

			// ignore document which not contains the age field.
			if(source.containsKey("age")) {
				int age = (int) source.get("age");
				assertThat(age, not(isOneOf(20, 22)));
			}
		}
	}
	
	
	@Ignore @Test // Date formatting not working yet
	public void dateSearch() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
		DateTime dateToCompare = new DateTime(2014, 8, 18, 0, 0, 0);

		SearchHits response = query(String.format("SELECT insert_time FROM %s.online WHERE insert_time < '2014-08-18'", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			Map<String, Object> source = hit.getSource();
			DateTime insertTime = formatter.parseDateTime((String) source.get("insert_time"));

			String errorMessage = String.format("insert_time must be smaller then 2014-08-18. found: %s", insertTime);
			assertTrue(errorMessage, insertTime.isBefore(dateToCompare));
		}
	}

    //@Test disable broken test: fails with Invalid format: "{ts '2015-03-13 13:27:33.954'}"
    public void dateSearchBraces() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
        DateTime dateToCompare = new DateTime(2015, 1, 15, 0, 0, 0);

        SearchHits response = query(String.format("SELECT insert_time FROM %s.odbc WHERE insert_time < {ts '2015-03-15 00:00:00.000'}", TEST_INDEX));
        SearchHit[] hits = response.getHits();
        for(SearchHit hit : hits) {
            Map<String, Object> source = hit.getSource();
            DateTime insertTime = formatter.parseDateTime((String) source.get("insert_time"));

            String errorMessage = String.format("insert_time must be smaller then 2015-03-15. found: %s", insertTime);
            assertTrue(errorMessage, insertTime.isBefore(dateToCompare));
        }
    }


	@Ignore @Test
	public void dateBetweenSearch() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);

		DateTime dateLimit1 = new DateTime(2014, 8, 18, 0, 0, 0);
		DateTime dateLimit2 = new DateTime(2014, 8, 21, 0, 0, 0);

		SearchHits response = query(String.format("SELECT insert_time FROM %s.online WHERE insert_time BETWEEN '2014-08-18' AND '2014-08-21' LIMIT 3", TEST_INDEX));
		SearchHit[] hits = response.getHits();
		for(SearchHit hit : hits) {
			Map<String, Object> source = hit.getSource();
			DateTime insertTime = formatter.parseDateTime((String) source.get("insert_time"));

			boolean isBetween =
					(insertTime.isAfter(dateLimit1) || insertTime.isEqual(dateLimit1)) &&
					(insertTime.isBefore(dateLimit2) || insertTime.isEqual(dateLimit2));

			assertTrue("insert_time must be between 2014-08-18 and 2014-08-21", isBetween);
		}
	}


	@Test // should be NULL
	public void isNullSearch() {
		SearchHits response = query(String.format("SELECT * FROM %s.phrase WHERE insert_time IS NULL", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		// should be 2 according to the data.
		assertEquals(response.getTotalHits(), 2);
		for(SearchHit hit : hits) {
			assertThat(hit.getSource(), not(hasKey("insert_time")));
		}
	}

	@Test // should be NULL
	public void isNotNullSearch() {
		SearchHits response = query(String.format("SELECT * FROM %s.phrase WHERE insert_time IS NOT NULL", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		// should be 2 according to the data.
		assertEquals(response.getTotalHits(), 2);
		for(SearchHit hit : hits) {
			assertThat(hit.getSource(), hasKey("insert_time"));
		}
	}
	


	@Test
	public void complexConditionQuery(){
		String errorMessage = "Result does not exist to the condition (gender='m' AND (age> 25 OR account_number>5)) OR (gender='f' AND (age>30 OR account_number < 8)";

		SearchHits response = query(String.format("SELECT * FROM %s.account WHERE (gender='m' AND (age> 25 OR account_number>5)) OR (gender='f' AND (age>30 OR account_number < 8))", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		for(SearchHit hit : hits) {
			Map<String, Object> source = hit.getSource();
			String gender = ((String)source.get("gender")).toLowerCase();
			int age = (int)source.get("age");
			int account_number = (int) source.get("account_number");

			assertTrue(errorMessage, (gender.equals("m") && (age > 25 || account_number > 5)) || (gender.equals("f") && (age > 30 || account_number < 8)));
		}
	}


	@Test
	public void orderByAsc() {
		SearchHits response = query(String.format("SELECT age FROM %s.account ORDER BY age ASC LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		ArrayList<Integer> ages = new ArrayList<Integer>();
		for(SearchHit hit : hits) {
			ages.add((int)hit.getSource().get("age"));
		}

		ArrayList<Integer> sortedAges = (ArrayList<Integer>)ages.clone();
		Collections.sort(sortedAges);
		assertTrue("The list is not ordered ascending", sortedAges.equals(ages));
	}


	@Test
	public void orderByDesc() {
		SearchHits response = query(String.format("SELECT age FROM %s.account ORDER BY age DESC LIMIT 1000", TEST_INDEX));
		SearchHit[] hits = response.getHits();

		ArrayList<Integer> ages = new ArrayList<Integer>();
		for(SearchHit hit : hits) {
			ages.add((int)hit.getSource().get("age"));
		}

		ArrayList<Integer> sortedAges = (ArrayList<Integer>)ages.clone();
		Collections.sort(sortedAges, Collections.reverseOrder());
		assertTrue("The list is not ordered descending", sortedAges.equals(ages));
	}

    @Test
    public void multipartWhere(){
        SearchHits response = query(String.format("SELECT * FROM %s.account WHERE (firstname LIKE 'opal' OR firstname like 'rodriquez') AND (state like 'oh' OR state like 'hi')", TEST_INDEX));
        assertEquals(2, response.getTotalHits());
    }

    @Test // 11 results is wrong
    public void multipartWhere2(){
        SearchHits response = query(String.format("SELECT * FROM %s.account where ((account_number > 200 and account_number < 300) or gender like 'm') and (state like 'hi' or address like 'avenue')", TEST_INDEX));
        assertEquals(127, response.getTotalHits());
    }

    @Test
    public void multipartWhere3() {
        SearchHits response = query(String.format("SELECT * FROM %s.account where ((account_number > 25 and account_number < 75) and age >35 ) and (state like 'md' or (address like 'avenue' or address like 'street'))", TEST_INDEX));
        assertEquals(7, response.getTotalHits());
    }

	private SearchHits query(String query) {
		try {
			SearchDao searchDao = MainTestSuite.getSearchDao();
			SearchRequestBuilder select = (SearchRequestBuilder) searchDao.explain(query);
			return select.get().getHits();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
