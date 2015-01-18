Polymer('chess-board', {
    data: null,
    size:'',
    highlightedFields: [],
    lastClicked: null,

    /* Maps the Game-Controllers Figure-Representation, to the Chess-Fonts Figure-Representation */
    figureFontMap:{"-1": "p", "-2": "T", "-3": "S", "-4": "V", "-5": "W", "-6": "k",
                    "0": "", "1": "P", "2": "R", "3": "N", "4": "L", "5": "Q", "6": "K"},


    /** The field state has changed, update out fields */
    dataChanged: function() {
        if(this.data!=null){
            var parsed = this.data.split(",");
            for(i = 0; i < this.fields.length; i++){
                this.fields[i].figure = this.figureFontMap[parsed[i]];
            }
        }
    },

    /* Adjust size */
    sizeChanged: function(){
        if(this.size == 'normal'){
            this.wrapper = "field-wrapper";
        }
        if(this.size == 'small'){
            this.wrapper = "field-wrapper-small";
        }
    },

    /** unHighlights all highlighted fields */
    unHighlightAll: function(){
        if(this.highlightedFields != null){
            for(i=0; i < this.highlightedFields.length; i++){
                this.highlightedFields[i].highlight='';
            }
            this.highlightedFields = [];
        }
    },

    /** Called from GameController.js when a PossbileMoves Message was received.
        Un-highlights all old fields, and highlights the fields noted in the message */
    gotPossibleMovesMessage: function(msg){
        this.unHighlightAll();
        for(i=0; i < msg.moves.length; i++){
          var field = this.fields[msg.moves[i][0]*8 + msg.moves[i][1]];
          this.highlightedFields.push(field);
          field.highlight = 'highlight';
        }
    },

    /** Send a move message, move figure from src to dst */
    move: function(src, dst){
        var msg = {type: "Move", srcX: src.row, srcY: src.col, dstX: dst.row, dstY: dst.col};
        GameController.sendMessage(msg);
        this.unHighlightAll();
        this.lastClicked=null;
    },

    /* Gets called when a field was clicked, decide whether to query for possible moves, or move a figure */
    clickedField: function(event, detail, sender){
       if(GameController.myTurn){
           var clicked = this.fields[sender.getAttribute('index')];
           var isHighlighted = $.inArray(clicked, this.highlightedFields) > 0;
           if(isHighlighted && clicked != this.lastClicked){
               this.move(this.lastClicked, clicked)
           }else if(this.lastClicked==null|| !isHighlighted){
               GameController.sendMessage({type: "PossibleMoves", x: clicked.row, y: clicked.col});
               this.lastClicked = clicked;
           }
       }
    },

    /* Polymer element was created, build up the field */
    created: function(){
        this.wrapper = "field-wrapper";
        this.fields = new Array(64);
        var x = 0;
        var y = 0;
        for(i = 0; i < this.fields.length; i++){
                var color;
                if((x + y) % 2 == 0){
                    color = "dark field";
                }else{
                    color = "bright field";
                }
                this.fields[i] = {i: i, row: x, col: y, color: color, figure:"", highlight:""};
                this.fields[i].click
                y++;
                if(y % 8 == 0){y = 0; x++;}
        }
    }
});