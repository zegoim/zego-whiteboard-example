var zegoRoomInfo = JSON.parse(sessionStorage.getItem('zegoRoomInfo'))
var zegoEnv = JSON.parse(sessionStorage.getItem('zegoEnv'))
var appID = 0
var isDocTestEnv = zegoEnv.docs_env == 1 ? true : false
var server = zegoEnv.whiteboard_env == 1 ? `wss://webliveroom${appID}-api.zego.im/ws` : `wss://webliveroom-test.zego.im/ws`
// 仅在测试环境生效，正式环境请调用自己的后台接口，token 生成方法请参考 ZEGO 开发者中心
var tokenUrl = 'https://wsliveroom-alpha.zego.im:8282/token';