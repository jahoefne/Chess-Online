var game = {
    socket: null,
    uri: "ws://" + window.location.host + "/socket/" + window._global_uuid + "/"+ window._global_playerID,

    playerID: window._global_playerID,

    yourTurn: false,
    clicked: null,
    clickedX: null,
    clickedY: null,
    possibleMoves: [],

    clickedField: function(x,y){
         if(this.clicked!=null){
             this.clicked.removeClass("highlight");
             this.clicked = null;
             this.move(this.clickedX,this.clickedY, x,y);
             for(var i =0; i<this.possibleMoves.length; i++){
                          this.possibleMoves[i].removeClass("highlight");
                      }
             return;
         }
         for(var i =0; i<this.possibleMoves.length; i++){
             this.possibleMoves[i].removeClass("highlight");
         }
         this.clickedX = x;
         this.clickedY = y;
         this.possibleMoves.length = 0;
         this.clicked = $("#"+x+""+y);
         this.clicked.addClass("highlight");
         console.log(this.clicked);
         var message = {type: "PossibleMoves", x: x, y: y};
         this.sendMessage(message);
         this.yourTurn = false;
    },

    move: function(srcX, srcY, dstX, dstY){
        var msg = {type:"Move", srcX: srcX, srcY: srcY, dstX:dstX, dstY:dstY}
        this.sendMessage(msg);
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
            console.log(msg.whiteOrBlack);
            console.log(this.playerID);
            console.log(msg.white);
            console.log(msg.black);
            break;

        case "PossibleMoves":
            console.log(msg);
            for(var i=0; i < msg.moves.length; i++){
                this.possibleMoves[i] = $("#"+msg.moves[i][0]+""+msg.moves[i][1]);
                console.log(msg.moves[i]);
                this.possibleMoves[i].addClass("highlight");
            }
            break;


        // turn indicator
        //  $("#header-bar").text("Your turn!");

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