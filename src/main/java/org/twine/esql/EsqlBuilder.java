package org.twine.esql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.parser.TokenMgrError;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.*;
import org.nlpcn.es4sql.domain.Field;
import org.nlpcn.es4sql.domain.From;
import org.nlpcn.es4sql.domain.KVValue;
import org.nlpcn.es4sql.domain.MethodField;
import org.twine.sql.processor.*;
import static org.twine.esql.EsqlUtil.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric on 4/8/15.
 */
public class EsqlBuilder
{
  private final EsqlCommand command = new EsqlCommand();

  public static EsqlCommand parse(String esql) {
    String normalized = normalizeEsql(esql);
    try {
      Statement statement = CCJSqlParserUtil.parse(normalized);
      EsqlBuilder builder = new EsqlBuilder(statement);
      return builder.complete();
    } catch (JSQLParserException|TokenMgrError e) {
      throw new EsqlSyntaxException(e);
    }
  }

  private EsqlCommand complete() {
    return command.complete();
  }

  private EsqlBuilder(Statement statement) {
    statement.accept(statement());
  }

  private StatementVisitor statement() {
    return new StatementBuilder();
  }

  class StatementBuilder extends StatementProcessor
  {
    protected SelectVisitor select() {
      return new SelectStatementBuilder();
    }
  }

  class SelectStatementBuilder extends SelectStatementProcessor
  {
    protected SelectItemVisitor selectItem() { return new SelectItemBuilder(); }
    protected FromItemVisitor   from()       { return new FromBuilder(); }
    protected ExpressionVisitor where()      { return new WhereBuilder(); }
    protected ExpressionVisitor groupBy()    { return new GroupByBuilder(); }
    protected OrderByVisitor    orderBy()    { return new OrderByBuilder(); }


    protected void visit(Limit limit) {
      command.setRowCount((int)limit.getRowCount());
      command.setOffset((int)limit.getOffset());
    }
  }

  class SelectItemBuilder extends SelectItemProcessor
  {
    protected ExpressionVisitor selectItemExpression(Alias alias) {
      return new SelectItemExpressionBuilder(alias);
    }

    // *
    public void visit(AllColumns columns) {
      // No need to do anything since absence of field implies *.
      // @todo: how to handle other explicit columns?
    }
  }

  class SelectItemExpressionBuilder extends SelectItemExpressionProcessor
  {
    /*package*/ SelectItemExpressionBuilder(Alias alias) {
      super(alias);
    }

    @Override
    public void visit(Function function) {
      List<KVValue> params = new ArrayList<>();
      if (function.getParameters() != null) {
        for (Expression e : function.getParameters().getExpressions()) {
          params.add(new KVValue(e.toString()));
        }
      } else {
        params.add(new KVValue("*"));
      }
      String option = function.isDistinct() ? "DISTNCT" : null;
      command.addField(new MethodField(function.getName(), params, option, alias));
    }

    @Override
    public void visit(Column column) {
      command.addField(new Field(column.getFullyQualifiedName(), alias));
    }
  }

  class FromBuilder extends FromProcessor
  {
    public void visit(Table table) {
      String alias = table.getAlias() != null ? table.getAlias().getName() : null;
      String index;
      String type = null;
      if (table.getSchemaName() == null) {
        index = table.getName();
      } else {
        index = table.getSchemaName();
        type = table.getName();
      }
      command.getFrom().add(new From(index, type));
    }
  }

  class WhereBuilder extends WhereExpressionProcessor
  {
    private void addToQuery(String s) {
      command.appendToQuery(s);
      System.out.print(s);
      System.out.flush();
    }

    protected ExpressionVisitor subExpression() {
      return this;
    }

    public void visit(Function function) {
      if (function.getName().equals("query")) {
        List<Expression> params = function.getParameters().getExpressions();
        if (params.size() != 1) {
          throw new EsqlSyntaxException("query() function expects exactly 1 parameter");
        } else {
          addToQuery(unwrapSingleQuotes(params.get(0).toString()));
        }
      }
    }

    public void visit(SignedExpression signedExpression) {
      addToQuery("-");
      signedExpression.accept(subExpression());
    }

    public void visit(Column column) {
      addToQuery(column.toString());
    }

    public void visit(ExistsExpression existsExpression) {
      if (existsExpression.isNot()) {
        addToQuery("_missing_:");
      } else {
        addToQuery("_exists_:");
      }
      existsExpression.accept(subExpression());
    }

    public void visit(Parenthesis parenthesis) {
      addToQuery("(");
      parenthesis.getExpression().accept(subExpression());
      addToQuery(")");
    }

    public void visit(AndExpression andExpression) {
      andExpression.getLeftExpression().accept(subExpression());
      addToQuery(" && ");
      andExpression.getRightExpression().accept(subExpression());
    }

    public void visit(OrExpression orExpression) {
      orExpression.getLeftExpression().accept(subExpression());
      addToQuery(" || ");
      orExpression.getRightExpression().accept(subExpression());
    }

    public void visit(Between between) {
      between.getLeftExpression().accept(subExpression());
      addToQuery(":(");
      if (between.isNot()) {
        addToQuery("<");
      } else {
        addToQuery(">=");
      }
      between.getBetweenExpressionStart().accept(subExpression());
      if (between.isNot()) {
        addToQuery(" AND >");
      } else {
        addToQuery(" AND <=");
      }
      between.getBetweenExpressionEnd().accept(subExpression());
      addToQuery(")");
    }

    public void visit(EqualsTo equalsTo) {
      // skip special construct used for queryString
      boolean skipEquals = equalsTo.getLeftExpression().toString().equals("_q");
      if (!skipEquals) {
        equalsTo.getLeftExpression().accept(subExpression());
        addToQuery(":");
      }
      equalsTo.getRightExpression().accept(subExpression());
    }

    public void visit(GreaterThan greaterThan) {
      greaterThan.getLeftExpression().accept(subExpression());
      addToQuery(":>");
      greaterThan.getRightExpression().accept(subExpression());
    }

    public void visit(GreaterThanEquals greaterThanEquals) {
      greaterThanEquals.getLeftExpression().accept(subExpression());
      addToQuery(":>=");
      greaterThanEquals.getRightExpression().accept(subExpression());
    }

    public void visit(MinorThan lessThan) {
      lessThan.getLeftExpression().accept(subExpression());
      addToQuery(":<");
      lessThan.getRightExpression().accept(subExpression());
    }

    public void visit(MinorThanEquals lessThanEquals) {
      lessThanEquals.getLeftExpression().accept(subExpression());
      addToQuery(":<=");
      lessThanEquals.getRightExpression().accept(subExpression());
    }

    public void visit(final InExpression inExpression) {
      inExpression.getLeftExpression().accept(subExpression());
      addToQuery(":(");
      inExpression.getRightItemsList().accept(new ItemsListVisitorAdapter()
      {
        @Override
        public void visit(ExpressionList expressionList) {
          for (Expression e : expressionList.getExpressions()) {
            if (inExpression.isNot()) {
              addToQuery(" -");
            } else {
              addToQuery(" ");
            }
            e.accept(subExpression());
          }
        }
      });
      addToQuery(")");
    }

    public void visit(LikeExpression like) {
      like.getLeftExpression().accept(subExpression());
      addToQuery(":(");
      if (like.isNot()) {
        addToQuery("!");
      }
      addToQuery(
        removeQuotesAndReplaceWildcards(like.getRightExpression().toString())
      );
      addToQuery(")");
    }

    public void visit(IsNullExpression isNull) {
      String test = isNull.isNot() ? "_exists_" : "_missing_";
      addToQuery(test + ":");
      isNull.getLeftExpression().accept(subExpression());
    }

    public void visit(NotEqualsTo notEqualsTo) {
      notEqualsTo.getLeftExpression().accept(subExpression());
      addToQuery(":!");
      notEqualsTo.getRightExpression().accept(subExpression());
    }

    public void visit(DoubleValue value) { addToQuery(value.toString()); }
    public void visit(LongValue value) { addToQuery(value.toString()); }

    public void visit(DateValue value) {
      addToQuery(value.getValue().toString());
    }

    public void visit(TimeValue value) {
      addToQuery(value.getValue().toString());
    }

    public void visit(TimestampValue value) {
      addToQuery(value.getValue().toString());
    }

    public void visit(StringValue value) {
      // Use toString() rather than getValue() to ensure the string is quoted
      String actual = value.getValue();
      if (actual.contains(" ")) {
        addToQuery("\"" + actual + "\"");
      } else {
        addToQuery(actual);
      }
    }
  }

  class GroupByBuilder extends GroupByProcessor
  {
    public void visit(Column column) {
      String name = column.getFullyQualifiedName();
      command.addGroupBy(new Field(name, null));
    }

    public void visit(Function function) {
      throw new UnsupportedOperationException("GROUP BY clause " +
        "must refer to a field");
    }
  }

  class OrderByBuilder extends OrderByProcessor {
    public void visit(OrderByElement orderBy) {
      command.addOrderBy(
        orderBy.getExpression().toString(),
        orderBy.isAsc() ? "ASC" : "DESC"
      );
    }
  }
}
