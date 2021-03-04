# 简介

ZegoWhiteboardExample 是集成即构[互动白板SDK](https://doc-zh.zego.im/zh/4396.html)和[文件共享SDK](https://doc-zh.zego.im/zh/4400.html)的功能示例项目，开发者可通过该项目快速了解即构白板文件的功能和集成方式。
即构互动白板服务（ZegoWhiteboardView）和即构文件共享服务（ZegoDocsView），基于即构亿级海量用户的实时信令网络构建，支持在白板画布上实时绘制涂鸦并多端同步，同时提供图形、激光笔等工具，满足不同场景的在线协同需求；同时提供文件转换和点播相关功能，支持将常见文件格式转码为向量、PNG、PDF、HTML5 页面等便于跨平台点播的目标格式。


# 快速开始

### 环境准备：

* Xcode 7.0 或以上版本 
* iOS 9.0 或以上版本且⽀持⾳视频的 iOS 真机
* iOS 设备已经连接到 Internet
* 在执行以下步骤之前，请确保已安装 CocoaPods。安装 CocoaPods 的方法以及常见问题可参考 [CocoaPods 常见问题：安装 CocoaPods](https://doc-zh.zego.im/zh/1253.html)

### 前提条件
请在 [即构管理控制台](https://console.zego.im/acount) 申请 SDK 初始化需要的 AppID 和 AppSign，申请流程请参考 ([项目管理](https://doc-zh.zego.im/zh/1265.html))

### 运行示例代码
1. 使用 Xcode 打开 ZegoWhiteboardExample.xcworkspace。进入工程后，找到 ZegoWhiteboardEnvManager.m 文件，在文件中填写好 AppID 和 AppSign。
2. 切换到 Podfile 文件所在目录，执行 pod repo update 更新本地依赖库索引。这一步可能会需要比较长的时间，视网络情况而定，大约耗时在 3 min ~ 20 min。
3. 执行 pod install。等待所有依赖库加载完毕。
4. 运行 ZegoWhiteboardExample 工程。


# 获取帮助

ZEGO 文档中心有关于 [下载示例源码](https://doc-zh.zego.im/zh/6547.html) 相关介绍。



# 作出贡献
如果您发现了文档中有表述错误，或者代码发现了 BUG，或者希望开发新的特性，或者希望提建议，可以[创建一个 Issue]()。请参考 Issue 模板中对应的指导信息来完善 Issue 的内容，来帮助我们更好地理解您的 Issue。


# FAQ



# LICENSE