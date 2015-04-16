package org.twine.elasticsearch.search.aggregations.metrics.first;

import org.elasticsearch.script.AbstractExecutableScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Called once after map has run (limited to one shard)
 */
public class CombineScript implements NativeScriptFactory
{
  public ExecutableScript newScript(final Map<String, Object> params) {
    return new AbstractExecutableScript()
    {
      List<String> fields = ScriptParam.fields(params);
      List<Object> values = ScriptParam.values(params);

      public Object run() {
        Map<String, Object> result = new HashMap<String, Object>(fields.size());
        if (fields.size() != values.size()) {
          throw new AssertionError(
            String.format("number of fields %d != number of values %d", fields.size(), values.size())
          );
        }
        for (int i = 0; i < fields.size(); i++) {
          result.put(fields.get(i), values.get(i));
        }
        return result;
      }
    };
  }
}
