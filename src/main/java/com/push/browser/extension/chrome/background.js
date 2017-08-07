 


var video;
var count = 0;

//var port = chrome.runtime.connect();
var text;

init22();


    
function init22() {
    chrome.storage.local.get('mytesttext', function (result) {
       text = result.mytesttext;
       alert(text);
       
    });
     var Button = document.getElementById("mybutton");
  Button.innerHTML = text;
 var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", "127.0.0.1:5668", false ); // false for synchronous request
    xmlHttp.send( null );
    return xmlHttp.responseText;
}







    
