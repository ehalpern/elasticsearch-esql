package org.twine.sql.processor;

import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class StatementProcessor implements StatementVisitor
{
	protected SelectVisitor select() {
		return new SelectStatementProcessor();
	}

	public void visit(Select select) {
		select.getSelectBody().accept(select());
	}

	public void visit(Delete delete) {
		throw new UnsupportedOperationException("DELETE not yet supported");
	}

	public void visit(Update update) {
		throw new UnsupportedOperationException("UPDATE not yet supported");
	}

	public void visit(Insert insert) {
		throw new UnsupportedOperationException("INSERT not yet supported");
	}

	public void visit(Replace replace) {
		throw new UnsupportedOperationException("REPLACE not yet supported");
	}

	public void visit(Drop drop) {
		throw new UnsupportedOperationException("DROP not yet supported");
	}

	public void visit(Truncate truncate) {
		throw new UnsupportedOperationException("TRUNCATE not yet supported");
	}

	public void visit(CreateIndex createIndex) {
		throw new UnsupportedOperationException("CREATE INDEX not supported");
	}

	public void visit(CreateTable createTable) {
		throw new UnsupportedOperationException("CREATE TABLE not supported");
	}

	public void visit(CreateView createView) {
		throw new UnsupportedOperationException("CREATE VIEW not supported");
	}

	public void visit(Alter alter) {
		throw new UnsupportedOperationException("ALTER not supported");
	}

	public void visit(Statements stmts) {
		throw new UnsupportedOperationException("Multiple statements not supported");
	}
}
