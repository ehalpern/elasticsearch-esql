package org.twine.sql.processor;


import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

public class FromProcessor implements FromItemVisitor
{
  public void visit(Table table) {
    throw new UnsupportedOperationException();
  }

  public void visit(SubSelect subSelect) {
    throw new UnsupportedOperationException("Sub select not supported");
  }

  public void visit(SubJoin subjoin) {
    throw new UnsupportedOperationException("Sub join not supported");
  }

  public void visit(LateralSubSelect lateralSubSelect) {

    throw new UnsupportedOperationException("Sub select not supported");
  }

  public void visit(ValuesList valuesList) {
    throw new UnsupportedOperationException("VALUES not supported in FROM");
  }
}
