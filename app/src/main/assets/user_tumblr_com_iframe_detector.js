var USER_TUMBLR_COM_URL_REG = "http://[^www].*.tumblr.com/.*";

//探测合适的iframe
registerIFrameDetectFunction(USER_TUMBLR_COM_URL_REG, function () {
    var iframeArray = new Array;

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

    return iframeArray;
});

//探测资源
registerResourceDetectFunction(USER_TUMBLR_COM_URL_REG, function () {
    //探测图片
	var picElements = document.getElementsByClassName("photoset_photo");
	if(!picElements) {
		log("detectPics() picElements is undefined");
		return;
	}
	log("detectPics() found pic elements count= " + picElements.length);

	for (var i= 0; i < picElements.length; i++) {
		var picSrc = picElements[i].dataset.src;
//		log("detectPics() found pic elements: " + picSrc);

		if(resourceDetectorListener) {
			resourceDetectorListener.onDetected("pic", document.title, picSrc, null, null);
		}
	}

	//探测视频
	detectVideoByVideoTag();
});