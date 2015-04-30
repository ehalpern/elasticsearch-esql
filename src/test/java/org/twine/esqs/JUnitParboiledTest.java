package org.twine.esqs;

import org.junit.Assert;
import org.parboiled.test.ParboiledTest;


public class JUnitParboiledTest<V> extends ParboiledTest<V>
{
  protected void assertEquals(Object actual, Object expected) {
    assertEquals(actual, expected);
  }

  protected void fail(String message) {
    Assert.fail(message);
  }
}