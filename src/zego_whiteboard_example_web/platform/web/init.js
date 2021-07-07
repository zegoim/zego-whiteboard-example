/*
 * @Description: 开发环境相关配置
{
    roomid: "",
    username: "",

    whiteboard_env: "-test,-alpha",
    docs_env: "test,",
    pptStepMode: "1",
    thumbnailMode: "1,2,3",
    fontFamily: "ZgFont"
}
*/

// 环境设置
$('#env-btn').click(function() {
    $('#reg-log').prop('checked', false);
});
// 登录
$('#login').click(function() {
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
        pptStepMode: $('#pptStepMode').val(),
        thumbnailMode: $('#thumbnailMode').val(),
        roomid: roomid,
        username: username
    };
    sessionStorage.setItem('zegoConfig', JSON.stringify(conf));
    initZegoConfig();
});

/**
 * 开源代码时需要改动这里的配置，引入SDK的相对路径（相对index.html的路径）
 */
var zegoSDKPathList = ['./platform/web/ZegoExpressWhiteboardWeb.js', './platform/web/ZegoExpressDocsWeb.js'];
initZegoConfig();

function initZegoConfig() {
    zegoConfig = JSON.parse(sessionStorage.getItem('zegoConfig'));
    if (zegoConfig) {
        /**
         * 开源代码时注意屏蔽账号相关信息
         */
        var appID = 0;
        var fileListUrl = '';

        Object.assign(zegoConfig, {
            appID: appID,
            server: `wss://webliveroom${zegoConfig.whiteboard_env || appID + '-api'}.zego.im/ws`,
            userid: createUserID(),
            isDocTestEnv: !!zegoConfig.docs_env,
            isTouch: 'ontouchstart' in document.body,
            fileListUrl: fileListUrl
        });
        loadScript('./platform/web/demo.js').then(function() {
            loadAllScript(zegoSDKPathList);
        });
        $('.login_container').css('display', 'none');
        $('.whiteboard_container').css('display', 'block');
    } else {
        loadScript('./platform/web/demo.js');
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
    var tasks = sdkPathList.map(function(path) {
        return loadScript(path);
    });
    if (zegoConfig.isTouch) {
        tasks.unshift(
            loadScript('./lib/vconsole.js').then(function() {
                new VConsole();
            })
        );
    }
    Promise.all(tasks).then(function() {
        loadScript('./biz.js');
    });
}

// SDK 初始化
var zegoWhiteboard;
var zegoDocs;
var userIDList = [];

function loginRoom() {
    return new Promise(function(resolve) {
        // 获取 token
        var tokenUrl = 'https://wsliveroom-alpha.zego.im:8282/token';
        getToken(zegoConfig.appID, zegoConfig.userid, tokenUrl).then(function(token) {
            initSDK(token);
            onRoomUserUpdate();
            resolve();
        });

        /**
         * 开源代码时注意屏蔽账号相关信息，这里仅演示获取 token 的示例代码
         *
         * @param tokenUrl 仅在测试环境生效，正式环境请调用自己的后台接口获取 token（生成方法请参考 ZEGO 开发者中心）
         */
        function getToken(appID, userID, tokenUrl) {
            return new Promise(function(resolve) {
                $.get(
                    tokenUrl,
                    {
                        app_id: appID,
                        id_name: userID
                    },
                    function(token) {
                        if (token) {
                            resolve(token);
                        }
                    },
                    'text'
                );
            });
        }

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
                    token,
                    {
                        userID,
                        userName: zegoConfig.username
                    },
                    {
                        maxMemberCount: 10,
                        userUpdate: true
                    }
                )
                .then(function() {
                    userIDList.unshift(userID);
                    $('#roomidtext').text(zegoConfig.roomid);
                    $('#idNames').html('房间所有用户ID：' + userIDList.toString());
                });
        }

        function onRoomUserUpdate() {
            zegoWhiteboard.on('roomUserUpdate', function(roomID, type, list) {
                if (type == 'ADD') {
                    list.forEach(function(v) {
                        userIDList.push(v.userID);
                    });
                } else if (type == 'DELETE') {
                    list.forEach(function(v) {
                        var id = v.userID;
                        var index = userIDList.findIndex(function(item) {
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
