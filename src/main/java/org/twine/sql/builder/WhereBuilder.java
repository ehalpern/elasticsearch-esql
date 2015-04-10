package org.twine.sql.builder;


import java.util.List;

/**
 * Created by eric on 4/7/15.
 */
public interface WhereBuilder
{
  WhereBuilder function(String function, List<String> params);
}

