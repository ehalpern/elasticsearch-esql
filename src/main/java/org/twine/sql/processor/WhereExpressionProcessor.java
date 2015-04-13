package org.twine.sql.processor;


import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

public class WhereExpressionProcessor extends ExpressionVisitorAdapter
{
  public void visit(Function function) {
  }

  public void visit(SignedExpression signedExpression) {
  }

  public void visit(Column column) {

  }

  public void visit(ExistsExpression existsExpression) {
  }


  public void visit(DoubleValue value) {

  }

  public void visit(LongValue value) {

  }

  public void visit(DateValue value) {

  }

  public void visit(TimeValue value) {
  }

  public void visit(TimestampValue value) {

  }

  public void visit(Parenthesis parenthesis) {
  }

  public void visit(StringValue value) {
  }

  public void visit(AndExpression andExpression) {
  }

  @Override
  public void visit(OrExpression orExpression) {
  }

  public void visit(Between between) {
  }

  @Override
  public void visit(EqualsTo equalsTo) {
  }

  @Override
  public void visit(GreaterThan greaterThan) {
  }

  @Override
  public void visit(GreaterThanEquals greaterThanEquals) {
  }

  @Override
  public void visit(InExpression inExpression) {
  }

  public void visit(IsNullExpression isNullExpression) {}

  public void visit(LikeExpression likeExpression) {
  }

  @Override
  public void visit(NotEqualsTo notEqualsTo) {
  }

  public void visit(Matches matches) {}
  public void visit(NullValue value) {}
  public void visit(AnalyticExpression aexpr) {}
  public void visit(Addition addition) {}
  public void visit(Division division) {}
  public void visit(Multiplication multiplication) {}
  public void visit(Subtraction subtraction) {}
  public void visit(IntervalExpression expr) {}
  public void visit(RegExpMatchOperator rexpr) {}
  public void visit(Concat concat) {}
  public void visit(JdbcParameter jdbcParameter) {}
  public void visit(JdbcNamedParameter jdbcNamedParameter) {}
  public void visit(SubSelect subSelect) {}
  public void visit(CaseExpression caseExpression) {}
  public void visit(WhenClause whenClause) {}
  public void visit(AllComparisonExpression allComparisonExpression) {}
  public void visit(AnyComparisonExpression anyComparisonExpression) {}
  public void visit(BitwiseAnd bitwiseAnd) {}
  public void visit(BitwiseOr bitwiseOr) {}
  public void visit(BitwiseXor bitwiseXor) {}
  public void visit(CastExpression cast) {}
  public void visit(Modulo modulo) {}
  public void visit(ExtractExpression eexpr) {}
  public void visit(MinorThan minorThan) {}
  public void visit(MinorThanEquals minorThanEquals) { }
  public void visit(OracleHierarchicalExpression oexpr) {}
}
