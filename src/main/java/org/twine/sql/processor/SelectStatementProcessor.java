package org.twine.sql.processor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.*;

public class SelectStatementProcessor implements SelectVisitor
{
  protected SelectItemVisitor selectItem() {
		return new SelectItemProcessor();
	}

	protected FromItemVisitor from() {
		return new FromProcessor();
	}

	protected ExpressionVisitor where() {
		return new WhereExpressionProcessor();
	}

	protected ExpressionVisitor groupBy() {
		return new GroupByProcessor();
	}

	protected OrderByVisitor orderBy() {
		return new OrderByProcessor();
	}


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

	protected void visit(Limit limit) {
		throw new UnsupportedOperationException();
	}

	public void visit(SetOperationList setOpList) {
		throw new UnsupportedOperationException("Set operations not supported");
	}

	public void visit(WithItem withItem) {
		throw new UnsupportedOperationException("WITH clause not supported");
	}
}
