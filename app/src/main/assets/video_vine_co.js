var VINE_CO_URL_REG = "https://vine.co/.*";

//此页面不需要检测iframe
registerIFrameDetectFunction(VINE_CO_URL_REG, function() {
    //空实现，因为这个页面不需要检测iframe
});

//注册视频检测的脚本
registerResourceDetectFunction(VINE_CO_URL_REG, function() {
	var playBtnElements = document.getElementsByClassName("play-button");
	if(!playBtnElements) {
		log("triggerVideoStart() playBtnElements is undefined");
		return;
	}
	log("triggerVideoStart() found play btn elements count= " + playBtnElements.length);

	for(var i = 0;i < playBtnElements.length; i++) {
		playBtnElements[i].click();
	}

	setTimeout(function() {
		detectVideoByVideoTag(document);
	}, 1000);

	return 1000;
});