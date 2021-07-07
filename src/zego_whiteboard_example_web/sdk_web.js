// 环境设置
$('#env-btn').click(function () {
    $('#reg-log').prop('checked', false);
});
// 登录
$('#login').click(function () {
    var username = $('#username').val();
    var roomid = $('#roomid').val();

    if (!username || !roomid) {
        alert('请输入用户名和roomID');
        return;
    }

    var conf = {
        whiteboard_ver: $('#whiteboard_ver').val(),
        docs_ver: $('#docs_ver').val(),
        whiteboard_env: $('#whiteboard_env').val(),
        docs_env: $('#docs_env').val(),
        fontFamily: $('#fontFamily').val(),
        dynamicPPT_HD: $('#dynamicPPT_HD').val(),
        pptStepMode: $('#pptStepMode').val(),
        thumbnailMode: $('#thumbnailMode').val(),
        roomid: roomid,
        username: username
    };
    sessionStorage.setItem('zegoConfig', JSON.stringify(conf));
    initZegoConfig();
});

initZegoConfig();

function initZegoConfig() {
    zegoConfig = JSON.parse(sessionStorage.getItem('zegoConfig'));
    if (zegoConfig) {
        var appID = 0
        Object.assign(zegoConfig, {
            appID: appID,
            server: `wss://webliveroom${zegoConfig.whiteboard_env || appID+'-api'}.zego.im/ws`,
            userid: createUserID(),
            isDocTestEnv: !!zegoConfig.docs_env,
            // ** tokenUrl 仅在测试环境生效，正式环境请调用自己的后台接口，token 生成方法请参考 ZEGO 开发者中心 **
            tokenUrl: 'https://wsliveroom-alpha.zego.im:8282/token',
            isTouch: 'ontouchstart' in document.body
        });
        $('.login_container').css('display', 'none');
        $('.whiteboard_container').css('display', 'block');
    } else {
        $('.whiteboard_container').css('display', 'none');
        $('.login_container').css('display', 'block');
    }
}



function createUserID() {
    var userID = sessionStorage.getItem('zegouid') || 'web' + new Date().getTime();
    sessionStorage.setItem('zegouid', userID);
    return userID;
}

// SDK 初始化
var zegoWhiteboard;
var zegoDocs;
var userIDList = [];

function loginRoom() {
    return new Promise((resolve) => {
        $.get(
            zegoConfig.tokenUrl, {
                app_id: zegoConfig.appID,
                id_name: zegoConfig.userid
            },
            function (token) {
                if (token) {
                    initSDK(token);
                    onRoomUserUpdate();
                    resolve();
                }
            },
            'text'
        );

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
                isTestEnv: zegoConfig.isDocTestEnv
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
                .then(() => {
                    userIDList.unshift(userID);
                    $('#roomidtext').text(`房间：${zegoConfig.roomid}`);
                    $('#idNames').html('房间所有用户ID：' + userIDList.toString());
                });
        }

        function onRoomUserUpdate() {
            zegoWhiteboard.on('roomUserUpdate', (roomID, type, list) => {
                if (type == 'ADD') {
                    list.forEach((v) => userIDList.push(v.userID));
                } else if (type == 'DELETE') {
                    list.forEach((v) => {
                        var id = v.userID;
                        var index = userIDList.findIndex((item) => id == item);
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