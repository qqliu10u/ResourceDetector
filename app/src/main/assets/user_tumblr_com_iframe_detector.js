var USER_TUMBLR_COM_URL_REG = "http://[^www].*.tumblr.com/.*";

//通过内容区收集iframe
function detectIframeByContent(iframeArray) {
    var contentSectionArray = document.getElementsByClassName("content clearfix avatar-style-square show-nav");
    log("detect in user.tumblr.com, found content= " + contentSectionArray.length);
    if(contentSectionArray) {
        for(var i = 0; i < contentSectionArray.length; i++) {
            var contentSection = contentSectionArray[i];
            var itemIframes = contentSection.getElementsByTagName("iframe");
            log("detect in user.tumblr.com, found itemIframes= " + itemIframes);
            if(!itemIframes) {
                continue;
            }

            for(var j = 0; j < itemIframes.length; j++) {
                var iframePart = itemIframes[j];
                //统计喜欢不喜欢的标签，此标签内的iframe不需要处理
                if("like_toggle" == iframePart.className) {
                    continue;
                }
                iframeArray.push(iframePart);
            }
        }
    }
}

//通过视频容器区收集iframe
function detectIframeByVideoContainer(iframeArray) {
    var videoContainers = document.getElementsByClassName("tumblr_video_container");
    if(!videoContainers) {
        return;
    }

    for(var i = 0 ; i < videoContainers.length; i++) {
        var itemVideoContainer = videoContainers[i];
        var itemIframes = itemVideoContainer.getElementsByTagName("iframe");
        if(!itemIframes) {
            continue;
        }

        for(var j = 0; j < itemIframes.length; j++) {
            var iframePart = itemIframes[j];
            iframeArray.push(iframePart);
        }
    }
}

//探测合适的iframe
registerIFrameDetectFunction(USER_TUMBLR_COM_URL_REG, function () {
    var iframeArray = new Array;

    //通过内容区定位iframe
    detectIframeByContent(iframeArray);

    //通过视频容器区收集iframe
    detectIframeByVideoContainer(iframeArray);

    return iframeArray;
});

//检测页面内的图集
function detectPicSet() {
    //探测图片
	var picElements = document.getElementsByClassName("photoset_photo");
	if(!picElements) {
		log("detectPicSet() picElements is undefined");
		return;
	}

	log("detectPicSet() found pic elements count= " + picElements.length);

	for (var i= 0; i < picElements.length; i++) {
		var picSrc = picElements[i].dataset.src;

		if(resourceDetectorListener) {
			resourceDetectorListener.onDetected("pic", document.title, picSrc, null, null);
		}
	}
}

//检测页面内只有一张图片的场景
function detectOnePic() {
    var contentSections = document.getElementsByClassName("content clearfix  avatar-style-square  show-nav");
    if(!contentSections) {
        return;
    }

    for(var i = 0;i < contentSections.length; i++) {
        var contentSection = contentSections[i];

        var photoWrapperInnerDivs = contentSection.getElementsByClassName("photo-wrapper-inner");

        if(!photoWrapperInnerDivs) {
            continue;
        }

        for(var j = 0;j < photoWrapperInnerDivs.length; j++) {
            var photoWrapperInnerDiv = photoWrapperInnerDivs[j];

            var imgDivs = photoWrapperInnerDiv.getElementsByTagName("img");

            if(!imgDivs) {
                continue;
            }

            for(var k = 0;k < imgDivs.length; k++) {
                var imgDiv = imgDivs[k];
                var picSrc = imgDiv.src;

                if(resourceDetectorListener) {
                    resourceDetectorListener.onDetected("pic", document.title, picSrc, null, null);
                }
            }
        }
    }
}

//探测资源
registerResourceDetectFunction(USER_TUMBLR_COM_URL_REG, function () {
    //探测图集
    detectPicSet();

	//探测视频
	detectVideoByVideoTag();

	//探测单张图片
	detectOnePic();
});