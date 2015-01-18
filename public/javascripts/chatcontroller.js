'use strict';

/** Controllers */
angular.module('chatapp.chatcontroller', []).
    controller('ChatCtrl', function ($scope) {

        $scope.msgs = [];

        /** handle incoming messages: add to messages array */
        $scope.addMsg = function (msg) {
            $scope.$apply(function () {
                $scope.msgs.push(msg);
            });

            if(msg.role != $("player-info").attr("playerRole").toLowerCase()){
                $("#chat-notify-sound")[0].play();
                if ('vibrate' in navigator){
                	navigator.vibrate(1000);
                }
            }
            $('#history').animate({scrollTop: $('#history').prop('scrollHeight')});
        };

        $scope.addAllMsgs = function (msgs) {
            for(var i=0; i< msgs.length; i++){
                $scope.$apply(function () { $scope.msgs.push(msgs[i]); });
            }
            $('#history').animate({scrollTop: $('#history').prop('scrollHeight')});
         }
    });
