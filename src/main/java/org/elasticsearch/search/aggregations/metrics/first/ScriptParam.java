package org.elasticsearch.search.aggregations.metrics.first;

import org.elasticsearch.script.ScriptException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptParam
{
  /**
   * @return fields list (validated)
   */
  public static List<String> fields(Map<String, Object> params) {
    // validate carefully since this is user provided
    Object o = params.get("fields");
    if (o != null) {
      try {
        List<String> fields = (List<String>)o;
        if (fields.size() > 0) {
          return fields;
        }
      } catch (ClassCastException e) {
        // fall through
      }
    }
    throw new ScriptException("Expecting 'params.fields[]' with at least one entry");
  }

  /**
   * @return values list (lazily created if need)
   */
  public static List<Object> values(Map<String, Object> params) {
    // lazily initialize if needed.  This isn't threadsafe but that's OK
    // since scripts are not multithreaded.
    List<Object> values = (List<Object>)params.get("values");
    if (values == null) {
      values = new ArrayList<Object>(fields(params).size());
      params.put("values", values);
    }
    return values;
  }

  /**
   * @return _aggs
   */
  public static List<Object> _aggs(Map<String, Object> params) {
    return (List<Object>)params.get("_aggs");
  }
}
