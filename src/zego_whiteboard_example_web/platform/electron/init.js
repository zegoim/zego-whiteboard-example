/*
 * @Description: 开发环境相关配置
{
    roomid: "",
    username: "",

    whiteboard_env: "-test",
    docs_env: "test",
    sdk_type: "express|liveroom",
    pptStepMode: "1",
    thumbnailMode: "1",
    fontFamily: "ZgFont"
}
*/

$('.web_input_file').css('display', 'none');
$('.ele_btn_file').css('display', 'block');
$('.ele_btns_cache').css('display', 'block');

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
        whiteboard_env: $('#whiteboard_env').val(),
        docs_env: $('#docs_env').val(),
        sdk_type: $('#sdk_type').val(),
        fontFamily: $('#fontFamily').val(),
        pptStepMode: $('#pptStepMode').val(),
        thumbnailMode: $('#thumbnailMode').val(),
        roomid: roomid,
        username: username
    };
    localStorage.setItem('zegoConfig', JSON.stringify(conf));
    initZegoConfig();
});

initZegoConfig();

function initZegoConfig() {
    zegoConfig = JSON.parse(localStorage.getItem('zegoConfig'));
    if (zegoConfig) {
        /**
         * 开源代码时注意屏蔽账号相关信息
         */
        // 替换成在 ZEGO 注册的 appID
        var appID = 0;
        // 替换成在 ZEGO 注册的 appSign
        var appSignStr = '';

        Object.assign(zegoConfig, {
            appID: appID,
            appSign: getAppSignArray(appSignStr),
            appSignStr: appSignStr,
            userid: createUserID(),
            isDocTestEnv: !!zegoConfig.docs_env,
            fileListUrl: fileListUrl,
            fileFilter: [{
                name: 'All',
                extensions: ['*']
            }],
            logDirs: {
                win32: 'c:/zegowblog/',
                darwin: process.env.HOME + '/zegowblog/'
            }
        });
        loadScript('./platform/electron/version.js')
            .then(() => loadScript(`./platform/electron/init_${zegoConfig.sdk_type}.js`))
            .then(() => loadScript('./biz.js'))
            .then(() => loadScript('./platform/electron/biz.js'));
        $('.login_container').css('display', 'none');
        $('.whiteboard_container').css('display', 'block');
    } else {
        loadScript('./platform/electron/version.js');
        $('.whiteboard_container').css('display', 'none');
        $('.login_container').css('display', 'block');
    }
}

function getAppSignArray(str) {
    var arr = [];
    for (var i = 0; i < str.length;) {
        arr.push(`0x${str[i]}${str[i + 1]}`);
        i += 2;
    }
    return arr;
}

function createUserID() {
    var userID = localStorage.getItem('zegouid') || 'ele' + new Date().getTime();
    localStorage.setItem('zegouid', userID);
    return userID;
}