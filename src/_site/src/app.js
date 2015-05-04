var NavBar = React.createClass({
  render: function() {
    var cs = React.addons.classSet;
    return (
      <nav className={cs('navbar', 'navbar-inverse', 'navbar-fixed-top')} role="navigation">
        <div className={cs('container-fluid')}>
          <div className={cs('navbar-header')}>

            <a className={cs('navbar-brand')} href="#">Elasticsearch QL</a>
          </div>
          <div id="navbar" className={cs('navbar-collapse', 'collapse')}>
            <ul className={cs('nav', 'navbar-nav', 'navbar-right')}>

              <li id="help_btn" data-toggle="modal" data-target="#help_popup">
                <a title="Help" href="#" data-placement="bottom">Help</a>
              </li>

            </ul>

            <form className={cs('navbar-form', 'navbar-right')}>
              <input type="text" id="urlBox" class="form-control" ng-model="url" />
            </form>
          </div>
        </div>
      </nav>
    );
  }
});

var QueryBox = React.createClass({
  render: function() {
    return (
      <div class="search-area">
        <textarea id="queryTextarea">SELECT * FROM myTable LIMIT 10</textarea>
        <button type="button" ng-click="search()" id="searchButton" class="btn btn-success search-button" ng-bind-html="getButtonContent(searchLoading,'Search')" ng-cloak>
        </button>
        <button type="button" ng-click="explain()" id="explainButton" class="btn btn-info explain-button" ng-bind-html="getButtonContent(explainLoading,'Explain')" ng-cloak>
       </button>
      </div>
    );
  }
});

var QueryError = React.createClass({
  render: function () {
    return (
      <div id="errorBox" ng-hide="error == ''" class="alert alert-danger fadein" role="alert"
           ng-cloak>
        <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
        <span class="sr-only">Error:</span>
        <span ng-model="error">error</span>
      </div>
    );
  }
});

var QueryResult = React.createClass({
  render: function () {
    return (
      <div class="fadein" ng-show="resultsColumns.length > 0" ng-cloak>
        <div>
          <h2 class="sub-header">Results</h2>
        </div>

        <div class="table-responsive">
          <table class="table table-striped" id="searchResult">
            <thead>
            <tr id="tableHead">
              <th ng-repeat="column in resultsColumns">column</th>
            </tr>
            </thead>
            <tbody id="tableBody">
            <tr ng-repeat="row in resultsRows">
              <td ng-repeat="column in resultsColumns">
                Foo
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <button type="button" ng-click="exportCSV()" id="exportCSV" class="btn btn-primary btn-sm"
                ng-cloak>Export CSV <span class="glyphicon glyphicon-share"></span></button>
      </div>
    );
  }
});

var ExplainResult = React.createClass({
  render: function () {
    return (
      <div class="fadein" ng-show="resultExplan" ng-cloak>
        <h2 class="sub-header">Results</h2>

        <div class="table-responsive">
          <textarea id="explanResult"></textarea>
        </div>
      </div>
    );
  }
});

React.render(
  <body>
    <NavBar />
    <div class="container-fluid">
      <div class="row">
        <div class="col-sm-9 col-sm-offset-3 col-md-11 col-md-offset-1 main">
          <h1 class="page-header">ESQL Query</h1>
          <QueryBox/>
          <QueryError/>
          <QueryResult/>
          <ExplainResult/>
        </div>
      </div>
    </div>
  </body>,
  document.getElementById('elasticsearch-esql')
);


