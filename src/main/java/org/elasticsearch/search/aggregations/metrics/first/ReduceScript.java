package org.elasticsearch.search.aggregations.metrics.first;

import org.elasticsearch.script.AbstractExecutableScript;
import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.Map;

/**
 * Called once per group after map.
 */
public class ReduceScript implements NativeScriptFactory
{
  public AbstractExecutableScript newScript(final Map<String, Object> params) {
    return new AbstractSearchScript() {
      public Object run() {
        return ScriptParam._aggs(params).get(0);
      }
    };
  }
}
