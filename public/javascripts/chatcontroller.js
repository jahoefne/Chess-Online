'use strict';

/** Controllers */
angular.module('chatapp.chatcontroller', []).
    controller('ChatCtrl', function ($scope) {

        $scope.msgs = [];

        /** handle incoming messages: add to messages array */
        $scope.addMsg = function (msg) {
            $scope.$apply(function () { $scope.msgs.push(msg); });
            $('#history').animate({scrollTop: $('#history').prop('scrollHeight')});
        };

    });
