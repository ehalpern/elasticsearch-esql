package org.twine.elasticsearch.esql;

import org.durid.sql.ast.expr.SQLCharExpr;
import org.durid.sql.ast.expr.SQLMethodInvokeExpr;
import org.nlpcn.es4sql.domain.*;
import org.twine.sql.builder.ColumnReferenceBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric on 4/8/15.
 */
public class EsqlCommand
{
  private final Select command = new Select();
  private final StatementBuilder statementBuilder = new StatementBuilder();

  public StatementBuilder statementBuilder() {
    return statementBuilder;
  }

  public Select getSelect() {
    return command;
  }

  class StatementBuilder implements org.twine.sql.builder.StatementBuilder {
    public SelectBuilder select() {
      return new SelectBuilder();
    }
  }

  class SelectBuilder implements org.twine.sql.builder.SelectBuilder
  {
    public SelectItemBuilder selectItem() {
      return new SelectItemBuilder();
    }

    public FromItemBuilder from() {
      return new FromItemBuilder();
    }

    public WhereBuilder where() {
      return new WhereBuilder();
    }

    public ColumnReferenceBuilder groupBy() {
      return new GroupByBuilder();
    }

    public SelectBuilder orderBy(String name, boolean ascending) {
      command.addOrderBy(name, ascending ? "ASC" : "DESC");
      return this;
    }

    public SelectBuilder limit(long offset, long limit) {
      command.setRowCount((int)limit);
      command.setOffset((int)offset);
      return this;
    }
  }

  class SelectItemBuilder implements org.twine.sql.builder.SelectItemBuilder {
    public SelectItemBuilder star() {
      // Don't add anything; next phase assumes an absense of fields implies *
      //command.addField(new Field("*", null));
      return this;
    }

    public SelectItemBuilder field(String name, String alias) {
      command.addField(new Field(name, alias));
      return this;
    }

    public SelectItemBuilder function(String function, List<String> params, String alias) {
      List<KVValue> params2 = new ArrayList<>();
      for (String p: params) {
        params2.add(new KVValue(p));
      }
      command.addField(new MethodField(function, params2, null, alias));
      return this;
    }
  }

  class FromItemBuilder implements org.twine.sql.builder.FromItemBuilder {
    public FromItemBuilder from(String index, String type, String alias) {
      command.getFrom().add(new From(index, type));
      return this;
    }
  }

  class WhereBuilder implements org.twine.sql.builder.WhereBuilder {
    public WhereBuilder function(String function, List<String> params) {
      SQLMethodInvokeExpr value = new SQLMethodInvokeExpr(function);
      List<SQLCharExpr> params2 = new ArrayList<>();
      for (String p : params) {
        if (p.startsWith("'") && p.endsWith("'")) {
          p = p.substring(1, p.length() - 1); // remove single quotes added to avoid parsing
        }
        params2.add(new SQLCharExpr(p));
      }
      value.getParameters().addAll(params2);
      Where where = new Condition(
        Condition.CONN.AND, "q", Condition.OPEAR.EQ, value
      );
      command.setWhere(where);
      return this;
    }
  }

  class GroupByBuilder implements ColumnReferenceBuilder {
    public ColumnReferenceBuilder field(String name) {
      command.addGroupBy(new Field(name, null));
      return this;
    }

    public ColumnReferenceBuilder function(String function, String field) {
      throw new UnsupportedOperationException("GROUP BY clause " +
        "must refer to a field");
    }
  }
}
