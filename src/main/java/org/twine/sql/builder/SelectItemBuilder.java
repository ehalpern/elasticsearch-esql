package org.twine.sql.builder;


import java.util.List;

/**
 * Created by eric on 4/7/15.
 */
public interface SelectItemBuilder {
  SelectItemBuilder star();
  SelectItemBuilder field(String name, String alias);
  SelectItemBuilder function(String function, List<String> params, String alias);
}

