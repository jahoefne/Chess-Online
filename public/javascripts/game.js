var game = {

    socket: null,
    uri: "ws://" + window.location.host + "/socket/" + window._global_uuid,

    yourTurn: false,


    clicked: null,
    possibleMoves: [],

    clickedField: function(x,y){
     this.clicked = null;
     this.clicked = $("#"+x+""+y);
     this.clicked.css("background-color","red");
     console.log(this.clicked);
     var message = {type: "PossibleMoves", x: x, y: y};
     this.sendMessage(message);
     this.yourTurn = false;
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

        case "PossibleMoves":
            console.log(msg);
            for(var x=0; x < this.possibleMoves.length; x++){
               //this.possibleMoves[x].css("");
            }
            this.possibleMoves.length=0;

            for(var i=0; i < msg.moves.length; i++){
                this.possibleMoves[i] = $("#"+msg.moves[i][0]+""+msg.moves[i][1]);
                console.log(msg.moves[i]);
                this.possibleMoves[i].css("background-color", "blue");
            }

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
          $( this ).text(figures[msg.field[x][y]])
        });
    }
};

// Init the Game-JS after the document is ready
$(document).ready(function(){
    game.init();
});