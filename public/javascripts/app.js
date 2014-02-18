(function() {
  var addZero, toHours, toMinutes, toSeconds, utils;
  utils = window.angular.module('utils', []);
  utils.filter('timer', [
    'time', function(time) {
      return function(input) {
        var hours, minutes, seconds;
        if (input) {
          seconds = toSeconds(input);
          minutes = toMinutes(input);
          hours = toHours(input);
          return "" + hours + ":" + minutes + ":" + seconds;
        } else {
          return "No crawler running...";
        }
      };
    }
  ]).service('time', function() {
    this.toHours = function(timeMillis) {
      return addZero(timeMillis / (1000 * 60 * 60));
    };
    this.toMinutes = function(timeMillis) {
      return addZero((timeMillis / (1000 * 60)) % 60);
    };
    this.toSeconds = function(timeMillis) {
      return addZero((timeMillis / 1000) % 60);
    };
    this.toTime = function(hours, minutes, seconds) {
      return ((hours * 60 * 60) + (minutes * 60) + seconds) * 1000;
    };
    return this.addZero = function(value) {
      value = Math.floor(value);
      if (value < 10) {
        return "0" + value;
      } else {
        return value;
      }
    };
  }).controller('TimerController', function($scope, $http) {
    var startWS;
    startWS = function() {
      var wsUrl;
      wsUrl = jsRoutes.controllers.AppController.indexWS().webSocketURL(rootURL, numberOfCrawlers, maxDepthOfCrawling, politenessDelay, maxPagesToFetch);
      $scope.socket = new WebSocket(wsUrl);
      $scope.academicUnits = [];
      return $scope.socket.onmessage = function(msg) {
        return $scope.$apply(function() {
        	//console.log(msg);
          //console.log("received : " + msg);
          //console.log(JSON.parse(msg.data));
          if(typeof JSON.parse(msg.data).AcademicUnit != "undefined") {
          	$scope.academicUnits.push(JSON.parse(msg.data));
          }
          return $scope.time = JSON.parse(msg.data).time;
        });
      };
    };
    startWSTeacher = function() {
      var wsUrlTeacher;
      wsUrlTeacher = jsRoutes.controllers.AppController.indexWSTeacher().webSocketURL(rootURL, numberOfCrawlers, maxDepthOfCrawling, politenessDelay, maxPagesToFetch);
      $scope.socket2 = new WebSocket(wsUrlTeacher);
      $scope.teachers = [];
      return $scope.socket2.onmessage = function(msg) {
        return $scope.$apply(function() {
        	//console.log(msg);
          //console.log("received : " + msg);
          //console.log(JSON.parse(msg.data));
          if(typeof JSON.parse(msg.data).TeacherDepartment != "undefined") {
          	$scope.teachers.push(JSON.parse(msg.data));
          }
          return $scope.time = JSON.parse(msg.data).time;
        });
      };
    };
    $scope.start = function() {
    	return $http.get(jsRoutes.controllers.AppController.start().url).success(function() {});
    };
    $scope.stop = function() {
    	return $http.get(jsRoutes.controllers.AppController.stop().url).success(function() {});
    };
    startWSTeacher();
    return startWS();
  });
  window.angular.module('app', ['utils']);
  addZero = function(value) {
    value = Math.floor(value);
    if (value < 10) {
      return "0" + value;
    } else {
      return value;
    }
  };
  toHours = function(time) {
    return addZero(time / (1000 * 60 * 60));
  };
  toMinutes = function(time) {
    return addZero((time / (1000 * 60)) % 60);
  };
  toSeconds = function(time) {
    return addZero((time / 1000) % 60);
  };
}).call(this);