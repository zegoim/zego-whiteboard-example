
# 介绍
ZegoWhiteboardExample 是集成即构[互动白板SDK](https://doc-zh.zego.im/zh/4394.html)和[文件共享SDK](https://doc-zh.zego.im/zh/4398.html)的功能示例项目，开发者可通过该项目快速了解即构白板文件的功能和集成方式。
即构互动白板（ZegoWhiteboardView）和即构文件共享（ZegoDocsView），基于即构亿级海量用户的实时信令网络构建，支持在白板画布上实时绘制涂鸦并多端同步，同时提供图形、激光笔等工具，满足不同场景的在线协同需求；同时提供文件转换和点播相关功能，支持将常见文件格式转码为向量、PNG、PDF、HTML5 页面等便于跨平台点播的目标格式。

# 开发准备

## 申请 AppID
请在 [即构管理控制台](https://console.zego.im/acount) 申请 SDK 初始化需要的 AppID 。

## server
为接入服务器地址，请登录[即构管理控制台](https://console.zego.im/acount)，在对应项目下单击 “配置”，弹出基本信息后单击 “环境配置” 下的 “查看” 按钮，在弹窗中依次选择 “集成的SDK” 和 “Web” 平台便可获取对应的 server 地址

## token

token 生成方法请参考 [ZEGO开发者中心](https://doc-zh.zego.im/zh/7646.html)。


## 注意事项

为了让开发者前期能快速体验功能效果，demo_config.js 文件中相关配置中的获取 token 的链接仅在测试环境生效。


# 启动

填写 demo_config.js 文件中相关配置中的 AppID，打开 login.html 页面输入用户名和房间号，进入房间后即可体验。


# 更多
请访问 [即构开发者中心](https://doc-zh.zego.im/?fromold=1)

