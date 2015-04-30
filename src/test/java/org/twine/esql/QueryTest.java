package org.twine.esql;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.hamcrest.CustomMatcher;
import org.junit.Ignore;
import org.junit.Test;
import org.nlpcn.es4sql.SearchDao;

import java.sql.SQLException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.twine.esql.ElasticSearchMatchers.HitCount.*;
import static org.twine.esql.ElasticSearchMatchers.EachHit.*;
import static org.twine.esql.ElasticSearchMatchers.SomeHit.*;
import static org.junit.Assert.*;


public class QueryTest
{
	@Test
	public void searchType() {
		assertThat(
			query("SELECT * FROM %s.phrase LIMIT 1000", TestsConstants.TEST_INDEX),
			hitCount(4)
		);
	}

	@Ignore @Test // not supported yet
	public void multipleFrom() {
		assertThat(
			query("SELECT * FROM %s.phrase, %s.account LIMIT 2000", TestsConstants.TEST_INDEX, TestsConstants.TEST_INDEX),
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
			query("SELECT age, account_number FROM %s.account", TestsConstants.TEST_INDEX),
		  eachHit(allOf(hasKey("age"), hasKey("account_number")))
		);
	}

	@Ignore @Test // Not implemented yet
	public void selectAliases() {
		assertThat(
			query("SELECT age AS myage, account_number AS myaccount_number FROM %s/account", TestsConstants.TEST_INDEX),
			eachHit(allOf(hasKey("myage"), hasKey("myaccount_number")))
		);
	}

	@Test
	public void equality() {
		assertThat(
			query("select * from %s.account where city = 'Nogal' LIMIT 1000", TestsConstants.TEST_INDEX),
			eachHit(hasEntry("city", "Nogal"))
		);
	}

	@Test
	public void equalityPhrase() {
		assertThat(
			query("SELECT * FROM %s.phrase WHERE phrase = 'quick fox here' LIMIT 1000", TestsConstants.TEST_INDEX),
			eachHit(hasEntry("phrase", "quick fox here"))
		);
	}

	@Test
	public void greaterThanTest() {
		assertThat(
			query("SELECT * FROM %s WHERE age > 25 LIMIT 1000", TestsConstants.TEST_INDEX),
			eachHit(hasEntry(is("age"), greaterThan(25)))
		);
	}

	@Test
	public void greaterThanOrEqual() {
		int age = 25;
		assertThat(
			query("SELECT * FROM %s WHERE age >= %s LIMIT 1000", TestsConstants.TEST_INDEX, age),
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
			query("SELECT * FROM %s WHERE age < %s LIMIT 1000", TestsConstants.TEST_INDEX, age),
			eachHit(hasEntry(is("age"), lessThan(age)))
		);
	}

	@Test
	public void lessThanOrEqual() {
		int age = 25;
		assertThat(
			query("SELECT * FROM %s WHERE age <= %s LIMIT 1000", TestsConstants.TEST_INDEX, age),
			allOf(
				eachHit(hasEntry(is("age"), lessThanOrEqualTo(age))),
				someHit(hasEntry("age", age))
			)
		);
	}

	@Test
	public void or() {
		assertThat(
			query("SELECT * FROM %s WHERE gender='F' OR gender='M' LIMIT 1000", TestsConstants.TEST_INDEX),
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
			query("SELECT * FROM %s WHERE age=32 AND gender='M' LIMIT 1000", TestsConstants.TEST_INDEX),
			allOf(
				eachHit(hasEntry("age", 32)),
				eachHit(hasEntry("gender", "M"))
			)
		);
	}

	@Test
	public void like() {
		assertThat(
			query("SELECT * FROM %s WHERE firstname LIKE 'amb%%' LIMIT 1000", TestsConstants.TEST_INDEX),
			eachHit(hasEntry("firstname", "Amber"))
		);
	}

	@Test
	public void notLike() {                                                                                  
		assertThat(
			query("SELECT * FROM %s WHERE firstname NOT LIKE 'amb%%' LIMIT 1000", TestsConstants.TEST_INDEX),
			allOf(
				hitCount(greaterThan(1)),
				eachHit(not(hasEntry("firstname", "Amber")))
			)
		);
	}

	@Test
	public void limit() {
		assertThat(
			query("SELECT * FROM %s LIMIT 30", TestsConstants.TEST_INDEX),
			hitCount(30)
		);
	}

	@Test
	public void between() {
		int min = 27;
		int max = 30;
		assertThat(
			query("SELECT * FROM %s WHERE age BETWEEN %s AND %s LIMIT 1000", TestsConstants.TEST_INDEX, min, max),
			eachHit(
				hasEntry(
					is("age"),
					allOf(greaterThanOrEqualTo(min), lessThanOrEqualTo(max))
				)
			)
		);
	}


	/**
	 * @note The current null behavior is not ANSI Nulls but rather null = null.  This means
	 *       age NOT BETWEEN will return rows for which age = null.  With ANSI null behavior,
	 *       null is considered unknown (meaning null != null).  An ANSI compliant query would
	 *       not return rows with age = null because this value means we don't know age's value
	 *       and hence cannot say that it's not between 20 and 37.
	 */
	@Test
	public void notBetween() {
		int min = 20;
		int max = 37;
		assertThat(
			query("SELECT * FROM %s WHERE age NOT BETWEEN %s AND %s LIMIT 1000", TestsConstants.TEST_INDEX, min, max),
			eachHit(hasEntry(is("age"), allOf(lessThan(min), greaterThan(max))))
		);
	}

	@Test
	public void in(){
		assertThat(
			query("SELECT age FROM %s.phrase WHERE age IN (20, 22) LIMIT 1000", TestsConstants.TEST_INDEX),
			eachHit(hasEntry(is("age"), isOneOf(20, 22)))
		);
	}

	@Test
	public void inTestWithStrings() {
		assertThat(
			query("SELECT phrase FROM %s.phrase WHERE phrase IN ('quick fox here', 'fox brown') LIMIT 1000", TestsConstants.TEST_INDEX),
			eachHit(hasEntry(is("phrase"), isOneOf("quick fox here", "fox brown")))
		);
	}

	/**
	 * @see #notBetween
	*/
	@Test
	public void notIn() {
		assertThat(
			query("SELECT age FROM %s WHERE age NOT IN (20, 22) LIMIT 1000", TestsConstants.TEST_INDEX),
			eachHit(not(hasEntry(is("age"), isOneOf(20, 22))))
		);
	}

	
	@Ignore @Test // Elasticsearch has a bug parsing <, > range queries with dates
	              // containing times.
	public void dateSearch()
	{
		final DateTimeFormatter format = DateTimeFormat.forPattern(TestsConstants.DATE_FORMAT);
		final DateTime endDate = format.parseDateTime("2014-08-18:00:00:00.000Z");
		assertThat(
			query("SELECT insert_time FROM %s.online WHERE insert_time < '%s'", TestsConstants.TEST_INDEX, endDate),
			eachHit(hasEntry(is("insert_time"), is(
				new CustomMatcher<String>("check data") {
					public boolean matches(Object item) {
						DateTime actual = format.parseDateTime((String)item);
						return actual.isBefore(endDate);
					}
				}
			)))
		);
	}

	@Test
	public void dateBetweenSearch()
	{
		final DateTimeFormatter format = DateTimeFormat.forPattern(TestsConstants.DATE_FORMAT);
		final DateTime dateLimit1 = new DateTime(2014, 8, 18, 0, 0, 0);
		final DateTime dateLimit2 = new DateTime(2014, 8, 21, 0, 0, 0);

		assertThat(
			query("SELECT insert_time FROM %s.online WHERE insert_time " +
				"BETWEEN '2014-08-18' AND '2014-08-21' LIMIT 3", TestsConstants.TEST_INDEX),
			eachHit(hasEntry(is("insert_time"), is(
				new CustomMatcher<String>("check data") {
					public boolean matches(Object item) {
						DateTime actual = format.parseDateTime((String) item);
						return (actual.isAfter(dateLimit1) || actual.isEqual(dateLimit1)) &&
									 (actual.isBefore(dateLimit2) || actual.isEqual(dateLimit2));
					}
				}
			)))
		);
	}

	@Test
	public void isNullSearch() {
		assertThat(
			query("SELECT * FROM %s.phrase WHERE insert_time IS NULL", TestsConstants.TEST_INDEX),
			eachHit(not(hasKey("insertTime")))
		);
	}

	@Test
	public void isNotNullSearch() {
		assertThat(
			query("SELECT * FROM %s.phrase WHERE insert_time IS NOT NULL", TestsConstants.TEST_INDEX),
			eachHit(hasKey("insert_time"))
		);
	}
	

	@Test
	public void complexConditionQuery(){
		assertThat(
			query(
				"SELECT * FROM %s.account" +
				"  WHERE (gender='m' AND (age> 25 OR account_number>5))" +
				"    OR (gender='f' AND (age>30 OR account_number < 8))",
				TestsConstants.TEST_INDEX
			),
			eachHit(
				new CustomMatcher<Map<? extends String, ? extends Object>>("match complex condition") {
					public boolean matches(Object entries) {
						Map<String, Object> map = (Map)entries;
						String gender =  (String)map.get("gender");
						int    age =     (Integer)map.get("age");
						int    account = (Integer)map.get("account_number");
						return (gender.equals("M") && (age > 25 || account > 5))
								|| (gender.equals("F") && (age > 30 || account < 8));
					}
				}
			)
		);
	}


	@Test
	public void orderByAsc() {
		SearchHits response = query("SELECT age FROM %s.account ORDER BY age ASC LIMIT 1000", TestsConstants.TEST_INDEX);
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
		SearchHits response = query("SELECT age FROM %s.account ORDER BY age DESC LIMIT 1000", TestsConstants.TEST_INDEX);
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
        query("SELECT * FROM %s.account WHERE (firstname LIKE 'opal' OR firstname like 'rodriquez') AND (state like 'oh' OR state like 'hi')", TestsConstants.TEST_INDEX),
				hitCount(2)
			);
    }

    @Test // 11 results is wrong
    public void multipartWhere2() {
			assertThat(
				query("SELECT * FROM %s.account where ((account_number > 200 and account_number < 300) or gender like 'm') and (state like 'hi' or address like 'avenue')", TestsConstants.TEST_INDEX),
				hitCount(127)
			);
    }

    @Test
    public void multipartWhere3() {
			assertThat(
        query("SELECT * FROM %s.account where ((account_number > 25 and account_number < 75) and age >35 ) and (state like 'md' or (address like 'avenue' or address like 'street'))", TestsConstants.TEST_INDEX),
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
