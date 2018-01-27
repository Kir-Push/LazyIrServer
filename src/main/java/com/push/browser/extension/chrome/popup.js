
var MyJasechvideo;
var MultipleVideos;
var isSingleVideo;
var JAsechCount = 0;
var jasechResponse = false;
var jasechInterval = 5000; // 1000 is one second;
var workthroughbackhround = false;;
var backgroundFailed;
var jasechIntervalId;
var pingId;
var pingInterval = 1000;
var setted;

function setCheckServerInterval() {
    if(jasechResponse === true)
        return;

    clearInterval(jasechIntervalId);
    jasechIntervalId = setInterval(function() {
        if(workthroughbackhround !== true)
        checkServer();
        else{
            clearInterval(pingId);
            pingId = setInterval(function() {
                chrome.runtime.sendMessage({message: "ping"},function (response) {
                });
            },pingInterval);
        }
    }, jasechInterval);
}

var onopen = function() {

    jasechResponse = true;
    workthroughbackhround = false;
};

var onclose = function(event) {

    jasechResponse = false;
    jasechsocket.close();
    jasechsocket = undefined;
};

var onerror = function(error) {
    if(jasechsocket.readyState === 3 && backgroundFailed !== true){
        jasechResponse = false;
       // clearInterval(jasechIntervalId);
       // clearInterval(pingId);
        setted = false;
    
        chrome.runtime.onMessage.addListener(
            function(request, sender, sendResponse) {
                if(!sender.tab){
                    if(request.resp === "connected"){
                         workthroughbackhround = true;
                    }else if(request.resp === "Not connected"){
                        workthroughbackhround = false;
                        backgroundFailed = true;
                        jasechResponse = false;
                     //   setCheckServerInterval();
                      //  clearInterval(pingId);
                    }else
                    parseResponse(request.resp);
                }
            });
         sendMessage("try connect");
    }
};

var onmg = function(event) {
    var incomingMessage = event.data;
    parseResponse(incomingMessage);
};

var sendMessage = function (msg) {
    console.log(workthroughbackhround + "  " + jasechResponse + "    " + jasechsocket);
    if(workthroughbackhround !== true && jasechResponse === true && jasechsocket !== undefined) {
        jasechsocket.send(msg);
    }
    else{
     chrome.runtime.sendMessage({message: msg},function (response) {
     });
    }
};


var jasechsocket;

var connectingPeople = function () {

    if(!jasechResponse) {
        jasechsocket = new WebSocket('ws:127.0.0.1:11520/lazyir/v1');
        jasechsocket.onclose = onclose;
        jasechsocket.onopen = onopen;
        jasechsocket.onerror = onerror;
        jasechsocket.onmessage = onmg;
    }
};

function checkServer() {
    var tempMultipleVideos = document.getElementsByTagName("video");
    var tempMultipleAudios = document.getElementsByTagName("audio");

    if(tempMultipleVideos.length === 1){
        isSingleVideo = true;
    }
    MultipleVideos = [];
    var arrayLength = tempMultipleVideos.length;
    for (var i = 0; i < arrayLength; i++) {
       if(tempMultipleVideos[i].currentSrc !== "" &&  tempMultipleVideos[i].currentSrc !== "undefined" && tempMultipleVideos[i].currentSrc !== "null"){
           MultipleVideos.push(tempMultipleVideos[i]);
       }
    }

    arrayLength = tempMultipleAudios.length;
    for (var i = 0; i < arrayLength; i++) {
        if(tempMultipleAudios[i].currentSrc !== "" &&  tempMultipleAudios[i].currentSrc !== "undefined" && tempMultipleAudios[i].currentSrc !== "null"){
            MultipleVideos.push(tempMultipleAudios[i]);
        }
    }
    if(MultipleVideos.length === 0)
        return;

    if(MyJasechvideo === undefined) {
        MyJasechvideo = MultipleVideos[0];
    }

    if(MyJasechvideo !== undefined)
    {
        connectingPeople();
    }
}


init();
//$(youtubePageChange);

function init() {
    checkServer();
    setCheckServerInterval();
}

function parseResponse(data)
 {
     console.log(data);
     var json = JSON.parse(data);

     console.log(json);
     if(json.multipleVids === "true"){
       var locId = json.lazyIrId;
         var arrayLength = MultipleVideos.length;
         for (var i = 0; i < arrayLength; i++) {
             console.log("======");
             console.log(MultipleVideos[i].lazyIrId);
             console.log(locId);
             console.log("======");
             if( MultipleVideos[i].lazyIrId == locId){
                 MyJasechvideo  = MultipleVideos[i];
                 console.log("!!!" + MyJasechvideo.lazyIrId);
                 break;
             }
         }
     }

     if(json.command === "pause")
     {
         pause();
     }
     else if(json.command === "play")
     {
         play();
     }
     else if(json.command === "playPause")
     {
         playPause();
     }
     else if(json.command === "setTime")
     {
         setTime(json.time);
     }
     else if(json.command === "setVolume")
     {
         setVolume(json.volume);
     }
     else if(json.command === "getInfo")
     {
         sendInfo();
     }
     else if(json.command === "next")
     {
         sendNext();
     }
     else if(json.command === "loop")
     {
         loop();
     }
 }

function loop() {
    if (typeof MyJasechvideo.loop == 'boolean') { // loop supported
        MyJasechvideo.loop = true;
    } else { // loop property not supported
        MyJasechvideo.addEventListener('ended', function () {
            this.currentTime = 0;
            this.play();
        }, false);
    }
}


 function sendStatus() {

 }


 function sendDuration() {

 }


 function sendTime() {

 }

function playPause() {
   var status = getStatus();
   if(status === "playing")
   {
       pause();
   }
   else
   {
       play();
   }
}


function sendNext() {
    MyJasechvideo.currentTime = getDuration();

}

 function sendInfo() {

     var arrayLength = MultipleVideos.length;
    if(arrayLength === 1) {
        if(MyJasechvideo.lazyIrId === undefined || MyJasechvideo.lazyIrId === null || MyJasechvideo.lazyIrId === 0)
        MyJasechvideo.lazyIrId = Math.random() * (999999 - 1) + 1;
        var obj = {
            "type": "getInfo",
            "title": getTitle(),
            "status": getStatus(),
            "time": getTime(),
            "duration": getDuration(),
            "volume": getVolume(),
            "url": getPageUrl(),
            "videoSrc": getVideoSrc(),
            "localId": MyJasechvideo.lazyIrId
        };
    }else{
        var obj = {
            "type": "getInfoMultiple",
            "numberOfVideos":arrayLength
        }
        for (var i = 0; i < arrayLength; i++) {
            MyJasechvideo = MultipleVideos[i];
            if(MyJasechvideo.lazyIrId === undefined || MyJasechvideo.lazyIrId === null || MyJasechvideo.lazyIrId === 0)
            MyJasechvideo.lazyIrId = Math.random() * (999999 - 1) + 1;
            obj["localId"+i] = MyJasechvideo.lazyIrId;
            obj["title"+i] = getTitle();
            obj["status"+i] = getStatus();
            obj["time"+i] = getTime();
            obj["duration"+i] = getDuration();
            obj["volume"+i] = getVolume();
            obj["url"+i] = getPageUrl();
            obj["videoSrc"+i] = getVideoSrc();
        }
        MyJasechvideo = MultipleVideos[0];
    }
     var myJSON = JSON.stringify(obj);
     sendMessage(myJSON);
 }

 function getVideoSrc() {
     if(!MyJasechvideo.currentSrc.startsWith("blob:") && !MyJasechvideo.currentSrc.startsWith("\"blob:\"") && MyJasechvideo.currentSrc !== "" &&  MyJasechvideo.currentSrc !== "undefined" && MyJasechvideo.currentSrc !== "null"){
         return MyJasechvideo.currentSrc;
     }
     else if(MyJasechvideo.src !== undefined && !MyJasechvideo.src.startsWith("blob:") && !MyJasechvideo.src.startsWith("\"blob:\"" && MyJasechvideo.src !== "" &&  MyJasechvideo.src !== "undefined" && MyJasechvideo.src !== null )){
         return MyJasechvideo.src;
     } else{
         return getPageUrl();
     }
 }

 function getPageUrl() {
     return MyJasechvideo.baseURI;
 }

function sendTitle() {
  //  document.title;
}

 function sendVolume() {

 }

 function getTitle() {
     return document.title;
 }

 function getStatus() {
    if(MyJasechvideo.paused)
        return "paused";
    else
        return "playing";
 }

 function getVolume() {
     return MyJasechvideo.volume;
 }

function getDuration()
{
    return MyJasechvideo.duration;
}

function setTime(sec)
{
    MyJasechvideo.currentTime = sec;
}

function setVolume(vol) {
  MyJasechvideo.volume = vol;
}


function pause() {
    MyJasechvideo.pause();
}

function play() {
    MyJasechvideo.play();
}

function getTime() {
    return MyJasechvideo.currentTime;
}




function youtubePageChange()
{
    $('body').on('transitionend', function(event)
    {
        if (event.target.id !== 'progress') return false;
        init();
    });
}

