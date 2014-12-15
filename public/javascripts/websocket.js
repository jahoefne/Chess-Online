var websocket = {
    socket: null,
    uri: "ws://" + window.location.host + "/socket/" + window._global_uuid + "/"+ window._global_playerID,
    playerID: window._global_playerID,

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
        var message = { type: "GetGame" }
        this.sendMessage(message);
        $('#chooseRoleModal').modal('show');
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

    onMessage: function(event){
       var msg = JSON.parse(event.data);
       switch(msg.type){

        case "ActiveGame":
            board.gotActiveGameMsg(msg);
            var role;
            if(msg.white == this.playerID){
                role = "White";
            }else if(msg.black == this.playerID){
                role = "Black"
            } else {
                role = "Spectator"
            }
            $("#status").text(role);
            break;

        case "PossibleMoves":
            for(var i=0; i < msg.moves.length; i++){
               $("#"+msg.moves[i][0]+""+msg.moves[i][1]).addClass("highlight");
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