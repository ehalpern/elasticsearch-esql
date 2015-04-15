
/**
 * Creates the appropriate result handler
 */
var ResultHandlerFactory = {
  "create": function(data) {
    return new DefaultQueryResultHandler(data)
  }
}

var DefaultQueryResultHandler = function(data) {
  function createScheme() {
    var rows = data.rows
    scheme = []
    for (index = 0; index < rows.length; index++) {
      row = rows[index]

      for (key in row) {
        if (scheme.indexOf(key) == -1) {
          scheme.push(key)
        }
      }
    }
    return scheme
  }

  this.data = data
  this.head = createScheme()
};

DefaultQueryResultHandler.prototype.getHead = function() {
  return this.head
};

DefaultQueryResultHandler.prototype.getBody = function() {
  var rows = this.data.rows
  var body = []
  for(var i = 0; i < rows.length; i++) {
    body.push(rows[i])
  }
  return body
};
