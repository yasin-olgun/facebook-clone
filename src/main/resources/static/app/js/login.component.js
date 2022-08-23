angular.module("my-app")
    .component("login", {
        templateUrl: "/app/template/login.html",
        controller: function ($scope, UserApi, $location, SocketService,Upload) {

            $scope.login = function () {
                UserApi.login({
                    username: $scope.loginRequest.username,
                    password: $scope.loginRequest.password
                }, {}, function (response) {
                    if (response.code == 0) {


                        $location.path("/wall");
                    } else {
                        toastr.error("Username or password wrong");
                    }


                });
            };

            $scope.fileSelected = function (file){
                Upload.base64DataUrl($scope.file).then(function(data){
                    $scope.user.avatar = data;

                });
                console.log($scope.user.avatar);
            }

            $scope.register = function () {

                UserApi.register($scope.user, function (response) {
                    console.log(response);
                    if (response != null) {
                        toastr.success("Register Succes");
                    } else {
                        toastr.error("Register failed");

                    }
                });
                console.log("register func");
            };

            $scope.init = function () {
                console.log("hello world");
            };

            $scope.init();
        }
    });
