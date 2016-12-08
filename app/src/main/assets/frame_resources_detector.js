function log(text) {
    //记录日志
	console.log("resdetector_ " + text + " , from: " + document.URL);
}

//判断是否函数
function isFunc(func) {
    if(!func) {
        return false;
    }

	return typeof func == 'function';
}

//根据video标签检测视频
function detectVideoByVideoTag() {
	var videoElements = document.getElementsByTagName("video");
	if(!videoElements) {
		log("detectVideoByVideoTag() | videoElements is undefined");
		return;
	}

	log("detectVideoByVideoTag() | found video elements count= " + videoElements.length);

	for(var i = 0;i < videoElements.length; i++) {
		var videoSrc = videoElements[i].src;
		if(videoSrc) {
		    log("detectVideoByVideoTag() | found video elements: " + videoSrc);

            if(resourceDetectorListener) {
        	    resourceDetectorListener.onDetected(
        	    "video",
        	    document.title,
        	    videoSrc,
        	    null,
        	    null);
            }
		    continue;
		}

		var sourceElements = videoElements[i].getElementsByTagName("source");
        for(var j = 0; j < sourceElements.length; j++) {
        	var itemSourceElements = sourceElements[j];
        	if(resourceDetectorListener) {
                resourceDetectorListener.onDetected(
                    "video",
                    document.title,
                    itemSourceElements.src,
                    itemSourceElements.media,
                    itemSourceElements.type);
            }
        }
	}
}

//根据Img标签检测图片
function detectPicByImgTag() {
	var picElements = document.getElementsByTagName("img");
	if(!picElements) {
		log("detectPicByImgTag() | picElements is undefined");
		return;
	}

	log("detectPicByImgTag() | found img elements count= " + picElements.length);

	for(var i = 0; i < picElements.length; i++) {
		var picSrc = picElements[i].src;
		if(!picSrc) {
		    continue;
		}

        if(resourceDetectorListener) {
        	resourceDetectorListener.onDetected(
        	"pic",
        	document.title,
        	picSrc,
        	null,
        	null);
        }
	}
}

//默认的资源检测函数
function defaultResourceDetect() {
    //检测图片
    detectPicByImgTag();
    //检测视频
    detectVideoByVideoTag();
}

//默认的iframe检测函数
function defaultIFrameDetect() {
    return document.getElementsByTagName("iframe");
}

//存储所有页面资源函数集合
var mResourceDetectFuncMap = {};

//存储所有页面iframe检测函数集合
var mIframeDetectFuncMap = {};

//注册单个页面的资源检测函数
function registerResourceDetectFunction(url, resDetectFunc) {
	if(!url || !isFunc(resDetectFunc)) {
        log("registerResourceDetectFunction()| param is illegal");
        return;
    }

//    log("registerResourceDetectFunction()| url= " + url + " resDetectFunc= " + resDetectFunc);
    mResourceDetectFuncMap[url] = resDetectFunc;
}

//注册单个页面的iframe检测函数
function registerIFrameDetectFunction(url, iframeDetectFunc) {
    if(!url || !isFunc(iframeDetectFunc)) {
        log("registerIFrameDetectFunction()| param is illegal");
        return;
    }

//    log("registerIFrameDetectFunction()| url= " + url + " iframeDetectFunc= " + iframeDetectFunc);
    mIframeDetectFuncMap[url] = iframeDetectFunc;
}

//获取当前页面的资源检测函数
function getResourceDetectFunctions() {
    var url = document.URL;

    //使用map的key对url做正则匹配
    var resDetectFunArray = new Array;
    for(var key in mResourceDetectFuncMap) {
        var keyReg = new RegExp(key);
        if(keyReg.test(url)) {
            log("getResourceDetectFunctions()| key= " + key);
            var detectFunc = mResourceDetectFuncMap[key];
            log("getResourceDetectFunctions()| detectFunc= " + detectFunc);
            if(!detectFunc) {
                continue;
            }

            resDetectFunArray.push(detectFunc);
        }
    }

    //没有注册的异常处理
    if(resDetectFunArray.length == 0) {
        resDetectFunArray.push(defaultResourceDetect);
    }

    return resDetectFunArray;
}

//获取当前页面iframe的检测函数
function getIFrameDetectFunctions() {
    var url = document.URL;

    //使用map的key对url做正则匹配
    var iframeDetectFunArray = new Array;
    for(var key in mIframeDetectFuncMap) {
        var keyReg = new RegExp(key);
        if(keyReg.test(url)) {
            log("getIFrameDetectFunctions()| key= " + key);
            var detectFunc = mIframeDetectFuncMap[key];
            log("getIFrameDetectFunctions()| detectFunc= " + detectFunc);
            if(!detectFunc) {
                continue;
            }

            iframeDetectFunArray.push(detectFunc);
        }
    }

    if(iframeDetectFunArray.length == 0) {
        iframeDetectFunArray.push(defaultIFrameDetect);
    }

    return iframeDetectFunArray;
}

//检测当前界面的资源，并通知到java层
function detectResourcesInPage() {
	log("detectResourcesInPage() | detect all resource begin");

    //获取当前页面对应的所有匹配的资源检测函数，一一执行
	var resDetectFunctionArray = getResourceDetectFunctions();
	var maxTimeWait = 0;
	for(var i = 0; i < resDetectFunctionArray.length; i++) {
        var waitTime = resDetectFunctionArray[i]();
        if(waitTime && maxTimeWait < waitTime) {
            maxTimeWait = waitTime;
        }
	}

	return maxTimeWait;
}

//检测当前界面的iframe，并通知到java层
function detectIframeInPage() {
	log("detectIframeInPage() | detect all iframe begin");
	//detect all iframe
	var iframeTagList = new Array;

	var iframeDetectFunctionArray = getIFrameDetectFunctions();
	for(var i = 0; i < iframeDetectFunctionArray.length; i++) {
	    var itemIframeTagList = iframeDetectFunctionArray[i]();
	    if(!itemIframeTagList) {
	        continue;
	    }

	    for(var j = 0 ; j < itemIframeTagList.length; j++) {
	        iframeTagList.push(itemIframeTagList[i]);
	    }
	}

	if(iframeTagList.length <= 0) {
		log("detectIframeInPage() | no iframe is found");
		return;
	}

	log("detectIframeInPage() | found iframes count= " + iframeTagList.length);

	for(var i = 0;i < iframeTagList.length;i++) {
		if(resourceDetectorListener) {
			resourceDetectorListener.openUrlForDetect(iframeTagList[i].src);
		}
	}
}

function doDetectAction() {
    //step1：判断当前页面是http或https的地址，区别about:blank
	var urlReg = /(http|https):.*/;
	if(!urlReg.test(document.URL)) {
	    log("doDetectAction() | current page url not matches");
	    setTimeOut("doDetectAction()", 1 * 1000);
	    return;
	}

    //step2: 检测当前界面的资源
    var maxTimeWait = detectResourcesInPage();

    //step3: 检测当前界面合适的iframe
    detectIframeInPage();

    return maxTimeWait;
}

//此处添加注册嗅探器的代码
//##%s##

function notifyDetectComplete() {
    if(resourceDetectorListener) {
        resourceDetectorListener.onDetectCompleted();
    }
}

log("onInit() | begin detecting action");
var maxTimeWait = doDetectAction();

if(maxTimeWait) {
    log("onInit() | wait " + maxTimeWait + " for detection");
    setTimeout("notifyDetectComplete()", maxTimeWait + 500)
} else {
    notifyDetectComplete();
}
