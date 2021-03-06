/*
 * Copyright 1999-2011 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.durid.sql.dialect.mysql.parser;

import org.durid.sql.ast.SQLExpr;
import org.durid.sql.ast.SQLName;
import org.durid.sql.ast.expr.SQLBinaryOpExpr;
import org.durid.sql.ast.expr.SQLBinaryOperator;
import org.durid.sql.ast.expr.SQLCharExpr;
import org.durid.sql.ast.expr.SQLHexExpr;
import org.durid.sql.ast.expr.SQLIdentifierExpr;
import org.durid.sql.ast.expr.SQLIntegerExpr;
import org.durid.sql.ast.expr.SQLMethodInvokeExpr;
import org.durid.sql.ast.expr.SQLVariantRefExpr;
import org.durid.sql.ast.statement.SQLAssignItem;
import org.durid.sql.ast.statement.SQLColumnDefinition;
import org.durid.sql.ast.statement.SQLPrimaryKey;
import org.durid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import org.durid.sql.dialect.mysql.ast.expr.MySqlBinaryExpr;
import org.durid.sql.dialect.mysql.ast.expr.MySqlBooleanExpr;
import org.durid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import org.durid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import org.durid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import org.durid.sql.dialect.mysql.ast.expr.MySqlIntervalUnit;
import org.durid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import org.durid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr.SearchModifier;
import org.durid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import org.durid.sql.dialect.mysql.ast.expr.MySqlUserName;
import org.durid.sql.dialect.mysql.ast.statement.MySqlSQLColumnDefinition;
import org.durid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import org.durid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import org.durid.sql.parser.Lexer;
import org.durid.sql.parser.ParserException;
import org.durid.sql.parser.SQLExprParser;
import org.durid.sql.parser.SQLSelectParser;
import org.durid.sql.parser.Token;

public class MySqlExprParser extends SQLExprParser {

    public MySqlExprParser(Lexer lexer){
        super(lexer);
    }

    public MySqlExprParser(String sql) {
        this(new MySqlLexer(sql));
        this.lexer.nextToken();
    }

    public SQLExpr relationalRest(SQLExpr expr) {
        if (identifierEquals("REGEXP")) {
            lexer.nextToken();
            SQLExpr rightExp = equality();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.RegExp, rightExp);
        }
        
        if (identifierEquals("RLIKE")) {
            lexer.nextToken();
            SQLExpr rightExp = equality();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.RegExp, rightExp);
        }

        return super.relationalRest(expr);
    }

    public SQLExpr multiplicativeRest(SQLExpr expr) {
        if (lexer.token() == Token.IDENTIFIER && "MOD".equalsIgnoreCase(lexer.stringVal())) {
            lexer.nextToken();
            SQLExpr rightExp = primary();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.Modulus, rightExp);
        }

        return super.multiplicativeRest(expr);
    }

    public SQLExpr notRationalRest(SQLExpr expr) {
        if (identifierEquals("REGEXP")) {
            lexer.nextToken();
            SQLExpr rightExp = primary();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotRegExp, rightExp);
        }
        
        if (identifierEquals("RLIKE")) {
            lexer.nextToken();
            SQLExpr rightExp = primary();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotRLike, rightExp);
        }

        return super.notRationalRest(expr);
    }

    public SQLExpr primary() {
        final Token tok = lexer.token();
        
        if (identifierEquals("outfile")) {
            lexer.nextToken();
            SQLExpr file = primary();
            SQLExpr expr = new MySqlOutFileExpr(file);
            
            return primaryRest(expr);
            
        }

        switch (tok) {
            case TRUE:
                lexer.nextToken();
                return primaryRest(new MySqlBooleanExpr(true));
            case FALSE:
                lexer.nextToken();
                return primaryRest(new MySqlBooleanExpr(false));
            case LITERAL_ALIAS:
                String aliasValue = lexer.stringVal();
                lexer.nextToken();
                return primaryRest(new SQLCharExpr(aliasValue));
            case VARIANT:
                SQLVariantRefExpr varRefExpr = new SQLVariantRefExpr(lexer.stringVal());
                lexer.nextToken();
                if (varRefExpr.getName().equalsIgnoreCase("@@global")) {
                    accept(Token.DOT);
                    varRefExpr = new SQLVariantRefExpr(lexer.stringVal(), true);
                    lexer.nextToken();
                } else if (varRefExpr.getName().equals("@") && lexer.token() == Token.LITERAL_CHARS) {
                    varRefExpr.setName("@'" + lexer.stringVal() + "'");
                    lexer.nextToken();
                } else if (varRefExpr.getName().equals("@@") && lexer.token() == Token.LITERAL_CHARS) {
                    varRefExpr.setName("@@'" + lexer.stringVal() + "'");
                    lexer.nextToken();
                } 
                return primaryRest(varRefExpr);
            case VALUES:
                lexer.nextToken();
                if (lexer.token() != Token.LPAREN) {
                    throw new ParserException("syntax error, illegal values clause");
                }
                return this.methodRest(new SQLIdentifierExpr("VALUES"), true);
            default:
                return super.primary();
        }

    }

    public final SQLExpr primaryRest(SQLExpr expr) {
        if (expr == null) {
            throw new IllegalArgumentException("expr");
        }

        if (lexer.token() == Token.LITERAL_CHARS) {
            if (expr instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr identExpr = (SQLIdentifierExpr) expr;
                String ident = identExpr.getName();

                if (ident.equalsIgnoreCase("x")) {
                    String charValue = lexer.stringVal();
                    lexer.nextToken();
                    expr = new SQLHexExpr(charValue);

                    return primaryRest(expr);
                } else if (ident.equalsIgnoreCase("b")) {
                    String charValue = lexer.stringVal();
                    lexer.nextToken();
                    expr = new MySqlBinaryExpr(charValue);

                    return primaryRest(expr);
                } else if (ident.startsWith("_")) {
                    String charValue = lexer.stringVal();
                    lexer.nextToken();

                    MySqlCharExpr mysqlCharExpr = new MySqlCharExpr(charValue);
                    mysqlCharExpr.setCharset(identExpr.getName());
                    if (identifierEquals("COLLATE")) {
                        lexer.nextToken();

                        String collate = lexer.stringVal();
                        mysqlCharExpr.setCollate(collate);
                        accept(Token.IDENTIFIER);
                    }

                    expr = mysqlCharExpr;

                    return primaryRest(expr);
                } else if (ident.equalsIgnoreCase("BINARY")) {
                    String charValue = lexer.stringVal();
                    lexer.nextToken();

                    MySqlCharExpr mysqlCharExpr = new MySqlCharExpr(charValue);
                    mysqlCharExpr.setCharset("BINARY");
                    expr = mysqlCharExpr;

                    return primaryRest(expr);
                }
            } else if (expr instanceof SQLCharExpr) {
                SQLMethodInvokeExpr concat = new SQLMethodInvokeExpr("CONCAT");
                concat.getParameters().add(expr);
                do {
                    String chars = lexer.stringVal();
                    concat.getParameters().add(new SQLCharExpr(chars));
                    lexer.nextToken();
                } while (lexer.token() == Token.LITERAL_CHARS);
                expr = concat;
            }
        } else if (lexer.token() == Token.IDENTIFIER) {
            if (expr instanceof SQLHexExpr) {
                if ("USING".equalsIgnoreCase(lexer.stringVal())) {
                    lexer.nextToken();
                    if (lexer.token() != Token.IDENTIFIER) {
                        throw new ParserException("syntax error, illegal hex");
                    }
                    String charSet = lexer.stringVal();
                    lexer.nextToken();
                    expr.getAttributes().put("USING", charSet);

                    return primaryRest(expr);
                }
            } else if ("COLLATE".equalsIgnoreCase(lexer.stringVal())) {
                lexer.nextToken();

                if (lexer.token() != Token.IDENTIFIER) {
                    throw new ParserException("syntax error");
                }

                String collate = lexer.stringVal();
                lexer.nextToken();

                SQLBinaryOpExpr binaryExpr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.COLLATE,
                                                                 new SQLIdentifierExpr(collate));

                expr = binaryExpr;

                return primaryRest(expr);
            } else if (expr instanceof SQLVariantRefExpr) {
                if ("COLLATE".equalsIgnoreCase(lexer.stringVal())) {
                    lexer.nextToken();

                    if (lexer.token() != Token.IDENTIFIER) {
                        throw new ParserException("syntax error");
                    }

                    String collate = lexer.stringVal();
                    lexer.nextToken();

                    expr.putAttribute("COLLATE", collate);

                    return primaryRest(expr);
                }
            } else if (expr instanceof SQLIntegerExpr) {
                SQLIntegerExpr intExpr = (SQLIntegerExpr) expr;
                String binaryString = lexer.stringVal();
                if (intExpr.getNumber().intValue() == 0 && binaryString.startsWith("b")) {
                    lexer.nextToken();
                    expr = new MySqlBinaryExpr(binaryString.substring(1));

                    return primaryRest(expr);
                }
            }
        }

        if (lexer.token() == Token.LPAREN && expr instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr identExpr = (SQLIdentifierExpr) expr;
            String ident = identExpr.getName();

            if ("EXTRACT".equalsIgnoreCase(ident)) {
                lexer.nextToken();

                if (lexer.token() != Token.IDENTIFIER) {
                    throw new ParserException("syntax error");
                }

                String unitVal = lexer.stringVal();
                MySqlIntervalUnit unit = MySqlIntervalUnit.valueOf(unitVal);
                lexer.nextToken();

                accept(Token.FROM);

                SQLExpr value = expr();

                MySqlExtractExpr extract = new MySqlExtractExpr();
                extract.setValue(value);
                extract.setUnit(unit);
                accept(Token.RPAREN);

                expr = extract;

                return primaryRest(expr);
            } else if ("SUBSTRING".equalsIgnoreCase(ident)) {
                lexer.nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(ident);
                for (;;) {
                    SQLExpr param = expr();
                    methodInvokeExpr.getParameters().add(param);

                    if (lexer.token() == Token.COMMA) {
                        lexer.nextToken();
                        continue;
                    } else if (lexer.token() == Token.FROM) {
                        lexer.nextToken();
                        SQLExpr from = expr();
                        methodInvokeExpr.putAttribute("FROM", from);

                        if (lexer.token() == Token.FOR) {
                            lexer.nextToken();
                            SQLExpr forExpr = expr();
                            methodInvokeExpr.putAttribute("FOR", forExpr);
                        }
                        break;
                    } else if (lexer.token() == Token.RPAREN) {
                        break;
                    } else {
                        throw new ParserException("syntax error");
                    }
                }

                accept(Token.RPAREN);
                expr = methodInvokeExpr;

                return primaryRest(expr);
            } else if ("TRIM".equalsIgnoreCase(ident)) {
                lexer.nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(ident);

                if (lexer.token() == Token.IDENTIFIER) {
                    String flagVal = lexer.stringVal();
                    if ("LEADING".equalsIgnoreCase(flagVal)) {
                        lexer.nextToken();
                        methodInvokeExpr.getAttributes().put("TRIM_TYPE", "LEADING");
                    } else if ("BOTH".equalsIgnoreCase(flagVal)) {
                        lexer.nextToken();
                        methodInvokeExpr.getAttributes().put("TRIM_TYPE", "BOTH");
                    } else if ("TRAILING".equalsIgnoreCase(flagVal)) {
                        lexer.nextToken();
                        methodInvokeExpr.putAttribute("TRIM_TYPE", "TRAILING");
                    }
                }

                SQLExpr param = expr();
                methodInvokeExpr.getParameters().add(param);

                if (lexer.token() == Token.FROM) {
                    lexer.nextToken();
                    SQLExpr from = expr();
                    methodInvokeExpr.putAttribute("FROM", from);
                }

                accept(Token.RPAREN);
                expr = methodInvokeExpr;

                return primaryRest(expr);
            } else if ("MATCH".equalsIgnoreCase(ident)) {
                lexer.nextToken();
                MySqlMatchAgainstExpr matchAgainstExpr = new MySqlMatchAgainstExpr();

                if (lexer.token() == Token.RPAREN) {
                    lexer.nextToken();
                } else {
                    exprList(matchAgainstExpr.getColumns());
                    accept(Token.RPAREN);
                }

                acceptIdentifier("AGAINST");

                accept(Token.LPAREN);
                SQLExpr against = primary();
                matchAgainstExpr.setAgainst(against);

                if (lexer.token() == Token.IN) {
                    lexer.nextToken();
                    if (identifierEquals("NATURAL")) {
                        lexer.nextToken();
                        acceptIdentifier("LANGUAGE");
                        acceptIdentifier("MODE");
                        if (identifierEquals("WITH")) {
                            lexer.nextToken();
                            acceptIdentifier("QUERY");
                            acceptIdentifier("EXPANSION");
                            matchAgainstExpr.setSearchModifier(SearchModifier.IN_NATURAL_LANGUAGE_MODE_WITH_QUERY_EXPANSION);
                        } else {
                            matchAgainstExpr.setSearchModifier(SearchModifier.IN_NATURAL_LANGUAGE_MODE);
                        }
                    } else {
                        throw new ParserException("TODO");
                    }
                } else if (identifierEquals("WITH")) {
                    throw new ParserException("TODO");
                }

                accept(Token.RPAREN);

                expr = matchAgainstExpr;

                return primaryRest(expr);
            } else if ("CONVERT".equalsIgnoreCase(ident)) {
                lexer.nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(ident);

                if (lexer.token() != Token.RPAREN) {
                    exprList(methodInvokeExpr.getParameters());
                }

                if (identifierEquals("USING")) {
                    lexer.nextToken();
                    if (lexer.token() != Token.IDENTIFIER) {
                        throw new ParserException("syntax error");
                    }
                    String charset = lexer.stringVal();
                    lexer.nextToken();
                    methodInvokeExpr.putAttribute("USING", charset);
                }

                accept(Token.RPAREN);

                expr = methodInvokeExpr;

                return primaryRest(expr);
            }
        }
        
        if (lexer.token() == Token.VARIANT && "@".equals(lexer.stringVal())) {
            lexer.nextToken();
            MySqlUserName userName = new MySqlUserName();
            if (expr instanceof SQLCharExpr) {
                userName.setUserName(((SQLCharExpr)expr).toString());
            } else {
                userName.setUserName(((SQLIdentifierExpr)expr).getName());
            }
            
            if (lexer.token() == Token.LITERAL_CHARS) {
                userName.setHost("'" + lexer.stringVal() + "'");
            } else {
                userName.setHost(lexer.stringVal());
            }
            lexer.nextToken();
            return userName;
        }

        return super.primaryRest(expr);
    }

    public SQLSelectParser createSelectParser() {
        return new MySqlSelectParser(this);
    }

    protected SQLExpr parseInterval() {
        accept(Token.INTERVAL);

        if (lexer.token() == Token.LPAREN) {
            lexer.nextToken();

            SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr("INTERVAL");
            if (lexer.token() != Token.RPAREN) {
                exprList(methodInvokeExpr.getParameters());
            }

            accept(Token.RPAREN);

            return primaryRest(methodInvokeExpr);
        } else {
            SQLExpr value = expr();

            if (lexer.token() != Token.IDENTIFIER) {
                throw new ParserException("Syntax error");
            }

            String unit = lexer.stringVal();
            lexer.nextToken();

            MySqlIntervalExpr intervalExpr = new MySqlIntervalExpr();
            intervalExpr.setValue(value);
            intervalExpr.setUnit(MySqlIntervalUnit.valueOf(unit.toUpperCase()));

            return intervalExpr;
        }
    }
    
    public SQLColumnDefinition parseColumn() {
        MySqlSQLColumnDefinition column = new MySqlSQLColumnDefinition();
        column.setName(name());
        column.setDataType(parseDataType());

        return parseColumnRest(column);
    }

    public SQLColumnDefinition parseColumnRest(SQLColumnDefinition column) {
        if (identifierEquals("AUTO_INCREMENT")) {
            lexer.nextToken();
            if (column instanceof MySqlSQLColumnDefinition) {
                ((MySqlSQLColumnDefinition) column).setAutoIncrement(true);
            }
            return parseColumnRest(column);
        }
        
        if (identifierEquals("PARTITION")) {
            throw new ParserException("syntax error " + lexer.token() + " " + lexer.stringVal());
        }
        super.parseColumnRest(column);

        return column;
    }
    
    public SQLExpr orRest(SQLExpr expr) {

        for (;;) {
            if (lexer.token() == Token.OR || lexer.token() == Token.BARBAR) {
                lexer.nextToken();
                SQLExpr rightExp = and();

                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanOr, rightExp);
            } else if (lexer.token() == Token.XOR) {
                lexer.nextToken();
                SQLExpr rightExp = and();

                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanXor, rightExp);
            } else {
                break;
            }
        }

        return expr;
    }
    
    public SQLAssignItem parseAssignItem() {
        SQLAssignItem item = new SQLAssignItem();
        
        SQLExpr var = primary();
        
        if (var instanceof SQLIdentifierExpr) {
            String ident = ((SQLIdentifierExpr) var).getName();
            
            if ("GLOBAL".equalsIgnoreCase(ident)) {
                ident = lexer.stringVal();
                lexer.nextToken();
                var = new SQLVariantRefExpr(ident, true);
            } else if ("SESSION".equalsIgnoreCase(ident)) {
                    ident = lexer.stringVal();
                    lexer.nextToken();
                    var = new SQLVariantRefExpr(ident, false);
            } else {
                var = new SQLVariantRefExpr(ident);
            }
        }
        item.setTarget(var);
        if (lexer.token() == Token.COLONEQ) {
            lexer.nextToken();
        } else {
            accept(Token.EQ);
        }
        item.setValue(expr());

        return item;
    }
    
    public SQLName nameRest(SQLName name) {
        if (lexer.token() == Token.VARIANT && "@".equals(lexer.stringVal())) {
            lexer.nextToken();
            MySqlUserName userName = new MySqlUserName();
            userName.setUserName(((SQLIdentifierExpr)name).getName());
            
            if (lexer.token() == Token.LITERAL_CHARS) {
                userName.setHost("'" + lexer.stringVal() + "'");
            } else {
                userName.setHost(lexer.stringVal());
            }
            lexer.nextToken();
            return userName;
        }
        return super.nameRest(name);
    }
    
    public Limit parseLimit() {
        if (lexer.token() == Token.LIMIT) {
            lexer.nextToken();

            MySqlSelectQueryBlock.Limit limit = new MySqlSelectQueryBlock.Limit();

            SQLExpr temp = this.expr();
            if (lexer.token() == (Token.COMMA)) {
                limit.setOffset(temp);
                lexer.nextToken();
                limit.setRowCount(this.expr());
            } else if (identifierEquals("OFFSET")) {
                limit.setRowCount(temp);
                lexer.nextToken();
                limit.setOffset(this.expr());
            } else {
                limit.setRowCount(temp);
            }
            return limit;
        }

        return null;
    }

    @Override
    public SQLPrimaryKey parsePrimaryKey() {
        accept(Token.PRIMARY);
        accept(Token.KEY);

        MySqlPrimaryKey primaryKey = new MySqlPrimaryKey();

        if (identifierEquals("USING")) {
            lexer.nextToken();
            primaryKey.setIndexType(lexer.stringVal());
            lexer.nextToken();
        }

        accept(Token.LPAREN);
        for (;;) {
            primaryKey.getColumns().add(this.expr());
            if (!(lexer.token() == (Token.COMMA))) {
                break;
            } else {
                lexer.nextToken();
            }
        }
        accept(Token.RPAREN);

        return primaryKey;
    }
}
