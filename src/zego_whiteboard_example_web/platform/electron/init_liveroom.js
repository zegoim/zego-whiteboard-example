/**
 * 开源时注意修改SDK引用路径（相对index.html的路径）
 */

// 引入 SDK
var ZegoLiveRoom = require(zegoSDKConfig.liveroom);
var ZegoWhiteBoard = require(zegoSDKConfig.liveroomWb);
var ZegoExpressDocs = require(zegoSDKConfig.docs);

/**
 * Liveroom 版本 SDK 初始化
 */

var logDir = zegoConfig.logDirs[require('os').platform()];

// 初始化 zegoLiveRoom
var zegoLiveRoom = new ZegoLiveRoom();
zegoLiveRoom.setUseEnv({ use_test_env: !!zegoConfig.whiteboard_env, use_alpha_env: false });
// log_level: 3 通常在发布产品中使用，4 调试阶段使用
zegoLiveRoom.setLogDir({ log_dir: logDir, log_level: 4 });
zegoLiveRoom.initSDK(
    {
        app_id: zegoConfig.appID,
        sign_key: zegoConfig.appSign,
        user_id: zegoConfig.userid,
        user_name: zegoConfig.username
    },
    function(rs) {
        if (rs.error_code !== 0) {
            zegoLiveRoom.unInitSDK();
        }
    }
);

// 初始化 ZegoWhiteboard
var zegoWhiteboard = new ZegoWhiteBoard(zegoLiveRoom);

// 初始化 ZegoDocs
var zegoDocs = new ZegoExpressDocs({
    appID: zegoConfig.appID,
    appSign: zegoConfig.appSign,
    dataFolder: logDir,
    cacheFolder: logDir,
    logFolder: logDir,
    isTestEnv: zegoConfig.isDocTestEnv
});

// 业务数据
var userIDList = [];

function loginRoom() {
    return new Promise((resolve, reject) => {
        const config = {
            room_id: zegoConfig.roomid,
            room_name: zegoConfig.roomid,
            role: 2 // 1: 主播  2: 观众
        };
        zegoLiveRoom.loginRoom(config, (res) => {
            if (res && res.error_code == 0) {
                userIDList.unshift(zegoConfig.userid);
                $('#roomidtext').text(zegoConfig.roomid);
                $('#idNames').html('房间所有用户ID：' + userIDList.toString());
                resolve();
            } else {
                reject(res);
            }
        });
    });
}

function logoutRoom() {
    zegoLiveRoom.logoutRoom(zegoConfig.roomid);
    localStorage.removeItem('zegoConfig');
}
