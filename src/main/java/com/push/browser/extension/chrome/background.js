


var video;
var count = 0;
var dict = {};
var dictping = {};
var intervalId;
var intervalCount = 10000;
//var port = chrome.runtime.connect();
var text;

LazyIrBackgroundInit();



function LazyIrBackgroundInit() {

    chrome.runtime.onMessage.addListener(
        function(request, sender, sendResponse) {
            if(sender.tab) {
                if (request.message === "try connect") {
                    tryConnect(sender);
                } else if (request.message === "forgot me") {
                    dict[sender.tab.id].close();
                    dict[sender.tab.id] = undefined;
                } else if (request.message === "ping") {
                    console.log("RECEIVE PING!");
                    if(dictping[sender.tab.id] !== undefined) {
                        dictping[sender.tab.id] = true;
                    }
                }
                else {
                    if (dict[sender.tab.id] !== undefined)
                        dict[sender.tab.id].send(request.message);
                }
            }
        });
    intervalId = setInterval(function() {
        for (var key in dictping) {
            // check if the property/key is defined in the object itself, not in parent
            if (dictping.hasOwnProperty(key)) {
                console.log(key, dictping[key]);
                if(dictping[key] === false){
                    if (dict[key] !== undefined){
                        dict[key].close();
                        console.log("IN PING CLOSE");
                        dict[key] = undefined;
                        dictping[key] = undefined;
                        delete dict[key];
                       delete dictping[key];
                    }
                }
                else if(dictping[key] === true){
                    dictping[key] = false;
                }
            }
        }
    }, intervalCount);

}

function tryConnect(sender) {
     if(dict[sender.tab.id] !== undefined){
         console.log("CLOSE SOCKET");
        dict[sender.tab.id].close();
        dict[sender.tab.id] = undefined;
    }
   var backgroundsocket = new WebSocket('ws:127.0.0.1:11520/lazyir/v1');
    backgroundsocket.onclose = function (p1) {
        sendMsg(sender.tab,"Not connected");
        dict[sender.tab.id] = undefined;
        dictping[sender.tab.id] = false;
    };
    backgroundsocket.onopen = function (event) {
        if(sender.tab) {
            if (dict[sender.tab.id] !== undefined) {
                dict[sender.tab.id].close();
                dict[sender.tab.id] = undefined;
            }
            console.log("NEW CONENCTION OPENED");
            dict[sender.tab.id] = backgroundsocket;
            dictping[sender.tab.id] = true;
            sendMsg(sender.tab,"connected");
        }
    };
    backgroundsocket.onerror = function (event) {
        sendMsg(sender.tab, "Not connected");
        if (dict[sender.tab.id] !== undefined) {
         dict[sender.tab.id].close();
       }
        dict[sender.tab.id] = undefined;
        dictping[sender.tab.id] = false;
    };
    backgroundsocket.onmessage = function (p1) {
        sendMsg(sender.tab,p1.data);
        dictping[sender.tab.id] = true;
    };
}

function sendMsg(tab,msg){
    chrome.tabs.get(tab.id,function () {
        if (chrome.runtime.lastError) {
            if (dict[tab.id] !== undefined) {
            dict[tab.id].close();
            dict[tab.id] = undefined;
        }}
    });
        chrome.tabs.sendMessage(tab.id, {resp: msg}, function(response) {
        });
}





