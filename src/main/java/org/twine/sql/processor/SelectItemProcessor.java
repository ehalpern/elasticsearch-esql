package org.twine.sql.processor;


import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import org.twine.sql.builder.SelectItemBuilder;

import java.util.Optional;

public class SelectItemProcessor implements SelectItemVisitor
{
  private final SelectItemBuilder builder;

  /*package*/ SelectItemProcessor(SelectItemBuilder builder) {
    this.builder = builder;
  }

  // *
  public void visit(AllColumns columns) {
    builder.star();
  }

  // Table.*
  public void visit(AllTableColumns columns) {
    builder.star();
  }

  public void visit(SelectExpressionItem item) {
    Expression ex = item.getExpression();
    ex.accept(new SelectItemExpressionProcessor(
      builder,
      item.getAlias()
    ));
  }
}
