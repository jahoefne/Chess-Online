var board = {
    playerID: window._global_playerID,
    myTurn: false,
    lastClicked:null,

    clickedField: function(x,y){
        if(this.myTurn){
             if(this.lastClicked != null){
                 this.unhighlightAll();
                 this.move(this.lastClicked[0], this.lastClicked[1], x, y);
                 this.lastClicked = null;
             }else{

                 //this.lastClicked = {el:$("#"+x+""+y), x:x, y:y};
                 this.lastClicked = [x, y];
                 //this.highlight(this.lastClicked);

                 // query possible moves
                 var message = {type: "PossibleMoves", x: x, y: y};

                 websocket.sendMessage(message);
             }
         }
    },

    highlight: function(el){
        //el.addClass("highlight");
        $("chess-board").attr("light", el);
        console.log("light", $("chess-board"));
    },

    unhighlightAll: function(){

                $("chess-board").attr("light","none");
    },

    move: function(srcX, srcY, dstX, dstY){
        console.log($("#"+srcX+""+srcY).offset());
        var msg = {type:"Move", srcX: srcX, srcY: srcY, dstX:dstX, dstY:dstY}
        websocket.sendMessage(msg);
    },

    gotActiveGameMsg: function(msg){
            this.updateField(msg);
            if(msg.white == "" || msg.black == ""){
               $("#header-bar").text("Waiting for other player!");
               $("#header-bar").addClass("blink_me");
               $("#header-bar").removeClass("red");
               return;
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
    },

    updateField: function(msg){
       // $("chess-board").data(msg.field);
       $("chess-board").attr("data",msg.field);
        console.log("Update field", $("chess-board"));
    }
};