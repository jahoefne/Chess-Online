var game = {

    socket: null,
    uri: "ws://" + window.location.host + "/socket/" + window._global_uuid,

    yourTurn: false,
    clicked: null,

    clickedField: function(x,y){
       /* if(this.clicked == null){
            this.clicked = event.target;
            console.log(this.clicked);
            var message = {type: "PossibleMoves", x: x, y: y};
            this.sendMessage(message);
        }   */

      /* TODO: get possible moves and highlight the fields,
               if one of the fields is clicked, call move
      */
     this.yourTurn = false;
      //console.log(x+","+y);
    },

    init: function() {
        this.socket = new WebSocket(this.uri);
        this.socket.onopen = this.onOpen.bind(this);
        this.socket.onclose = this.onClose.bind(this);
        this.socket.onerror = this.onError.bind(this);
        this.socket.onmessage = this.onMessage.bind(this);
    },

    onOpen: function(){
        console.log(this.socket);
        var message = { type: "GetGame" }
        this.sendMessage(message);
        var message = { type: "GetRole" }
        this.sendMessage(message);
    },

    onClose: function(event){
        console.log("Close:",event);
    },

    onError: function(event){
        console.log("Error:",event);
    },

    onMessage: function(event){
       console.log(event);
       var msg = JSON.parse(event.data);
       switch(msg.type){
        case "Role":
            $("#status").text("You're the "+msg.role);
            break;

        case "ActiveGame":
            this.updateField(msg);
            break;

        case "YourTurn":
            this.yourTurn = true;
            $("#header-bar").text("Your turn!");

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

    updateField: function(msg){
        $( ".field" ).each(function(i) {

        var x = Math.floor(i/8);
        var y = i%8;
        console.log(x,":",y)
          $( this ).text(figures[msg.field[y][x]])
        });
    }
};

// Init the Game-JS after the document is ready
$(document).ready(function(){
    game.init();
});