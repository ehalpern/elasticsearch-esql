package org.twine.sql.processor;


import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import org.twine.sql.builder.WhereBuilder;

import java.util.ArrayList;
import java.util.List;

public class WhereExpressionProcessor extends ExpressionVisitorAdapter
{
  private final WhereBuilder builder;

  /*package*/ WhereExpressionProcessor(WhereBuilder builder) {
    this.builder = builder;
  }

  @Override
  public void visit(Function function) {
    List<String> params = new ArrayList<>();
    for (Expression p : function.getParameters().getExpressions()) {
      params.add(p.toString());
    }
    builder.function(function.getName(), params);
  }

  @Override
  public void visit(Column column) {
  }


  @Override
  public void visit(NullValue value) {

  }

  @Override
  public void visit(DoubleValue value) {

  }

  @Override
  public void visit(LongValue value) {

  }

  @Override
  public void visit(DateValue value) {

  }

  @Override
  public void visit(TimeValue value) {

  }

  @Override
  public void visit(TimestampValue value) {

  }

  @Override
  public void visit(StringValue value) {

  }

  @Override
  public void visit(IntervalExpression expr) {

  }

}
