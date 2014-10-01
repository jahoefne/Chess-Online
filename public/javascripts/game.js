var game = {

    socket: null,
    uri: "ws://" + window.location.host + "/socket/" + window._global_uuid,

    clickedField: function(x,y){
        console.log(x+","+y);
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
        var message = { type: "GetRole" }

        this.send(message);
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
            alert(msg.role);
            break;
        default:
            break;
       };
    },

    send: function(msg){
        this.socket.send(JSON.stringify(msg));
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