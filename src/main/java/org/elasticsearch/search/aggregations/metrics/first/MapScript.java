package org.elasticsearch.search.aggregations.metrics.first;

import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.script.ScriptException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Called for every document (limited to 1 group and shard)
 */
public class MapScript implements NativeScriptFactory
{
  public ExecutableScript newScript(final Map<String, Object> params)
  {
    return new AbstractSearchScript()
    {
      List<String> fields = ScriptParam.fields(params);
      List<Object> values = ScriptParam.values(params);

      public Object run() {
        if (values.isEmpty()) {
          for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i).toString();
            values.add(source().extractValue(field));
          }
        }
        return params;
      }
    };
  }
}
