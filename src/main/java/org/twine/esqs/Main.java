package org.twine.esqs;


import org.parboiled.Parboiled;
import org.parboiled.common.StringUtils;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.RecoveringParseRunner;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;

import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    EsqsParser parser = Parboiled.createParser(EsqsParser.class);

    while (true) {
      System.out.print("Enter an esqs expression (single RETURN to exit)!\n");
      String input = new Scanner(System.in).nextLine();
      if (StringUtils.isEmpty(input)) break;

      ParsingResult<?> result = new ReportingParseRunner(parser.Expression()).run(input);

      if (!result.parseErrors.isEmpty())
        System.out.println(ErrorUtils.printParseError(result.parseErrors.get(0)));
      else
        System.out.println(printNodeTree(result) + '\n');
    }
  }
}