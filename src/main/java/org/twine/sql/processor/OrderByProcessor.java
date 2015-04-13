package org.twine.sql.processor;


import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import org.twine.sql.builder.SelectBuilder;

public class OrderByProcessor implements OrderByVisitor
{
  public void visit(OrderByElement orderBy) {
    throw new UnsupportedOperationException();
  }
}
