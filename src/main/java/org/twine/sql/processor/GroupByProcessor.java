package org.twine.sql.processor;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;

public class GroupByProcessor extends ExpressionVisitorAdapter
{
  @Override
  public void visit(Function function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(Column column) {
    throw new UnsupportedOperationException();
  }
}
