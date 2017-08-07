
var MyJasechvideo;
var JAsechCount = 0;
var jasechResponse = false;
var jasechInterval = 10000; // 1000 is one second;
var jasechIntervalId;

function setCheckServerInterval() {
    if(jasechResponse === true)
        return;
    jasechIntervalId = setInterval(function() {
        checkServer();
    }, jasechInterval);
}

var onopen = function() {
    jasechResponse = true;
    // sendMessage("hello my dear friend")// test
};

var onclose = function(event) {
    if (event.wasClean) {
        //todo
    } else {
        // например, "убит" процесс сервера
    }
    jasechResponse = false;
    jasechsocket = undefined;
};

var onerror = function(error) {
    //todo repsone false need or not?
    //  alert("Ошибка " + error.message);
};

var onmg = function(event) {
    var incomingMessage = event.data;
    parseResponse(incomingMessage);
};

var sendMessage = function (msg) {
    jasechsocket.send(msg);
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
    if(MyJasechvideo === undefined) {
        MyJasechvideo = document.getElementsByTagName("video")[0];
        if(MyJasechvideo === undefined)
        {
            MyJasechvideo = document.getElementsByTagName("audio")[0];
        }
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
    var json = JSON.parse(data);
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
        //todo
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
    { //todo
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
    var obj = {"type":"getInfo","title":getTitle(),"status":getStatus(),"time":getTime(),"duration":getDuration(),"volume":getVolume()};
    var myJSON = JSON.stringify(obj);
    sendMessage(myJSON);
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

