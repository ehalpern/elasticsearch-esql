package org.twine.sql.builder;

/**
 * Created by eric on 4/7/15.
 */
public interface SelectBuilder {
  SelectItemBuilder selectItem();
  FromItemBuilder from();
  WhereBuilder where();
  ColumnReferenceBuilder groupBy();
  SelectBuilder orderBy(String name, boolean ascending);
  SelectBuilder limit(long offset, long limit);
}

