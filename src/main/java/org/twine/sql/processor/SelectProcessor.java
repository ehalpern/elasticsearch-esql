package org.twine.sql.processor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import org.twine.sql.builder.SelectBuilder;

import java.util.List;

public class SelectProcessor implements SelectVisitor
{
	private final SelectBuilder builder;

	/*package*/ SelectProcessor(SelectBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void visit(PlainSelect select)
	{
		for (SelectItem item: select.getSelectItems()) {
			item.accept(new SelectItemProcessor(builder.selectItem()));
		}

		select.getFromItem().accept(new FromItemProcessor(builder.from()));

		if (select.getWhere() != null) {
			select.getWhere().accept(new WhereExpressionProcessor(builder.where()));
		}

		if (select.getGroupByColumnReferences() != null) {
			for (Expression column : select.getGroupByColumnReferences()) {
				column.accept(new ColumnReferenceProcessor(builder.groupBy()));
			}
		}

		if (select.getOrderByElements() != null) {
			for (OrderByElement element : select.getOrderByElements()) {
				builder.orderBy(
					element.getExpression().toString(),
					element.isAsc()
				);
			}
		}

		if (select.getLimit() != null) {
			builder.limit(
				select.getLimit().getOffset(),
				select.getLimit().getRowCount()
			);
		}
	}

	@Override
	public void visit(SetOperationList setOpList) {
		List<?> selects = setOpList.getPlainSelects();
		List<?> orderByElements = setOpList.getOrderByElements();
		Limit limit = setOpList.getLimit();
		// May include ORDER BY and LIMIT
	}

	@Override
	public void visit(WithItem withItem) {
		throw new UnsupportedOperationException("WITH clause not supported");
	}
}
