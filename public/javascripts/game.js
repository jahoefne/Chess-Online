var game = {

    webSocketUri : "ws://" + window.location.host + "/socket/" + window._global_uuid,

    clickedField: function(x,y){
        console.log(x+","+y);
    }

    /**
     *  Connect to Server using Websockets
     */

}