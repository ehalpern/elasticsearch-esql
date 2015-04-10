package org.twine.sql.processor;


import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import org.twine.sql.builder.ColumnReferenceBuilder;

public class ColumnReferenceProcessor extends ExpressionVisitorAdapter
{
  private final ColumnReferenceBuilder builder;

  /*package*/ ColumnReferenceProcessor(ColumnReferenceBuilder builder) {
    this.builder = builder;
  }

  @Override
  public void visit(Function function) {
    builder.function(function.getName(), function.getParameters().toString());
  }

  @Override
  public void visit(Column column) {
    builder.field(column.getFullyQualifiedName());
  }
}
