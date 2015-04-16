// Create the queryTextarea editor
window.onload = function() {

  save = function(editor) {
    localStorage[window.location.href.split("#")[0]] = editor.getValue()
  }

  CodeMirror.defineOption("persist", false, function(editor, value) {
    if (value) {
      var address = window.location.href.split("#")[0]
      var persisted = localStorage[address] || editor.getValue()
      editor.setValue(persisted)
      editor.on("blur", save)
    } else {
      editor.off("blur", save)
    }
  });

  window.editor = CodeMirror.fromTextArea(document.getElementById('queryTextarea'), {
    mode: 'text/x-mysql',
    indentWithTabs: true,
    smartIndent: true,
    lineNumbers: true,
    matchBrackets : true,
    autofocus: true,
    persist: true,
    extraKeys: {
      "Ctrl-Space": "autocomplete",
      "Ctrl-Enter": angular.element($("#queryTextarea")).scope().search
    }
  });

  window.explanResult = CodeMirror.fromTextArea(document.getElementById('explanResult'), {
    mode: 'application/json',
    indentWithTabs: true,
    smartIndent: true,
    lineNumbers: true,
    matchBrackets : true
  });
};
