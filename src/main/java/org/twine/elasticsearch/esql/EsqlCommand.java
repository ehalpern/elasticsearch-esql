package org.twine.elasticsearch.esql;

import org.durid.sql.ast.expr.SQLCharExpr;
import org.durid.sql.ast.expr.SQLMethodInvokeExpr;
import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.Condition;
import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.domain.Where;
import org.nlpcn.es4sql.query.AggregationQueryAction;
import org.nlpcn.es4sql.query.DefaultQueryAction;
import org.nlpcn.es4sql.query.QueryAction;

/**
 * Created by eric on 4/8/15.
 */
public class EsqlCommand extends Select
{
  private StringBuffer queryString = new StringBuffer();

  /*package*/ void appendToQuery(String s) {
    queryString.append(s);
  }

  /*package*/ EsqlCommand complete() {
    if (queryString.length() > 0) {
      SQLMethodInvokeExpr value = new SQLMethodInvokeExpr("query");
      value.getParameters().add(new SQLCharExpr(queryString.toString()));
      Where where = new Condition(Condition.CONN.AND, "_q", Condition.OPEAR.EQ, value);
      setWhere(where);
    }
    return this;
  }

  public QueryAction actionQuery(Client client) {
    if (isAgg) {
      return new AggregationQueryAction(client, this);
    } else {
      return new DefaultQueryAction(client, this);
    }
  }

}
