/*
 * @Description: 开发环境相关配置
{
    roomid: "",
    username: "",

    whiteboard_env: "-test",
    docs_env: "test",
    fontFamily: "ZgFont",
    thumbnailMode: "1",
    pptStepMode: "1",
}
*/

// APP 账号
var _openConfig = {
    appID: 0, // 请填写自己申请的 APPID
    token: '', // 需要用于验证身份的 Token，获取方式请参考 用户权限控制[https://doc-zh.zego.im/article/7646]。如需快速调试，可使用控制台生成临时 Token。
    fileListUrl: '',
    // 引入SDK的相对路径（相对index.html的路径）
    SDKPathList: ['./sdk/ZegoExpressWhiteboardWeb.js', './sdk/ZegoExpressDocsWeb.js']
};

// 环境设置
$('#env-btn').click(function () {
    $('#reg-log').prop('checked', false);
});
// 登录
$('#login').click(function () {
    var username = $('#username').val();
    var roomid = $('#roxiugaomid').val();

    if (!username || !roomid) {
        alert('请输入用户名和roomID');
        return;
    }

    var conf = {
        roomid: roomid,
        username: username,
        whiteboard_env: $('#whiteboard_env').val(),
        docs_env: $('#docs_env').val(),
        fontFamily: $('#fontFamily').val(),
        thumbnailMode: $('#thumbnailMode').val(),
        pptStepMode: $('#pptStepMode').val()
    };
    sessionStorage.setItem('zegoConfig', JSON.stringify(conf));
    initZegoConfig();
});

function initZegoConfig() {
    zegoConfig = JSON.parse(sessionStorage.getItem('zegoConfig'));
    if (zegoConfig) {
        if (!_openConfig.appID) {
            alert('请填写 appID');
            return;
        }
        Object.assign(zegoConfig, {
            appID: _openConfig.appID,
            server: `wss://webliveroom${zegoConfig.whiteboard_env || _openConfig.appID + '-api'}.zego.im/ws`,
            userid: createUserID(),
            isTouch: 'ontouchstart' in window,
            fileListUrl: _openConfig.fileListUrl
        });
        loadScript('./sdk/demo.js').then(function () {
            loadAllScript(_openConfig.SDKPathList);
        });
        $('.login_container').css('display', 'none');
        $('.whiteboard_container').css('display', 'block');
    } else {
        loadScript('./sdk/demo.js');
        $('.whiteboard_container').css('display', 'none');
        $('.login_container').css('display', 'block');
    }
}

function createUserID() {
    var userID = sessionStorage.getItem('zegouid') || 'web' + new Date().getTime();
    sessionStorage.setItem('zegouid', userID);
    return userID;
}

function loadAllScript(sdkPathList) {
    var tasks = sdkPathList.map(function (path) {
        return loadScript(path);
    });
    if (zegoConfig.isTouch) {
        tasks.unshift(
            loadScript('./lib/vconsole.js').then(function () {
                new VConsole();
            })
        );
    }
    Promise.all(tasks).then(function () {
        loadScript('./biz.js');
    });
}

// SDK 初始化
var zegoWhiteboard;
var zegoDocs;
var userIDList = [];

function loginRoom() {
    return new Promise(async function (resolve) {
        initSDK(_openConfig.token);
        onRoomUserUpdate();
        resolve();

        function initSDK(token) {
            var userID = zegoConfig.userid;
            // 互动白板
            zegoWhiteboard = new ZegoExpressEngine(zegoConfig.appID, zegoConfig.server);
            zegoWhiteboard.setLogConfig({
                logLevel: 'info'
            });
            zegoWhiteboard.setDebugVerbose(false);
            // 文件转码
            zegoDocs = new ZegoExpressDocs({
                appID: zegoConfig.appID,
                userID,
                token,
                isTestEnv: !!zegoConfig.docs_env
            });

            zegoWhiteboard
                .loginRoom(
                    zegoConfig.roomid,
                    token, {
                        userID,
                        userName: zegoConfig.username
                    }, {
                        maxMemberCount: 10,
                        userUpdate: true
                    }
                )
                .then(function () {
                    userIDList.unshift(userID);
                    $('#roomidtext').text(zegoConfig.roomid);
                    $('#idNames').html('房间所有用户ID：' + userIDList.toString());
                });
        }

        function onRoomUserUpdate() {
            zegoWhiteboard.on('roomUserUpdate', function (roomID, type, list) {
                if (type == 'ADD') {
                    list.forEach(function (v) {
                        userIDList.push(v.userID);
                    });
                } else if (type == 'DELETE') {
                    list.forEach(function (v) {
                        var id = v.userID;
                        var index = userIDList.findIndex(function (item) {
                            return id == item;
                        });
                        if (index != -1) {
                            userIDList.splice(index, 1);
                        }
                    });
                }
                $('#idNames').html('房间所有用户ID：' + userIDList.toString());
            });
        }
    });
}

function logoutRoom() {
    zegoWhiteboard.logoutRoom(zegoConfig.roomid);
    sessionStorage.removeItem('zegoConfig');
}

initZegoConfig();