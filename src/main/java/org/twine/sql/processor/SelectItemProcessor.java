package org.twine.sql.processor;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

public class SelectItemProcessor implements SelectItemVisitor
{
  protected ExpressionVisitor selectItemExpression(Alias alias) {
    return new SelectItemExpressionProcessor(alias);
  }

  // *
  public void visit(AllColumns columns) {
    throw new UnsupportedOperationException();
  }

  // Table.*
  public void visit(AllTableColumns columns) {
    throw new UnsupportedOperationException();
  }

  public void visit(SelectExpressionItem item) {
    Expression ex = item.getExpression();
    ex.accept(selectItemExpression(item.getAlias()));
  }
}
