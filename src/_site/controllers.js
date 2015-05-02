var elasticsearchSqlApp = angular.module('elasticsearchSqlApp', ["ngAnimate", "ngSanitize"]);

elasticsearchSqlApp.controller('MainController', function ($scope, $http, $sce, $q) {
	$scope.url = getUrl();
	$scope.error = "";	
	$scope.resultsColumns = [];
	$scope.resultsRows = [];
	$scope.hintColumns = ["q", "desc"];
	$scope.hintRows = [
		{ q: "SELECT * FROM i LIMIT 10", desc: "Show the first 10 documents from index i (which each field as a column)" },
	  { q: "SELECT f1, f3 FROM i LIMIT 10", desc: "Show fields f1 and f3 of the first 10 documents from index i" }
	];
	$scope.searchLoading = false;
	$scope.explainLoading = false;
	$scope.resultExplan = false;

	$scope.canceller = $q.defer();

	$scope.cancelSearch = function() {
		$scope.canceller.resolve("cancelled");
	}

	$scope.search = function() {
		// Reset results and error box
		$scope.error = "";
		$scope.resultsColumns = [];
		$scope.resultsRows = [];
		$scope.searchLoading = true;
		$scope.$apply();
		$scope.resultExplan = false;

		saveUrl()

		var query = window.editor.getValue();

		$http.post($scope.url + "_esql", query, { timeout: $scope.canceller.promise })
			.success(function(data, status, headers, config) {
				var handler = ResultHandlerFactory.create(data);
				$scope.resultsColumns = handler.getHead();
				$scope.resultsRows = handler.getBody();

			})
			.error(function(data, status, headers, config) {
				if(data == "") {
					$scope.error = "Error occurred! response is not available.";
				} else {
					$scope.error = data; // JSON.stringify(data);
				}
			})
			.finally(function() {
				$scope.searchLoading = false;
				$scope.$apply()
			});
	}
	
	$scope.explain = function() {
		// Reset results and error box
		$scope.error = "";
		$scope.resultsColumns = [];
		$scope.resultsRows = [];
		$scope.explainLoading = true;
		$scope.$apply();
		$scope.resultExplan = true;

		saveUrl()

		var query = window.editor.getValue();
		$http.post($scope.url + "_esql/_explain", query)
			.success(function(data, status, headers, config) {
				 $scope.resultExplan = true;
				 window.explanResult.setValue(JSON.stringify(data, null, "\t"));
			})
			.error(function(data, status, headers, config) {
				$scope.resultExplan = false;
				if(data == "") {
					$scope.error = "Error occured! response is not avalible.";
				} else {
					$scope.error = data;
				}
			})
			.finally(function() {
				$scope.explainLoading = false;
				$scope.$apply()
			});
	}

	$scope.exportCSV = function() {			
		var columns = $scope.resultsColumns ;
		var rows = $scope.resultsRows ;
		var data =arr2csvStr(columns,',') ;
		for(var i=0; i<rows.length ; i++){
			data += "\n";
			data += map2csvStr(columns,rows[i],',') ;

		}
		window.location='data:text/csv;charset=utf8,' + encodeURIComponent(data);
		return true;
	}

	$scope.getButtonContent = function(isLoading , defName) {
		var loadingContent = "<span class=\"glyphicon glyphicon-refresh glyphicon-refresh-animate\"></span> Loading...";
		var returnValue = isLoading ? loadingContent : defName;
		return $sce.trustAsHtml(returnValue);
	}
	
	
	function arr2csvStr(arr,op){
		var data = arr[0]; 
		for(var i=1; i<arr.length ; i++){
				data += op;
				data += arr[i] ;
		}
		return data ;
	}
	
	function map2csvStr(columns,arr,op){
		var data = arr[columns[0]]; 
		for(var i=1; i<columns.length ; i++){
				data += op;
				data += arr[columns[i]] ;
		}
		return data ;
	}


	function getUrl() {
		var url = localStorage.getItem("lasturl");
		if (url == undefined) {
			if(location.protocol == "file") {
				url = "http://localhost:9200"
			}
			else {
				url = location.protocol+'//' + location.hostname + (location.port ? ':'+location.port : '');
			}
		}
		
		if(url.substr(url.length - 1, 1) != '/') {
			url += '/'
		}

		return url
	}

	function saveUrl() {
		localStorage.setItem("lasturl", $scope.url);
	} 
});