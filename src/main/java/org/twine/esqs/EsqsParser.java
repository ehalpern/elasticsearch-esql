package org.twine.esqs;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.*;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * Created by eric on 4/18/15.
 */
@BuildParseTree
public class EsqsParser extends BaseParser<Object>
{
  private static EsqsParser INSTANCE = null;

  public static ParsingResult parse(String esqs) {
    // race condition is harmless here
    if (INSTANCE == null) {
      INSTANCE = Parboiled.createParser(EsqsParser.class);
    }
    ParseRunner runner = new BasicParseRunner(INSTANCE.Expression());
    return runner.run(esqs);
  }

  public Rule Expression() {
    return Sequence(
      ExpressionFactor(),
      ZeroOrMore(Sequence(
        FirstOf(And(), Or()),
        ExpressionFactor()
      ))
    );
  }

  public Rule ExpressionFactor() {
    return Sequence(
      Optional(Not()),
      FirstOf(SingleExpression(), ExpressionGroup())
    );
  }

  public Rule ExpressionGroup() {
    return Sequence(
      LPAREN,
      Expression(),
      RPAREN
    );
  }

  public Rule SingleExpression() {
    return Sequence(Field(), COLON, TermExpression());
  }

  public Rule Field() {
    return OneOrMore(FirstOf(FieldCharacter(), Escaped()));
  }

  //-------------------------------------------------------------------------
  //  Term Expression (RHS)
  //-------------------------------------------------------------------------
  public Rule TermExpression() {
    return Sequence(
      TermFactor(),
      ZeroOrMore(
        Sequence(
          Optional(FirstOf(And(), Or())),
          TermFactor()
        )
      )
    );
  }

  public Rule TermFactor() {
    return Sequence(
      Optional(Comparator()),
      FirstOf(Term(), TermGroup())
    );
  }

  public Rule TermGroup() {
    return Sequence(
      LPAREN,
      TermExpression(),
      RPAREN
    );
  }

  public Rule Term() {
    return FirstOf(
      Phrase(),
      Range(),
      WildcardTerm(),
      Sequence(Optional(PlusOrMinus()), SimpleTerm())
    );
  }

  public Rule Range() {
    return Sequence(
      RangeStart(), SimpleTerm(), TO, SimpleTerm(), RangeEnd()
    );
  }

  public Rule RangeStart() { return FirstOf(LBRACE, LBRACKET); }
  public Rule RangeEnd()   { return FirstOf(RBRACE, RBRACKET); }

  public Rule WildcardTerm() {
    return Sequence(OneOrMore(FirstOf(TermCharacter(), Wildcard(), Escaped())), Spacing());
  }

  public Rule Phrase() {
    return Sequence(
      QUOTE,
      OneOrMore(SimpleTerm()),
      QUOTE
    );
  }

  public Rule SimpleTerm() {
    return Sequence(OneOrMore(FirstOf(TermCharacter(), Escaped())), Spacing());
  }

  public Rule Comparator()  { return FirstOf(Not(), LT, LTE, GT, GTE); }
  public Rule Not()         { return FirstOf(NOT, BANG); }
  public Rule And()         { return FirstOf(AND, AMPAMP); }
  public Rule Or()          { return FirstOf(OR, PIPEPIPE); }
  public Rule PlusOrMinus() { return FirstOf(PLUS, MINUS); }

  public Rule Number()      { return Sequence(OneOrMore(CharRange('0', '9')), Spacing()); }

  //-------------------------------------------------------------------------
  //  Characters
  //-------------------------------------------------------------------------
  @SuppressNode
  public Rule TermCharacter() {
    return FirstOf(
      CharRange('A', 'z'),
      Digit(),
      Ch('_')
    );
  }

  @SuppressNode
  public Rule FieldCharacter() {
    return FirstOf(
      CharRange('A', 'z'),
      Digit(),
      Ch('_')
    );
  }

  @SuppressNode
  public Rule Wildcard()         { return FirstOf('*', '?'); }

  @SuppressNode
  public Rule Digit()            { return CharRange('0', '9'); }

  @SuppressNode
  public Rule Escaped()          { return Sequence("\\", Special()); }

  @SuppressNode
  public Rule Special() {
    return FirstOf(
      ':', '!', '>', '<', '&', '|', '-', '+',
      '{', '}', '[', ']', '(', ')',
      '/', '~', '?', '*', '^', '\\', '"'
    );
  }

  final Rule COLON = Terminal(":");

  final Rule GT = Terminal(">", AnyOf("=>"));
  final Rule GTE = Terminal(">=");
  final Rule LT = Terminal("<", AnyOf("=<"));
  final Rule LTE = Terminal("<=");

  final Rule TO = Terminal("TO");

  final Rule NOT = Terminal("NOT");
  final Rule BANG = Terminal("!");
  final Rule AND = Terminal("AND");
  final Rule AMPAMP = Terminal("&&");
  final Rule OR =  Terminal("OR");
  final Rule PIPEPIPE = Terminal("||");
  final Rule SPACE = Terminal(" ");
  final Rule MINUS = Terminal("-");
  final Rule PLUS = Terminal("+");

  final Rule LBRACE = Terminal("{");
  final Rule RBRACE = Terminal("}");
  final Rule LBRACKET = Terminal("[");
  final Rule RBRACKET = Terminal("]");
  final Rule LPAREN = Terminal("(");
  final Rule RPAREN = Terminal(")");

  final Rule SLASH = Terminal("/");
  final Rule TILDE = Terminal("~");
  final Rule QUESTION = Terminal("?");
  final Rule STAR = Terminal("*");
  final Rule CARET = Terminal("^");
  final Rule BSLASH = Terminal("\\");
  final Rule QUOTE = Terminal("\"");



  @SuppressNode
  @DontLabel
  Rule Terminal(String string) {
    return Sequence(string, Spacing()).label('\'' + string + '\'');
  }

  @SuppressNode
  @DontLabel
  Rule Terminal(String string, Rule mustNotFollow) {
    return Sequence(string, TestNot(mustNotFollow), Spacing()).label('\'' + string + '\'');
  }

  public Rule Spacing() {
    return ZeroOrMore(AnyOf(" \t\r\n\f"));
  }


  // we redefine the public Rule creation for string literals to automatically match trailing whitespace if the string
  // literal ends with a space character, this way we don't have to insert extra whitespace() rules after each
  // character or string literal

  /*
  @Override
  protected Rule fromStringLiteral(String string) {
    return string.endsWith(" ") ?
      Sequence(String(string.substring(0, string.length() - 1)), Spacing()) :
      String(string);
  }
  */
}