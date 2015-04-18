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
import static org.nlpcn.es4sql.EsMatchers.HitCount.*;
import static org.nlpcn.es4sql.EsMatchers.EachHit.*;
import static org.nlpcn.es4sql.EsMatchers.SomeHit.*;
import static org.junit.Assert.*;
import static org.nlpcn.es4sql.TestsConstants.DATE_FORMAT;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX;


public class QueryTest
{
	@Test
	public void searchType() {
		assertThat(
			query("SELECT * FROM %s.phrase LIMIT 1000", TEST_INDEX),
			hitCount(4)
		);
	}

	@Ignore @Test // not supported yet
	public void multipleFrom() {
		assertThat(
			query("SELECT * FROM %s.phrase, %s.account LIMIT 2000", TEST_INDEX, TEST_INDEX),
			hitCount(1004)
		);
	}

	@Ignore @Test
	public void indexWithWildcard() {
		assertThat(
			query("SELECT * FROM elasticsearch-* LIMIT 1000"),
			hitCount(greaterThan(0))
		);
	}


	@Test
	public void selectSpecificFields() {
		assertThat(
			query("SELECT age, account_number FROM %s.account", TEST_INDEX),
		  eachHit(allOf(hasKey("age"), hasKey("account_number")))
		);
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
		assertThat(
			query("select * from %s.account where city = 'Nogal' LIMIT 1000", TEST_INDEX),
			eachHit(hasEntry("city", "Nogal"))
		);
	}


	@Test
	public void equalityPhrase() {
		assertThat(
			query("SELECT * FROM %s.phrase WHERE phrase = 'quick fox here' LIMIT 1000", TEST_INDEX),
			eachHit(hasEntry("phrase", "quick fox here"))
		);
	}


	@Test
	public void greaterThanTest() {
		assertThat(
			query("SELECT * FROM %s WHERE age > 25 LIMIT 1000", TEST_INDEX),
			eachHit(hasEntry(is("age"), greaterThan(25)))
		);
	}


	@Test
	public void greaterThanOrEqual() {
		int age = 25;
		assertThat(
			query("SELECT * FROM %s WHERE age >= %s LIMIT 1000", TEST_INDEX, age),
			allOf(
				eachHit(hasEntry(is("age"), greaterThanOrEqualTo(age))),
				someHit(hasEntry("age", age))
			)
		);
	}


	@Test
	public void lessThanTest() {
		int age = 25;
		assertThat(
			query("SELECT * FROM %s WHERE age < %s LIMIT 1000", TEST_INDEX, age),
			eachHit(hasEntry(is("age"), lessThan(age)))
		);
	}


	@Test
	public void lessThanOrEqual() {
		int age = 25;
		assertThat(
			query("SELECT * FROM %s WHERE age <= %s LIMIT 1000", TEST_INDEX, age),
			allOf(
				eachHit(hasEntry(is("age"), lessThanOrEqualTo(age))),
				someHit(hasEntry("age", age))
			)
		);
	}


	@Test
	public void or() {
		assertThat(
			query("SELECT * FROM %s WHERE gender='F' OR gender='M' LIMIT 1000", TEST_INDEX),
			allOf(
				hitCount(1000),
				eachHit(
					anyOf(
						hasEntry("gender", "F"),
						hasEntry("gender", "M")
					)
				)
			)
		);
	}


	@Test
	public void and() {
		assertThat(
			query("SELECT * FROM %s WHERE age=32 AND gender='M' LIMIT 1000", TEST_INDEX),
			allOf(
				eachHit(hasEntry("age", 32)),
				eachHit(hasEntry("gender", "M"))
			)
		);
	}


	@Test
	public void like() {
		assertThat(
			query("SELECT * FROM %s WHERE firstname LIKE 'amb%%' LIMIT 1000", TEST_INDEX),
			eachHit(hasEntry("firstname", "Amber"))
		);
	}

	@Test
	public void notLike() {                                                                                  
		assertThat(
			query("SELECT * FROM %s WHERE firstname NOT LIKE 'amb%%' LIMIT 1000", TEST_INDEX),
			allOf(
				hitCount(greaterThan(1)),
				eachHit(not(hasEntry("firstname", "Amber")))
			)
		);
	}

	@Test
	public void limit() {
		assertThat(
			query("SELECT * FROM %s LIMIT 30", TEST_INDEX),
			hitCount(30)
		);
	}

	@Test
	public void between() {
		int min = 27;
		int max = 30;
		assertThat(
			query("SELECT * FROM %s WHERE age BETWEEN %s AND %s LIMIT 1000", TEST_INDEX, min, max),
			eachHit(
				hasEntry(
					is("age"),
					allOf(greaterThanOrEqualTo(min), lessThanOrEqualTo(max))
				)
			)
		);
	}


	/**
	 * @todo NOT currently includes documents that do not contain the field.  Should
	 *       documents that don't include the field be excluded?  What does MySQL do?
	 */
	@Test
	public void notBetween() {
		int min = 20;
		int max = 37;
		assertThat(
			query("SELECT * FROM %s WHERE age NOT BETWEEN %s AND %s LIMIT 1000", TEST_INDEX, min, max),
			eachHit(hasEntry(is("age"), allOf(lessThan(min), greaterThan(max))))
		);
	}

	@Test
	public void in(){
		assertThat(
			query("SELECT age FROM %s.phrase WHERE age IN (20, 22) LIMIT 1000", TEST_INDEX),
			eachHit(hasEntry(is("age"), isOneOf(20, 22)))
		);
	}

	@Test
	public void inTestWithStrings() {
		assertThat(
			query("SELECT phrase FROM %s.phrase WHERE phrase IN ('quick fox here', 'fox brown') LIMIT 1000", TEST_INDEX),
			eachHit(hasEntry(is("phrase"), isOneOf("quick fox here", "fox brown")))
		);
	}


	/**
	 * @see #notBetween
	*/
	@Test
	public void notIn() {
		assertThat(
			query("SELECT age FROM %s WHERE age NOT IN (20, 22) LIMIT 1000", TEST_INDEX),
			eachHit(not(hasEntry(is("age"), isOneOf(20, 22))))
		);
	}
	
	
	@Ignore @Test // Date formatting not working yet
	public void dateSearch() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
		DateTime dateToCompare = new DateTime(2014, 8, 18, 0, 0, 0);

		SearchHits response = query("SELECT insert_time FROM %s.online WHERE insert_time < '2014-08-18'", TEST_INDEX);
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

        SearchHits response = query("SELECT insert_time FROM %s.odbc WHERE insert_time < {ts '2015-03-15 00:00:00.000'}", TEST_INDEX);
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

		SearchHits response = query("SELECT insert_time FROM %s.online WHERE insert_time BETWEEN '2014-08-18' AND '2014-08-21' LIMIT 3", TEST_INDEX);
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


	@Test
	public void isNullSearch() {
		assertThat(
			query("SELECT * FROM %s.phrase WHERE insert_time IS NULL", TEST_INDEX),
			eachHit(not(hasKey("insertTime")))
		);
	}

	@Test
	public void isNotNullSearch() {
		assertThat(
			query("SELECT * FROM %s.phrase WHERE insert_time IS NOT NULL", TEST_INDEX),
			eachHit(hasKey("insert_time"))
		);
	}
	


	@Test
	public void complexConditionQuery(){
		String errorMessage = "Result does not exist to the condition (gender='m' AND (age> 25 OR account_number>5)) OR (gender='f' AND (age>30 OR account_number < 8)";

		SearchHits response = query("SELECT * FROM %s.account WHERE (gender='m' AND (age> 25 OR account_number>5)) OR (gender='f' AND (age>30 OR account_number < 8))", TEST_INDEX);
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
		SearchHits response = query("SELECT age FROM %s.account ORDER BY age ASC LIMIT 1000", TEST_INDEX);
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
		SearchHits response = query("SELECT age FROM %s.account ORDER BY age DESC LIMIT 1000", TEST_INDEX);
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
    public void multipartWhere() {
			assertThat(
        query("SELECT * FROM %s.account WHERE (firstname LIKE 'opal' OR firstname like 'rodriquez') AND (state like 'oh' OR state like 'hi')", TEST_INDEX),
				hitCount(2)
			);
    }

    @Test // 11 results is wrong
    public void multipartWhere2() {
			assertThat(
				query("SELECT * FROM %s.account where ((account_number > 200 and account_number < 300) or gender like 'm') and (state like 'hi' or address like 'avenue')", TEST_INDEX),
        hitCount(127)
			);
    }

    @Test
    public void multipartWhere3() {
			assertThat(
        query("SELECT * FROM %s.account where ((account_number > 25 and account_number < 75) and age >35 ) and (state like 'md' or (address like 'avenue' or address like 'street'))", TEST_INDEX),
        hitCount(7)
			);
    }

	private SearchHits query(String query, Object... args) {
		try {
			String q = String.format(query, args);
			SearchDao searchDao = MainTestSuite.getSearchDao();
			SearchRequestBuilder select = (SearchRequestBuilder) searchDao.explain(q);
			return select.get().getHits();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
