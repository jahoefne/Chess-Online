var board = {
    playerID: window._global_playerID,

    myTurn: false,

    lastClicked:null,

    clickedField: function(x,y){
        if(this.myTurn){
             if(this.lastClicked != null){
                 this.unhighlightAll();
                 this.move(this.lastClicked.x, this.lastClicked.y, x, y);
                 this.lastClicked = null;
             }else{
                 this.lastClicked = {el:$("#"+x+""+y), x:x, y:y};
                 this.highlight(this.lastClicked.el);

                 // query possible moves
                 var message = {type: "PossibleMoves", x: x, y: y};

                 websocket.sendMessage(message);
             }
         }
    },

    highlight: function(el){
        el.addClass("highlight");
    },

    unhighlightAll: function(){
         $( ".field" ).each(function() {
                $( this ).removeClass("highlight");
         });
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
               return;
            }

            if(this.playerID == msg.white && msg.whiteOrBlack > 0  // we are white player and it's our turn
               || this.playerID == msg.black && msg.whiteOrBlack < 0 ){  // we are black player and it's our turn
                 this.myTurn = true;
                 $("#header-bar").text("It's your turn!");
            }else{
                 this.myTurn = false;
                  $("#header-bar").text("Please wait.");
            }

            if(msg.gameOver){
                $("#header-bar").text("Game over");
                 this.myTurn = false;
            }
    },

    updateField: function(msg){
        console.log("Update field");
        $( ".field" ).each(function(i) {
            var x = Math.floor(i/8);
            var y = i%8;
            $( this ).html(figures[msg.field[x][y]]);
        });
    }
};