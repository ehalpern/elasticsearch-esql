package org.twine.sql.processor;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.twine.esql.EsqlUnsupportedFeatureException;

public abstract class FromProcessor implements FromItemVisitor
{
  public abstract void visit(Table table);

  public void visit(SubSelect subSelect) {
    throw new EsqlUnsupportedFeatureException("Sub-select");
  }

  public void visit(SubJoin subjoin) {
    throw new EsqlUnsupportedFeatureException("Sub-join");
  }

  public void visit(LateralSubSelect lateralSubSelect) {
    throw new EsqlUnsupportedFeatureException("Lateral sub-select");
  }

  public void visit(ValuesList valuesList) {
    throw new EsqlUnsupportedFeatureException("VALUES in FROM");
  }
}
