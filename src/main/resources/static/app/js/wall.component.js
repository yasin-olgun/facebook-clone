angular.module("my-app")
    .component("wall", {
        templateUrl: "/app/template/wall.html",
        controller: function ($scope, PostApi, UserApi, SocketService, $q,$timeout) {

            $scope.init = function () {

                UserApi.get(function (response) {
                    $scope.user = response;

                    let promise = SocketService.connect();
                    promise.then(function (){
                        SocketService.subscribe("/server-client/post/" + $scope.user.id, function (response2) {
                            $scope.postList.push(response2);
                            $scope.postList = _.sortBy($scope.postList, ['post', 'creationTime']).reverse();
                            $scope.$apply();

                        });

                        SocketService.subscribe("/server-client/request", function (response3) {
                            if (response3.id !== $scope.user.id) {
                                $scope.friendRequests.push(response3);
                                $scope.$apply();
                            }
                        });

                    });




                });


                UserApi.posts(function (response) {
                    $scope.postList = response;
                    $scope.postList = _.sortBy($scope.postList, ['post', 'creationTime']).reverse();

                });
                UserApi.friendRequests(function (response) {
                    $scope.friendRequests = response;
                });


            }


            $scope.accept = function (id) {
                UserApi.acceptRequest({id: id, state: 0}, {}, function (response) {
                    if (response.code == 0) {
                        toastr.success("OK");
                        _.remove($scope.friendRequests, {id: id})
                    }
                });
            }
            $scope.deny = function (id) {
                UserApi.acceptRequest({id: id, state: 1}, {}, function (response) {
                    if (response.code == 1) {
                        toastr.success("DENY");
                        _.remove($scope.friendRequests, {id: id})

                    }
                });


            }


            $scope.newPost = function () {
                UserApi.newPost($scope.post, function (response) {
                    if (response.code == 0) {
                        toastr.success("POST OK");
                    } else if (response.code == 10) {
                        toastr.success("POST UPDATED");
                    } else {
                        toastr.warning("ERROR");
                    }

                });
            }
            $scope.sendRequest = function () {
                UserApi.sendRequest({username: $scope.username}, {}, function (response) {
                    if (response.code == 0) {
                        toastr.success("Request SEND");
                    } else if (response.code == 1) {
                        toastr.warning("Request already sent");
                    } else if (response.code == 11) {
                        toastr.warning("You can't send a request to yourself")
                    } else {
                        toastr.error("User not found!");
                    }
                });
            }


            $scope.init();

        }
    });