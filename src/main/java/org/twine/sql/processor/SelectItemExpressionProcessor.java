package org.twine.sql.processor;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import org.twine.sql.builder.SelectItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class SelectItemExpressionProcessor extends ExpressionVisitorAdapter
{
  protected final String alias;

  protected SelectItemExpressionProcessor(Alias alias) {
    this.alias = (alias != null) ? alias.getName() : null;
  }

  @Override
  public void visit(Function function) {
    List<String> params = new ArrayList<>();
    for (Expression e: function.getParameters().getExpressions()) {
      params.add(e.toString());
    }
  }

  @Override
  public void visit(Column column) {
  }
}
