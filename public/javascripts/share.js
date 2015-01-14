var shareButton = {
    init: function(){
        var title = "Chess-Online invitation";
        var description = window._global_username +
                          " invited you to a round of chess. Join over here: " + window.location.href ;

        new Share('.share-button',{
                        title: title,
                        description: description,
                        url:window.location.href,
           networks: {
                      email: {
                      enabled: true,
                      title:  title,
                      description: description
                      }
                  }
        });
    }
 }

 // display share button after the document is ready
 $(document).ready(function(){ shareButton.init(); });