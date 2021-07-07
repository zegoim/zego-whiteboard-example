/**
 * electron 平台的业务
 */

var fs = require('fs');
var { remote, ipcRenderer } = require('electron');

ipcRenderer.on('message', (event, text) => {
    console.log(arguments);
});
ipcRenderer.on('downloadProgress', (event, progressObj) => {
    console.log(progressObj.percent || 0);
});
ipcRenderer.on('isUpdateNow', () => {
    ipcRenderer.send('isUpdateNow');
});
window.onbeforeunload = function(e) {
    var funcs = ['message', 'downloadProgress', 'isUpdateNow'];
    for (var i = 0, l = funcs.length; i < l; i++) {
        ipcRenderer.removeListener(funcs[i], console.log);
    }
};
window.addEventListener('online', function() {
    zegoWhiteboardView && zegoWhiteboardView.undo();
    setTimeout(function() {
        zegoWhiteboardView && zegoWhiteboardView.redo();
    }, 100);
});

var mainWindow = remote.getCurrentWindow();
mainWindow.on('maximize', onResizeHandle);
mainWindow.on('restore', onResizeHandle);

function showOpenDialog() {
    return new Promise((resolve, reject) => {
        remote.dialog.showOpenDialog(
            {
                properties: ['openFile'],
                filters: zegoConfig.fileFilter
            },
            (paths) => {
                if (paths) resolve(paths[0].replace(/\\/g, '/'));
                else toast('No file');
            }
        );
    });
}

function showSaveDialog() {
    zegoWhiteboardView.snapshot().then(function(res) {
        remote.dialog.showSaveDialog(
            {
                title: '保存快照',
                defaultPath: '~/' + zegoWhiteboardView.getName() + seqMap.saveImg++ + '.png'
            },
            function(filename) {
                if (filename) {
                    var base64Data = res.image.replace(/^data:image\/\w+;base64,/, '');
                    var dataBuffer = Buffer.from(base64Data, 'base64');
                    fs.writeFile(filename, dataBuffer, function(e) {
                        toast(e || '保存成功');
                    });
                }
            }
        );
    });
}

function cacheFile() {
    zegoDocs
        .cacheFile(document.getElementById('preloadFileID').value, function(res) {
            seqMap.cache = res.seq;
            toast('缓存进度：' + JSON.stringify(res));
        })
        .then(toast);
}

function queryCache() {
    zegoDocs
        .queryFileCached(document.getElementById('preloadFileID').value)
        .then(toast)
        .catch(toast);
}

function cancelCacheFile() {
    zegoDocs.cancelCacheFile(seqMap.cache).then(toast);
}
