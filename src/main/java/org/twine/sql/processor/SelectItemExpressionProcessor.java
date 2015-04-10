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
  private final SelectItemBuilder builder;
  private final String alias;

  /*package*/ SelectItemExpressionProcessor(SelectItemBuilder builder, Alias alias) {
    this.builder = builder;
    this.alias = (alias != null) ? alias.getName() : null;
  }

  @Override
  public void visit(Function function) {
    List<String> params = new ArrayList<>();
    for (Expression e: function.getParameters().getExpressions()) {
      params.add(e.toString());
    }
    builder.function(function.getName(), params, alias);
  }

  @Override
  public void visit(Column column) {
    // for later
    String tableName = column.getTable().getName();
    Alias tableAlias = column.getTable().getAlias();
    builder.field(column.getFullyQualifiedName(), alias);
  }
}
