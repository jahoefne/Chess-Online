var websocket = {
    socket: null,
    uri: "ws://" + window.location.host + "/socket/" + window._global_uuid + "/"+ window._global_playerID,
    playerID: window._global_playerID,
    roleChosen: false,
    myTurn: false,

    init: function() {
        this.socket = new WebSocket(this.uri);
        this.socket.onopen = this.onOpen.bind(this);
        this.socket.onmessage = this.onMessage.bind(this);

        window.onbeforeunload = function() {
        websocket.socket.onclose = $.noop;
        websocket.socket.close();
        }
    },

    onOpen: function(){
        this.becomeSpectator(); // Start as spec
        this.sendMessage({type: "GetGame"});
    },

    becomeBlackPlayer: function(){
        console.log("Black");
        $('#chooseRoleModal').modal('hide')
        this.sendMessage({ type: "BlackPlayer" })
    },

    becomeWhitePlayer: function(){
        console.log("White");
        $('#chooseRoleModal').modal('hide')
        this.sendMessage({ type: "WhitePlayer" })
    },

    becomeSpectator: function(){
        console.log("Spec");
        $('#chooseRoleModal').modal('hide')
        this.sendMessage({ type: "Spectator" })
    },

    sendChatMessage: function(m, i, e){
      console.log("chatMessage");
        this.sendMessage({type: "chatMessageClient", txt: m, time: (new Date()).toUTCString(), role: $("#status").text(), img: i, email: e })
    },

    onMessage: function(event){
       console.log(event);
       var msg = JSON.parse(event.data);

       switch(msg.type){

        case "ActiveGame":
            console.log("ActiveGameMessage");
            //board.gotActiveGameMsg(msg);


            if(!this.roleChosen){
                $('#chooseRoleModal').modal('show');
                this.roleChosen=true;
            }


            var role;
            if(msg.white == this.playerID){
                role = "White";
            }else if(msg.black == this.playerID){
                role = "Black"
            } else {
                role = "Spectator"
            }
            if(this.playerID == msg.white && msg.whiteOrBlack > 0  // we are white player and it's our turn
                || this.playerID == msg.black && msg.whiteOrBlack < 0 ){  // we are black player and it's our turn
                this.myTurn = true;
                $("#header-bar").text("It's your turn!");
                $("#header-bar").addClass("blink_me red");
            }else{
                this.myTurn = false;
                $("#header-bar").text("Please wait.");
                $("#header-bar").removeClass("blink_me red");
            }

            if(msg.gameOver){
                $("#header-bar").text("Game over");
                $("#header-bar").removeClass("blink_me");
                this.myTurn = false;
            }
            if(msg.white == "" || msg.black == ""){
                $("#header-bar").text("Waiting for other player!");
                $("#header-bar").addClass("blink_me");
                $("#header-bar").removeClass("red");

            }
            $("player-info").attr("white", msg.whiteName);
            $("player-info").attr("black", msg.blackName);
            $("player-info").attr("whitePic", msg.whitePic);
            $("player-info").attr("blackPic", msg.blackPic);
            $("#status").text(role);
            $("chess-board").attr("data",msg.field);

            break;

        case "PossibleMoves":
            for(var i=0; i < msg.moves.length; i++){
               //$("#"+msg.moves[i][0]+""+msg.moves[i][1]).addClass("highlight");
               var mov =[msg.moves[i][0], msg.moves[i][1]];

            }

            //board.highlight(msg.moves);
            $("chess-board").attr("light", msg.moves);
            break;

        case "chatMessage":
            console.log(this.playerID);
            angular.element($('#chatwrapper')).scope().addMsg(msg);
               if(msg.role != $("#status").text()){
                   $("#chat-notify-sound")[0].play();
               }
             break;
        default:
            break;
       };
    },

	sendMessage : function(message) {
		console.log("sendMessage");
		if (this.socket.readyState !== this.socket.OPEN) {
			console.log("Error sending message")
			return false;
		}
		this.socket.send(JSON.stringify(message));
		return true;
	}
};

// Init the Game-JS after the document is ready
$(document).ready(function(){
    websocket.init();
});