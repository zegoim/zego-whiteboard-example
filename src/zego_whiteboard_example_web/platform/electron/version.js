var zegoSDKConfig = {
    liveroom: '../../sdk/v1220/zego-liveroom-whiteboard-electron/ZegoLiveRoom.js',
    liveroomWb: '../../sdk/v1220/zego-liveroom-whiteboard-electron/ZegoWhiteBoardView.js',
    express: '../../sdk/v1220/zego-express-engine-electron/ZegoExpressEngine.js',
    expressWb: '../../sdk/v1220/zego-express-engine-electron/ZegoWhiteBoardView.js',
    docs: '../../sdk/v1220/zego-express-docsview-electron'
};

$('#env-btn').before(
    `<div class="form-group mt-2">
        <label for="">SDK类型</label>
        <select id="sdk_type" class="form-style">
        <option value="express">express</option>
        <option value="liveroom">liveroom</option>
        </select>
    </div>`
);

// 内部自定义设置，不对外暴露
function hookInitSDKConfig() {}
