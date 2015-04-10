package org.twine.sql.processor;


import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.twine.sql.builder.FromItemBuilder;

public class FromItemProcessor implements FromItemVisitor
{
  private final FromItemBuilder builder;

  /*package*/ FromItemProcessor(FromItemBuilder builder) {
    this.builder = builder;
  }

  public void visit(Table table) {
    String alias = table.getAlias() != null ? table.getAlias().getName() : null;
    builder.from(table.getSchemaName(), table.getName(), alias);
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
