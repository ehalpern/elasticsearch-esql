package org.twine.sql.processor;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;

public abstract class GroupByProcessor extends ExpressionVisitorAdapter
{
  public abstract void visit(Function function);
}

