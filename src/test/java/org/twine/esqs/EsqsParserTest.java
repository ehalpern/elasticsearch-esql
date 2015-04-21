package org.twine.esqs;
import org.junit.Test;

import org.parboiled.Parboiled;
import org.parboiled.support.ParsingResult;

import static org.junit.Assert.*;

public class EsqsParserTest extends JUnitParboiledTest<Object>
{
  @Test
  public void goodQueries() {
    testParse("name:fred");
    testParse("name:\"fred flinstone\"");
    testParse("name:(fred flinstone)");
    testParse("name:(fred AND flinstone)");
    testParse("name:!(fred AND flinstone)");
    testParse("name:(+fred -wilma +flinstone)");
    testParse("name:fred flinstone");
    testParse("age:<40");
    testParse("age:<=40");
    testParse("age:>20");
    testParse("age:>=20");
  }

  @Test
  public void badQueries() {
    ParsingResult r = testParseResult("name:((fred OR wilma) AND flinstone");
    assertTrue(r.hasErrors());
  }

  private void testParse(String esql) {
    EsqsParser parser = Parboiled.createParser(EsqsParser.class);
    test(parser.Expression(), esql)
      .hasNoErrors();
  }

  private ParsingResult testParseResult(String esql) {
    EsqsParser parser = Parboiled.createParser(EsqsParser.class);
    return test(parser.Expression(), esql).result;
  }

}