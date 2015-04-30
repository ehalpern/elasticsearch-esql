package org.twine.sql.processor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.*;
import org.twine.esql.EsqlUnsupportedFeatureException;

public abstract class SelectStatementProcessor implements SelectVisitor
{
  protected abstract SelectItemVisitor selectItem();
	protected abstract FromItemVisitor from();
	protected abstract ExpressionVisitor where();
	protected abstract ExpressionVisitor groupBy();
	protected abstract OrderByVisitor orderBy();
	protected abstract void visit(Limit limit);

	public void visit(PlainSelect select)
	{
		for (SelectItem item: select.getSelectItems()) {
			item.accept(selectItem());
		}

		select.getFromItem().accept(from());

		if (select.getWhere() != null) {
			select.getWhere().accept(where());
		}

		if (select.getGroupByColumnReferences() != null) {
			for (Expression column : select.getGroupByColumnReferences()) {
				column.accept(groupBy());
			}
		}

		if (select.getOrderByElements() != null) {
			for (OrderByElement element : select.getOrderByElements()) {
				element.accept(orderBy());
			}
		}

		if (select.getLimit() != null) {
			visit(select.getLimit());
		}
	}

	public void visit(SetOperationList setOpList) {
		throw new EsqlUnsupportedFeatureException("Set operations");
	}

	public void visit(WithItem withItem) {
		throw new EsqlUnsupportedFeatureException("WITH");
	}
}
