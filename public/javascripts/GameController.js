var GameController = {
    socket: null,
    uri: "ws://" + window.location.host + "/socket/" + window._global_uuid + "/"+ window._global_playerID,
    playerID: window._global_playerID,
    myTurn: false,

    init: function() {
        this.socket = new WebSocket(this.uri);
        this.socket.onopen = this.onOpen.bind(this);
        this.socket.onmessage = this.onMessage.bind(this);

        window.onbeforeunload = function() {
            GameController.socket.onclose = $.noop;
            GameController.socket.close();
        }
    },

    onOpen: function(){
        this.becomeSpectator();
        this.sendMessage({type: "GetGame"});
        this.sendMessage({type: "getAllChatMessages"});
        $('#chooseRoleModal').modal('show');
    },

    becomeBlackPlayer: function(){
        $('#chooseRoleModal').modal('hide')
        this.sendMessage({ type: "BlackPlayer" })
    },

    becomeWhitePlayer: function(){
        $('#chooseRoleModal').modal('hide')
        this.sendMessage({ type: "WhitePlayer" })
    },

    becomeSpectator: function(){
        $('#chooseRoleModal').modal('hide')
        this.sendMessage({ type: "Spectator" })
    },

    sendChatMessage: function(message, image, email){
       this.sendMessage({type: "chatMessageClient",
                         txt: message,
                         time: (new Date()).toUTCString(),
                         role: $("player-info").attr("playerRole").toLowerCase(),
                         img: image,
                         email: email });
    },

    onMessage: function(event){
       console.log("Got Message",event);
       var msg = JSON.parse(event.data);

       switch(msg.type){

        case "ActiveGame":
            this.gotActiveGameMsg(msg);
            break;

        case "PossibleMoves":
            document.querySelector("chess-board").gotPossibleMovesMessage(msg);
            break;

        case "getAllChatMessages":
            angular.element($('#chatwrapper')).scope().addAllMsgs(msg.msgs)
            break;

        case "chatMessage":
            angular.element($('#chatwrapper')).scope().addMsg(msg);
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
	},

    /** Update the views to match the current game state */
	gotActiveGameMsg: function(msg){
         var role;
         if(msg.white == this.playerID){
             role = "White";
         }else if(msg.black == this.playerID){
             role = "Black"
         } else {
             role = "Spectator"
         }
         $("player-info").attr("playerRole", role);

         // Ugly comparison necessary because of bad design in SE project
         if(this.playerID == msg.white && msg.whiteOrBlack > 0 || this.playerID == msg.black && msg.whiteOrBlack < 0 ){
             this.myTurn = true;
             $("player-info").attr("status", "It's your turn!");
         }else{
             this.myTurn = false;
             $("player-info").attr("status","Please wait…");
         }

         if(msg.gameOver){
             $("player-info").attr("status", "Game Over");
             this.myTurn = false;
         }

         if(msg.white == "" || msg.black == ""){
             $("player-info").attr("status", "Waiting for other player!");
         }
         $("player-info").attr("white", msg.whiteName);
         $("player-info").attr("black", msg.blackName);
         $("player-info").attr("whitePic", msg.whitePic);
         $("player-info").attr("blackPic", msg.blackPic);
         $("chess-board").attr("data", msg.field);
	}
};

// Init the Game-JS after the document is ready
$(document).ready(function(){
    GameController.init();
});