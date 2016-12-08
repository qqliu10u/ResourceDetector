var WWW_TUMBLR_COM_URL_REG = "http://www.tumblr.com/.*";

//探测图片
registerResourceDetectFunction(WWW_TUMBLR_COM_URL_REG, function () {
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

registerIFrameDetectFunction(WWW_TUMBLR_COM_URL_REG, function () {
    //do nothing
});