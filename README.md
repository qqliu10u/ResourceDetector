# ResourceDetector使用方法介绍

如果不需要对其他网站进行资源嗅探，那么下载我的最新版本apk就可以了，我后面会不断优化tumblr内的资源的探测，毕竟目前还是有些资源探测不到，需要调整探测算法。

##1 具体的使用方法
1. 通过点击感兴趣的内容条目上的分享按钮，选择分享对象时选择“资源嗅探器”即可调起工具进行资源嗅探流程，在嗅探过程中，可以返回到上个页面继续浏览。
2. 如果想要自己输入url探测，则在桌面启动应用点击右上角“探测资源”进入探测界面，然后输入url点击开始探测即可。

##2 自己写探测脚本
我们以user.tumblr.com为例，来说明探测脚本的写法：
首先这个页面内既可能是嵌套iframe来展示资源（如展示图集或展示视频），也可能直接展示资源（单个图集和部分视频）。所以这个页面两个嗅探逻辑都需要写。
其次，这个页面需要一个判断是当前网站的一个url正则表达式，而且需要与www.tumblr.com分开，我这里写的是"http://[^www].*.tumblr.com/.*"。
所以这个页面的探测脚本如下：
```javascript
//探测合适的iframe
registerIFrameDetectFunction(USER_TUMBLR_COM_URL_REG, function () {
    var iframeArray = new Array;

    //通过内容区定位iframe
    detectIframeByContent(iframeArray);

    //通过视频容器区收集iframe
    detectIframeByVideoContainer(iframeArray);

    return iframeArray;
});

//探测资源
registerResourceDetectFunction(USER_TUMBLR_COM_URL_REG, function () {
    //探测图集
    detectPicSet();

	//探测视频
	detectVideoByVideoTag();

	//探测单张图片
	detectOnePic();
});
```

因为这个url对应的页面结构比较复杂，所以我们需要多种iframe的探测方法（代码中的注册iframe检测registerIFrameDetectFunction需要注册两种iframe的探测方法，注册资源检测registerResourceDetectFunction需要完成三种资源检测步骤）。但总体逻辑都是相同的，使用者需要针对指定页面完成三步：
>第一步，确定url的正则表达式，以字符串参数形式传递作为registerIFrameDetectFunction和registerResourceDetectFunction第一个参数。
>第二步，根据页面的Dom树的className和id完成iframe的过滤，脚本函数作为registerIFrameDetectFunction的第二个参数注册。
>第三步，根据页面的Dom树的className和id完成资源的过滤，包括视频和图片两部分，脚本函数作为registerResourceDetectFunction第三个参数注册。

三步写完，一个新网址的资源探测过程脚本就编写完成了，将文件存为*.js，放入/sdcard/ResourceDetector/detect_code/目录下，**重启应用**即可生效。

使用方法很简单吧，想看具体的设计思路与细节：请参考：http://blog.csdn.net/u013478336/article/details/53539526