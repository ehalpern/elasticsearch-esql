package org.nlpcn.es4sql;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Map;

import static org.hamcrest.Matchers.is;

/**
 * Created by eric on 4/16/15.
 */
public class ElasticSearchMatchers
{
  public static class EachHit extends TypeSafeMatcher<SearchHits> {
    private final Matcher<Map<String, Object>> matcher;

    public EachHit(Matcher<Map<String, Object>> matcher) {
      this.matcher = matcher;
    }

    @Override
    public boolean matchesSafely(SearchHits actual) {
      for (SearchHit hit : actual) {
        if (!matcher.matches(hit.getSource())) {
          return false;
        }
      }
      return true;
    }

    @Override
    public void describeMismatchSafely(SearchHits actual, Description mismatchDescription) {
      if (actual.getHits().length == 0) {
        mismatchDescription.appendText(" search result was empty");
      } else {
        for (SearchHit hit: actual) {
          if (!matcher.matches(hit.getSource())) {
            mismatchDescription.appendText(" first mismatch was ").appendValue(hit.getSource());
            return;
          }
        }
      }
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(" each hit matching ")
        .appendDescriptionOf(matcher);
    }

    @Factory
    public static <V> EachHit eachHit(Matcher<Map<? extends String, ? extends V>> matcher) {
      return new EachHit((Matcher<Map<String, Object>>)(Matcher)matcher);
    }
  }

  public static class SomeHit extends TypeSafeMatcher<SearchHits> {
    private final Matcher<Map<String, Object>> matcher;

    public SomeHit(Matcher<Map<String, Object>> matcher) {
      this.matcher = matcher;
    }

    @Override
    public boolean matchesSafely(SearchHits actual) {
      for (SearchHit hit : actual) {
        if (matcher.matches(hit.getSource())) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void describeMismatchSafely(SearchHits actual, Description mismatchDescription) {
      if (actual.getHits().length == 0) {
        mismatchDescription.appendText("search result was empty");
      } else {
        for (SearchHit hit: actual) {
          if (!matcher.matches(hit.getSource())) {
            mismatchDescription.appendText(" no hit matched ").appendValue(hit.getSource());
            return;
          }
        }
      }
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(" some hit matching ")
        .appendDescriptionOf(matcher);
    }

    @Factory
    public static <V> SomeHit someHit(Matcher<Map<? extends String, ? extends V>> matcher) {
      return new SomeHit((Matcher<Map<String, Object>>)(Matcher)matcher);
    }
  }



  public static class HitCount extends TypeSafeMatcher<SearchHits>
  {
    private Matcher<Integer> matcher;

    public HitCount(Matcher<Integer> matcher) {
      this.matcher = matcher;
    }

    protected boolean matchesSafely(SearchHits searchHits) {
      return matcher.matches(searchHits.getHits().length);
    }

    public void describeMismatchSafely(final SearchHits searchHits, final Description mismatchDescription) {
      mismatchDescription.appendText(" was ").appendValue(searchHits.getHits().length);
    }

    public void describeTo(final Description description) {
      description.appendText(" hit count should match ").appendDescriptionOf(matcher);
    }

    @Factory
    public static <T> Matcher<SearchHits> hitCount(Matcher<Integer> matcher) {
      return new HitCount(matcher);
    }

    @Factory
    public static <T> Matcher<SearchHits> hitCount(Integer expected) {
      return new HitCount(is(expected));
    }
  }
}
