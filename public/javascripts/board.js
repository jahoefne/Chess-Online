Polymer('chess-board', {
            created: function(){
                this.figures = new Array();
                this.figures[-1] = "\u265F";
                this.figures[-2] = "\u265C";
                this.figures[-3] = "\u265E";
                this.figures[-4] = "\u265D";
                this.figures[-5] = "\u265B";
                this.figures[-6] = "\u265A";
                this.figures[0] = "";
                this.figures[1] = "\u2659";
                this.figures[2] = "\u2656";
                this.figures[3] = "\u2658";
                this.figures[4] = "\u2657";
                this.figures[5] = "\u2655";
                this.figures[6] = "\u2654";
                this.wrapper = "field-wrapper";

                this.fields = new Array(64);
                x = 0;
                y = 0;
                for(i = 0; i < 64; i++){
                        var color;
                        if((x + y) % 2 == 0){
                            color = "dark field";
                        }else{
                            color = "bright field";
                        }
                        this.fields[i] = {i: i, row: x, col: y, color: color, figure:"", highlight:""};
                        y++;

                    if(y % 8 == 0){
                            y = 0;
                            x++;
                    }

                }
            },
            data: null,
            dataChanged: function() {
            console.error("Attribute changed");
                if(this.data!=null){
                var parsed = this.data.split(",");
                for(i = 0; i < 64; i++){
                    this.fields[i].figure = this.figures[parsed[i]];
                    if( parsed[i] > 0){
                        this.fields[i].figureColor = 'white';
                    }else {
                        this.fields[i].figureColor = 'black';
                    }
                }
            }
            },
            light:'',
            lightChanged: function(){


                if(this.light == 'none'){
                for(i = 0; i < 64; i++){
                    this.fields[i].highlight = '';
                    }
                }
                else{
                var parsed = this.light.split(",");

                for(i = 0; i < 64; i++){
                    for (j = 0; j < parsed.length-1; j++){
                    if(parsed[j] == this.fields[i].row && parsed[j+1] == this.fields[i].col){
                    this.fields[i].highlight = 'highlight';
                    }
                    j++;

                    }

                }
                }
                this.light = '';
            },
            size:'',
            sizeChanged: function(){
            if(this.size == 'normal'){
                this.wrapper = "field-wrapper";
            }
            if(this.size == 'small'){
                this.wrapper = "field-wrapper-small";
            }
            }

        });

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
        $("chess-board").data(msg.field);
        $("chess-board").attr("data",msg.field);
        console.log("Update field", $("chess-board"));
    }
};