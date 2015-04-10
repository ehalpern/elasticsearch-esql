package org.twine.sql.builder;


/**
 * Created by eric on 4/7/15.
 */
public interface FromItemBuilder
{
  FromItemBuilder from(String index, String type, String alias);
}

