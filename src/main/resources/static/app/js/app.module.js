var app = angular.module("my-app",
    [
        'ngRoute',
        'ngResource',
        'ui.bootstrap',
        'ngFileUpload'
    ]);


app.config(function ($routeProvider, $locationProvider) {

    $locationProvider.html5Mode(true);

    $routeProvider
        .when("/login", {
            template: '<login></login>'
        })
        .when("/wall",
            {
                template: '<wall></wall>'
            })
        .otherwise({
            redirectTo: "/"
        });

});


app.factory("UserApi", ['$resource', function ($resource) {

    var baseUrl = "/user";

    return $resource('/user/:id', {id: '@id'}, {
        register: {
            method: 'POST',
            url: baseUrl + "/save",
        },
        login: {
            method: 'POST',
            url: baseUrl + "/login"
        },
        delete: {
            method: 'DELETE',
            url: baseUrl
        },
        friends: {
            method: 'GET',
            url: baseUrl + "/friends",
            isArray: true
        },
        posts: {
            method: 'GET',
            url: baseUrl + "/posts",
            isArray: true
        },
        user: {
            method: 'GET',
            url: baseUrl
        },
        newPost: {
            method: 'POST',
            url: baseUrl + "/new-post"
        },
        sendRequest: {
            method: 'POST',
            url: baseUrl + "/send-request"
        },
        acceptRequest: {
            method: 'POST',
            url: baseUrl + "/accept-request"
        },
        friendRequests: {
            method: 'GET',
            url: baseUrl + "/friend-requests",
            isArray: true
        }

    });


}]);

app.factory("PostApi", ['$resource', function ($resource) {

    var baseUrl = "/post";

    return $resource('/', {}, {
        new: {
            method: 'POST',
            url: baseUrl + "/new"
        }

    });

}]);


app.filter("timeago", function () {
    //time: the time
    //local: compared to what time? default: now
    //raw: wheter you want in a format of "5 minutes ago", or "5 minutes"
    return function (time, local, raw) {
        if (!time) return "never";

        if (!local) {
            (local = Date.now())
        }

        if (angular.isDate(time)) {
            time = time.getTime();
        } else if (typeof time === "string") {
            time = new Date(time).getTime();
        }

        if (angular.isDate(local)) {
            local = local.getTime();
        } else if (typeof local === "string") {
            local = new Date(local).getTime();
        }

        if (typeof time !== 'number' || typeof local !== 'number') {
            return;
        }

        var
            offset = Math.abs((local - time) / 1000),
            span = [],
            MINUTE = 60,
            HOUR = 3600,
            DAY = 86400,
            WEEK = 604800,
            MONTH = 2629744,
            YEAR = 31556926,
            DECADE = 315569260;

        if (offset <= MINUTE) span = ['', raw ? 'now' : 'less than a minute'];
        else if (offset < (MINUTE * 60)) span = [Math.round(Math.abs(offset / MINUTE)), 'min'];
        else if (offset < (HOUR * 24)) span = [Math.round(Math.abs(offset / HOUR)), 'hr'];
        else if (offset < (DAY * 7)) span = [Math.round(Math.abs(offset / DAY)), 'day'];
        else if (offset < (WEEK * 52)) span = [Math.round(Math.abs(offset / WEEK)), 'week'];
        else if (offset < (YEAR * 10)) span = [Math.round(Math.abs(offset / YEAR)), 'year'];
        else if (offset < (DECADE * 100)) span = [Math.round(Math.abs(offset / DECADE)), 'decade'];
        else span = ['', 'a long time'];

        span[1] += (span[0] === 0 || span[0] > 1) ? 's' : '';
        span = span.join(' ');

        if (raw === true) {
            return span;
        }
        return (time <= local) ? span + ' ago' : 'in ' + span;
    }
});


app.factory("SocketService", function ($q) {

    let connected = false;

    function connect() {
        let deferred = $q.defer();

        let socket = new SockJS('/facebook-socket');
        stompClient = Stomp.over(socket);
        //stompClient.debug = null;
        stompClient.connect({}, function (frame) {
            connected = true;
            deferred.resolve();
        });

        return deferred.promise;
    }
    //todo ulasti okundu tikler, typing, online, last seen, anlik mesajlasma, birebir ve grup chat

    function subscribe(destination, cb) {
        let deferred = $q.defer();
        if (connected) {
            deferred.resolve();
        } else {
            let promise = connect();
            promise.then(function () {
                deferred.resolve();
            });
        }

        deferred.promise.then(function () {
            stompClient.subscribe(destination, function (message) {
                let data = JSON.parse(message.body);
                cb(data);
            });
        });

    }

    function unsubscribe(destination) {
        // TBD
    }

    function disconnect() {
        // TBD
    }

    return {
        connect: connect,
        subscribe: subscribe,
        unsubscribe: unsubscribe,
        disconnect: disconnect
    }
});
