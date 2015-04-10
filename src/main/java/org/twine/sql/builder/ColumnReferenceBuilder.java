package org.twine.sql.builder;


/**
 * Created by eric on 4/7/15.
 */
public interface ColumnReferenceBuilder
{
  ColumnReferenceBuilder field(String name);
  ColumnReferenceBuilder function(String function, String field);
}

