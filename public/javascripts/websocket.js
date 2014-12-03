var websocket = {
    socket: null,
    uri: "wss://" + window.location.host + "/socket/" + window._global_uuid + "/"+ window._global_playerID,
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
        var message = { type: "GetRole" }
        this.sendMessage(message);
    },

    onMessage: function(event){
       var msg = JSON.parse(event.data);
       switch(msg.type){

        case "Role":
            $("#status").text("You're the "+msg.role);
            break;

        case "ActiveGame":
            board.gotActiveGameMsg(msg);
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