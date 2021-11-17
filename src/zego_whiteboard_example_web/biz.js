var parentId = 'mywhiteboard';

var zegoWhiteboardView; // 当前激活白板实例对象
var zegoDocsView;
var zegoWhiteboardViewList = []; // 白板/文件 展示下拉框
var WBNameIndex = 1; // 白板索引
var seqMap = {
    upload: 0,
    cache: 0,
    saveImg: 1
};

var resizeTicking = false;
var imageErrorTipsMap = {
    3000002: '参数错误',
    3000005: '下载失败',
    3030008: '图片大小超过限制，请重新选择',
    3030009: '图片格式暂不支持',
    3030010: 'url地址错误或无效'
};
var uploadFileTipsMap = {
    1: '上传中',
    2: '已上传',
    4: '排队中',
    8: '转换中',
    16: '转换成功',
    32: '转换失败',
    64: '取消上传'
};

getRemoteConfig();
loginRoom().then(initSDKConfig);

function initSDKConfig() {
    // 设置字体
    if (zegoConfig.fontFamily) {
        document.getElementById(parentId).style.fontFamily = zegoConfig.fontFamily;
    }
    // 设置动态PPT步数切页模式
    zegoDocs.setConfig('pptStepMode', zegoConfig.pptStepMode);
    // 设置缩略图清晰度模式
    zegoDocs.setConfig('thumbnailMode', zegoConfig.thumbnailMode);

    onWhiteboardEventHandle();
    onDocumentEventHandle();
    document.title = `${zegoConfig.sdk_type || ''}互动白板:${zegoWhiteboard.getVersion()},${zegoDocs.getVersion()}`;
}

// 监听sdk回调
function onWhiteboardEventHandle() {
    zegoWhiteboard.on('whiteboardAuthChange', function(data) {
        $('#userViewAuth').html(
            `白板权限：<span class="badge badge-primary mr-2">${data.scale ? '缩放' : ''}</span>
            <span class="badge badge-info mr-2">${data.scroll ? '翻页' : ''}</span>`
        );
    });
    zegoWhiteboard.on('whiteboardGraphicAuthChange', function(data) {
        $('#userGraphicAuth').html(
            `图元权限：<span class="badge badge-success mr-2">${data.create ? '创建' : ''}</span>
            <span class="badge badge-danger mr-2">${data.clear ? '清空' : ''}</span>
            <span class="badge badge-primary mr-2">${data.update ? '编辑' : ''}</span>
            <span class="badge badge-info mr-2">${data.move ? '移动' : ''}</span>
            <span class="badge badge-secondary mr-2">${data.delete ? '擦除' : ''}</span>`
        );
    });
    zegoWhiteboard.on('error', toast);
    zegoWhiteboard.on('viewAdd', function(wbView) {
        var id = wbView.getID();
        if (
            !zegoWhiteboardViewList.some(function(view) {
                return view.getID() == id;
            })
        ) {
            zegoWhiteboardViewList.unshift(wbView);
            updateRemoteView();
        }
    });
    zegoWhiteboard.on('viewRemoved', onWhiteboardRemovedHandle);
    zegoWhiteboard.on('viewScroll', function(res) {
        if (zegoWhiteboardView && zegoWhiteboardView.getID() == res.id) {
            $('#curPage').html(res.page);
            $('#curStep').html(res.step);
        }
    });
}

function onWhiteboardRemovedHandle(id) {
    console.log('on viewRemoved', id);
    var index = zegoWhiteboardViewList.findIndex(function(view) {
        return view.getID() == id;
    });
    if (index !== -1) {
        // remove excel option
        removeExcelOption(zegoWhiteboardViewList[index]);
        zegoWhiteboardViewList.splice(index, 1);
        updateRemoteView();
    }
}

function removeExcelOption(whiteboardView) {
    var fileInfo = whiteboardView.getFileInfo();
    if (fileInfo && fileInfo.fileType === 4) {
        var optionValue = fileInfo.fileID + ',' + fileInfo.fileName;
        $('#excelView option[value="' + optionValue + '"]').remove();
    }
}

// 更新 自定义图片、白板背景图 下拉框数据
function getRemoteConfig() {
    $.get(
        'https://storage.zego.im/goclass/config_images.json',
        function(data) {
            $('#whiteboardImgUrlSelect').html(
                data.whiteboard_custom_images
                    .map(function(v) {
                        return `<option value="${v.id}">${v.name}</option>`;
                    })
                    .join('')
            );
            $('#whiteboardBgImgUrlSelect').html(
                data.whiteboard_bg_images
                    .map(function(v) {
                        return `<option value="${v.id}">${v.name}</option>`;
                    })
                    .join('')
            );
        },
        'json'
    );
}

// 切换面板
$('#panel-change').on('click', 'button', function() {
    var index = $(this).index();
    $('#panel')
        .find('.panel')
        .siblings()
        .hide()
        .eq(index)
        .show();
});
// 右侧菜单
$('.menu-btn,.menu-mask').click(function() {
    $('.operation-container').toggle();
    $('.menu-mask').toggle();
});
// ------ 头部 dom 事件 ------
/**
 * tip:
 * 如果是对文件白板进行翻页操作，仅需对白板实例进行scroll滚动即可，无需另外对文件实例进行翻页操作。
 */
// 上一页
$('#previousPage').click(function() {
    if (!zegoWhiteboardView) return;
    var currentPage = zegoWhiteboardView.getCurrentPage();
    var totalPage = zegoWhiteboardView.getPageCount();
    if (currentPage <= 1 || totalPage <= 1) return;
    var percent = (currentPage - 2) / totalPage;
    var { direction } = zegoWhiteboardView.getCurrentScrollPercent();
    if (direction === 1) {
        zegoWhiteboardView.scroll(percent, 0);
    } else {
        zegoWhiteboardView.scroll(0, percent);
    }
});
// 下一页
$('#nextPage').click(function() {
    if (!zegoWhiteboardView) return;
    var currentPage = zegoWhiteboardView.getCurrentPage();
    var totalPage = zegoWhiteboardView.getPageCount();
    if (currentPage >= totalPage || totalPage <= 1) return;
    var percent = currentPage / totalPage;
    var { direction } = zegoWhiteboardView.getCurrentScrollPercent();
    if (direction === 1) {
        zegoWhiteboardView.scroll(percent, 0);
    } else {
        zegoWhiteboardView.scroll(0, percent);
    }
});
// 上一步
$('#previousStep').click(function() {
    zegoDocsView && zegoDocsView.previousStep();
});
// 下一步
$('#nextStep').click(function() {
    zegoDocsView && zegoDocsView.nextStep();
});
// 跳转指定页
$('#flipPage').keypress(function(e) {
    if (e.which == 13) {
        flipPage();
    }
});
$('#flipPageNum').click(flipPage);

// 离开房间
$('#leaveRoom').click(function() {
    logoutRoom();
    location.reload();
});

// 创建普通白板
$('#createView').click(function() {
    /**
     *tips：多端同步时，aspectWidth、aspectHeight请与白板容器尺寸比例保持一致
     *若父容器 宽:高=w:h
     *创建单页白板 aspectWidth=w，aspectHeight=h
     *创建m页横向白板 aspectWidth=w*m，aspectHeight=h
     *创建m页纵向白板 aspectWidth=w，aspectHeight=h*m
     *创建文件白板，不区分方向，固定 aspectWidth=w，aspectHeight=h*m
     */
    var pageCount = 5;
    var options = {
        roomID: zegoConfig.roomid,
        name: handleWBname(`${zegoConfig.username}创建的白板${WBNameIndex++}`),
        aspectWidth: 16 * pageCount,
        aspectHeight: 9,
        pageCount: pageCount
    };
    zegoWhiteboard
        .createView(options)
        .then(function(view) {
            return zegoWhiteboard.attachView(view, parentId).then(function() {
                zegoWhiteboardView = view;
                zegoDocsView = null;
                setOperationModeState();
                zegoWhiteboardViewList.unshift(view);
                updateRemoteView();
                updatePageAndStep();
            });
        })
        .catch(toast);
});

$('#undo').click(function() {
    zegoWhiteboardView && zegoWhiteboardView.undo();
});

$('#redo').click(function() {
    zegoWhiteboardView && zegoWhiteboardView.redo();
});

$('#clear').click(function() {
    zegoWhiteboardView && zegoWhiteboardView.clear();
});

$('#clearCurrentPage').click(function() {
    zegoWhiteboardView && zegoWhiteboardView.clearCurrentPage();
});

$('#setBrushColor').click(function() {
    var color = $('#brushColor').val();
    zegoWhiteboardView && zegoWhiteboardView.setBrushColor(color);
});

$('#setBackgroundColor').click(function() {
    var color = $('#backgroundColor').val();
    zegoWhiteboardView && zegoWhiteboardView.setBackgroundColor(color);
});

$('#addtext').click(function() {
    if (!zegoWhiteboardView) return;
    if (zegoWhiteboardView.getToolType() !== 2) return toast('该功能仅在工具类型为文本时才生效');
    var text = $('#addtext_val').val();
    var x = +$('#addtext_x').val();
    var y = +$('#addtext_y').val();
    zegoWhiteboardView.addText(text, x, y);
});

// 设置工具类型
$('#tooltype').change(function() {
    if (!zegoWhiteboardView) return;
    var type = $('#tooltype').val();
    if (type == 'drag') {
        zegoWhiteboardView.setToolType(null);
    } else {
        type = +type;
        zegoWhiteboardView.setToolType(type);
        // 上传自定义图形
        type == 512 && zegoWhiteboardView.addImage(1, 0, 0, $('#whiteboardImgUrlSelect').val());
    }
});

$('#whiteboardImgUrlSelect').change(function() {
    if (!zegoWhiteboardView) return;
    zegoWhiteboardView.setToolType(512);
    zegoWhiteboardView.addImage(1, 0, 0, $('#whiteboardImgUrlSelect').val());
    $('#tooltype').val() != '512' && $('#tooltype').val('512');
});
// 笔锋设置
$('#enablePenStroke').change(function() {
    if (!zegoWhiteboard) return;
    zegoWhiteboard.enableHandwriting($('#enablePenStroke').prop('checked'));
});
// 设置字体大小
$('#textsize').change(function() {
    var val = $('#textsize').val();
    zegoWhiteboardView && zegoWhiteboardView.setTextSize(Number(val));
});

// 设置画笔粗细
$('#brushsize').change(function() {
    var val = $('#brushsize').val();
    zegoWhiteboardView && zegoWhiteboardView.setBrushSize(Number(val));
});
$('#disableOperatio').on('change', function() {
    if (!zegoWhiteboardView) return;
    var none = Boolean($('#disableOperatio').prop('checked')) ? 1 : 0;
    var val = none || 14;
    zegoWhiteboardView.setWhiteboardOperationMode(val);
    none && toast('不可操作模式下，不支持其他操作模式');
    $('#enableOperatioScroll').prop('checked', !none);
    $('#enableOperatioDraw').prop('checked', !none);
    $('#enableOperatioZoom').prop('checked', !none);
});
// 设置白板是否允许滚动、绘制、缩放
$('#enableOperatioScroll,#enableOperatioDraw,#enableOperatioZoom').on('change', function() {
    if (!zegoWhiteboardView) return;
    var scroll = Boolean($('#enableOperatioScroll').prop('checked')) ? 2 : 0;
    var draw = Boolean($('#enableOperatioDraw').prop('checked')) ? 4 : 0;
    var zoom = Boolean($('#enableOperatioZoom').prop('checked')) ? 8 : 0;
    var val = scroll | draw | zoom || 14;
    console.log('setWhiteboardOperationMode', val);
    zegoWhiteboardView.setWhiteboardOperationMode(val);
});
// 设置白板是否同步缩放
$('#enableSyncScale').on('change', function() {
    zegoWhiteboard && zegoWhiteboard.enableSyncScale(Boolean($(this).prop('checked')));
});
$('#enableResponseScale').on('change', function() {
    zegoWhiteboard && zegoWhiteboard.enableResponseScale(Boolean($(this).prop('checked')));
});

// 删除选中图元
function deleteSelectedGraphics() {
    zegoWhiteboardView && zegoWhiteboardView.deleteSelectedGraphics();
}

function getViewList() {
    zegoWhiteboard
        .getViewList()
        .then(function(list) {
            zegoWhiteboardViewList = list;
            zegoWhiteboardViewList.reverse();
            updateRemoteView();
        })
        .catch(toast);
}

function updateRemoteView() {
    console.log('updateRemoteView');
    if (zegoWhiteboardViewList.length) {
        var optionsList = [];
        var noReatIds = {}; // 不重复的fileId列表
        zegoWhiteboardViewList.forEach(function(view) {
            var id = view.getID();
            var fileInfo = view.getFileInfo();
            // 纯白板
            if (!fileInfo) {
                optionsList.push('<option value=' + id + '>' + id + '-' + view.getName() + '</option>');
            }
            // 文件白板
            // 过滤excel(同一个excel文件下多个sheet,只存一个option)
            if (fileInfo && !noReatIds[fileInfo.fileID]) {
                noReatIds[fileInfo.fileID] = 1;
                optionsList.push('<option value=' + id + '>' + id + '-' + view.getName() + '</option>');
            }
        });
        $('#remoteView').html(
            '<option value="" disabled selected style="display: none">文件/白板列表</option>' + optionsList.join('')
        );
        if (zegoWhiteboardView) {
            $('#remoteView').val(zegoWhiteboardView.getID());
        }
    } else {
        $('#remoteView').html('<option value="" disabled selected style="display: none">文件/白板列表</option>');
        initSheetList();
    }
}

function updatePageAndStep() {
    $('#filename').html(zegoWhiteboardView.getName());
    $('#curStep').html(1);
    $('#curPage').html(zegoWhiteboardView.getCurrentPage());
    $('#totalPage').html(zegoWhiteboardView.getPageCount());
}

/**
 * @desc: 加载远程白板
 */
var lastwhiteboardIDIsFile = false;
function selectRemoteView(whiteboardID) {
    console.log('change file or wb', lastwhiteboardIDIsFile);

    // 切换白板时，判断即将被切换的白板是否是文件白板，如果是，则暂停该文件白板的音视频播放
    lastwhiteboardIDIsFile && zegoDocsView && zegoDocsView.stopPlay();

    initSheetList();
    var id = whiteboardID ? whiteboardID : $('#remoteView').val();
    if (id) {
        zegoWhiteboardView = zegoWhiteboardViewList.find(function(view) {
            return id === view.getID();
        });
        if (!zegoWhiteboardView) {
            toast('远端白板不存在');
            return;
        }

        var fileInfo = zegoWhiteboardView.getFileInfo();

        if (fileInfo) {
            lastwhiteboardIDIsFile = true;

            zegoDocsView = zegoDocs.createView(parentId, id, fileInfo.fileName);
            zegoDocsView
                .loadFile(fileInfo.fileID, fileInfo.authKey, function(res) {
                    zegoWhiteboard.attachView(zegoWhiteboardView, res.viewID).then(setOperationModeState);
                    updatePageAndStep();
                    getAllSheet(res);
                })
                .catch(toast);
        } else {
            lastwhiteboardIDIsFile = false;
            zegoDocsView = null;
            zegoWhiteboard.attachView(zegoWhiteboardView, parentId).then(setOperationModeState);
            updatePageAndStep();
        }
    }
}

// 关联白板操作模式状态
function setOperationModeState() {
    $('#disableOperatio').prop('checked', false);
    $('#enableOperatioScroll').prop('checked', true);
    $('#enableOperatioDraw').prop('checked', true);
    $('#enableOperatioZoom').prop('checked', true);
    zegoWhiteboardView && zegoWhiteboardView.setWhiteboardOperationMode(2 | 4 | 8);
}

// 设置文本粗体
function setFontBold() {
    var bold = $('#bold').prop('checked');
    zegoWhiteboardView && zegoWhiteboardView.setFontBold(bold);
}
// 设置文本斜体
function setFontItalic() {
    var italic = $('#italic').prop('checked');
    zegoWhiteboardView && zegoWhiteboardView.setFontItalic(italic);
}
// 设置缩放
function setZoom(event) {
    zegoWhiteboardView && zegoWhiteboardView.setScaleFactor(+event.target.value);
}

// 销毁view
function destroyView() {
    if (!zegoWhiteboardView) return;
    onWhiteboardRemovedHandle(zegoWhiteboardView.getID());
    zegoWhiteboard
        .destroyView(zegoWhiteboardView)
        .then(function() {
            zegoWhiteboardView = null;
            zegoDocsView = null;
        })
        .catch(toast);
}

// 销毁全部白板
function destroyAllView() {
    zegoWhiteboardViewList.forEach(function(item) {
        zegoWhiteboard.destroyView(item);
    });
    zegoWhiteboardViewList = [];
    updateRemoteView();
}
// 清除白板上的所有图元
function clear() {
    zegoWhiteboardView && zegoWhiteboardView.clear();
}
// 清除当前页图元，在工具为橡皮擦的时候生效
function clearCurrentPage() {
    zegoWhiteboardView && zegoWhiteboardView.clearCurrentPage();
}
/**
 * 上传文件
 */
function execUploadFile(file, type) {
    if (!file) {
        toast('请先选择文件');
        return;
    }
    var id = type == 3 ? '#staticFile' : '#dynamicFile';
    $(id).val('');
    zegoDocs
        .uploadFile(file, type, {}, function(res) {
            console.log('onUpload', res);
            seqMap.upload = res.fileHash || res.seq;
            toast(uploadFileTipsMap[res.status] + res.uploadPercent || '');
        })
        .then(createFileView)
        .catch(toast);
}

function uploadFileHandle(event, type) {
    if (zegoConfig.appSign) {
        showOpenDialog().then(function(path) {
            execUploadFile(path, type);
        });
    } else {
        execUploadFile(event.target.files[0], type);
    }
}

function selectExcel(event) {
    var value = $('#excelView').val();
    var fileID = value.split(',')[0];
    var sheetName = value.split(',')[1];
    createFileView(fileID, sheetName);
}
/**
 * 创建文件白板流程：
 *  1，创建 docsView
 *  2，加载文件
 *  3，文件加载成功的回调中创建白板
 */
function createFileView(fileID, sheetName) {
    fileID = fileID || $('#fileID').val();
    if (!fileID) return;
    var matchedView = zegoWhiteboardViewList.find(function(item) {
        var fileInfo = item.getFileInfo() || {};
        // 寻找excel中匹配的sheet文件。
        if (sheetName) return fileInfo.fileID === fileID && fileInfo.fileName === sheetName;
        return fileID === fileInfo.fileID;
    });
    // 如果是已经存在的文件白板
    if (!!matchedView) {
        return selectRemoteView(matchedView.whiteboardID);
    }

    // createView 入参一个sheetName，表示想要loadFile该sheet。
    zegoDocsView = zegoDocs.createView(parentId, undefined, sheetName);
    zegoDocsView
        .loadFile(fileID, '', function(res) {
            if (res.fileType === 4) {
                // excel 创建所有sheet的白板
                createSheetWbView(res.fileID, res.sheets, res.sheets.length - 1);
            } else {
                createFileWBView(res);
            }
        })
        .catch(toast);
}

// 创建文件白板
function createFileWBView(res) {
    console.log('=== create file wb ===', res);
    zegoWhiteboard
        .createView({
            roomID: zegoConfig.roomid,
            name: handleWBname(res.fileType === 4 ? res.name : res.fileName),
            aspectWidth: res.width,
            aspectHeight: res.height,
            pageCount: res.pageCount,
            fileInfo: {
                fileID: res.fileID,
                fileName: res.fileName,
                fileType: res.fileType,
                authKey: res.authKey
            }
        })
        .then(function(view) {
            zegoWhiteboardViewList.unshift(view);
            zegoWhiteboardView = view;
            // 更新白板列表
            updateRemoteView();
            zegoWhiteboard.attachView(view, res.viewID).then(setOperationModeState);
            updatePageAndStep();
            getAllSheet(res);
        });
}
// 递归 串行创建白板sheet
function createSheetWbView(fileID, sheets, sheetIndex) {
    if (sheetIndex < 0) return;

    zegoDocs.createView(parentId, undefined, sheets[sheetIndex]).loadFile(fileID, '', function(res) {
        if (sheetIndex == 0) {
            createFileWBView(res);
            createSheetWbView(fileID, sheets, sheetIndex - 1);
            return;
        }

        console.log('=== create sheet wb ===', sheetIndex, sheets[sheetIndex], res);
        zegoWhiteboard
            .createView({
                roomID: zegoConfig.roomid,
                name: handleWBname(res.name),
                aspectWidth: res.width,
                aspectHeight: res.height,
                pageCount: res.pageCount,
                fileInfo: {
                    fileID: fileID,
                    fileName: sheets[sheetIndex],
                    fileType: res.fileType,
                    authKey: res.authKey
                }
            })
            .then(function(view) {
                zegoWhiteboardViewList.unshift(view);
                createSheetWbView(fileID, sheets, sheetIndex - 1);
            });
    });
}

function initSheetList() {
    // 初始化sheet列表
    $('#excelView').html('<option value="" disabled selected style="display: none">sheet列表</option>');
}
// sheet列表
function getAllSheet(res) {
    var { sheets, fileID, fileType } = res;
    if (fileType === 4) {
        var excelSheetHtml = sheets.map(function(sheet) {
            return '<option value=' + fileID + ',' + sheet + '>' + sheet + '</option>';
        });
        $('#excelView').html(
            '<option value="" disabled selected style="display: none">sheet列表</option>' + excelSheetHtml.join('')
        );
        $('#excelView').val(fileID + ',' + res.fileName);
    }
}

//取消上传
function cancelUpload() {
    zegoDocs.cancelUploadFile(seqMap.upload).then(function(res) {
        if (res === true) toast('取消上传操作成功');
        $('#staticFile').val('');
        $('#dynamicFile').val('');
    });
}

//获取缩略图
function getThumbnailUrlList() {
    if (!zegoDocsView) return;
    var type = zegoDocsView.getFileType();
    // 仅支持PDF，PPT，动态PPT 文件格式
    var supportType = [1, 8, 512, 4096];
    if (supportType.includes(type)) {
        var thumbnailUrlList = zegoDocsView.getThumbnailUrlList();
        if (thumbnailUrlList.length > 0) {
            var imgs = thumbnailUrlList.map(function(v, i) {
                return '<span>' + (i + 1) + '</span><img src="' + v + '"/>';
            });
            $('#thumbnail-list').html(imgs.join(''));
            $('.thumbnail').show();
        }
    } else {
        toast('获取缩略图仅支持“PDF，PPT，动态PPT，H5”文件格式');
    }
}

function closeThumbnail() {
    $('.thumbnail').hide();
}

// 添加图片-本地
var myLocalIMG;

function uploadLocalIMG(event) {
    if (zegoConfig.appSign) {
        showOpenDialog().then(function(path) {
            myLocalIMG = {
                path
            };
        });
    } else {
        myLocalIMG = event.target.files[0];
    }
}

$('#localImg').blur(function() {
    var str = $('#localImg')
        .val()
        .trim();
    if (str) {
        $('#uploadFile').val('');
        myLocalIMG = str;
    }
});

/**
 * @desc: 添加图片
 * @param type 0: 插入图片，1: 自定义图形
 */
function addImage(type) {
    if (!zegoWhiteboardView) return;
    var positionX, positionY, address;
    if (type == 1) {
        positionX = 0;
        positionY = 0;
        address = $('#whiteboardImgUrl')
            .val()
            .trim();
    } else {
        positionX = $('#imgpositionX').val();
        positionY = $('#imgpositionY').val();
        positionX = positionX && +positionX;
        positionY = positionY && +positionY;
        address = myLocalIMG;
    }
    console.warn(type, positionX, positionY, address);
    zegoWhiteboardView
        .addImage(type, positionX, positionY, address, toast)
        .then(function(res) {
            if (type == 1) {
                var name = decodeURIComponent(res).replace(/(.*\/)*([^.]+).*/gi, '$2');
                var html = `<option value="${res}">${name}</option>${$('#whiteboardImgUrlSelect').html()}`;
                $('#whiteboardImgUrlSelect').html(html);
                $('#whiteboardImgUrlSelect').val('');
            }
        })
        .catch(function(e) {
            if (e && e.code) {
                toast(e.code + '：' + (imageErrorTipsMap[e.code] || e.msg));
            }
        });
}

/**
 * 设置白板信令延迟
 *
 * 调用时机：初始化后
 *
 * @param delay 延迟时长，单位ms
 *
 * 设置后 除了图元操作，还是获取白板列表、创建白板、渲染白板、销毁白板都有影响
 */
function setDeferredRenderingTime() {
    var delay = $('#deferredRenderingTime').val();
    zegoWhiteboard.setDeferredRenderingTime(+delay);
}

function toast(msg) {
    $('.toast').toast('show');
    $('#error_msg').html(typeof msg === 'string' ? msg : JSON.stringify(msg));
}

/**
 * 白板名称超过128字节时，创建白板会失败
 * @param {*} name 白板名称
 * @note 一个中文占3字节，英文、字母等占1字节
 */
function handleWBname(name) {
    var size = new Blob([name]).size;
    if (size <= 128) return name;
    var str;
    for (var i = name.length - 1; i > 0; i--) {
        str = name.substr(0, i);
        size = new Blob([str]).size;
        if (size <= 128) {
            return str;
        }
    }
    // 截断规则：前21字符...后20字符，这里当做全中文处理（21*3+3+20*3=126）
    // return name.replace(/^(.{21}).*?(.{20})$/, '$1...$2');
}

function onDocumentEventHandle() {
    window.addEventListener('keydown', function(event) {
        var e = event || window.event || arguments.callee.caller.arguments[0];
        if (!e) return;
        switch (e.keyCode) {
            case 8: // 监听backspace按键，批量删除选中图元
            case 46: // 监听Delete按键，批量删除选中图元
                deleteSelectedGraphics();
                break;
            case 27: // esc 退出全屏
                triggerFullscreen(true);
                break;
            default:
                break;
        }
    });
    // 白板大小自适应，移动端软键盘收缩会引起变化
    onResizeHandle();
    window.addEventListener(zegoConfig.isTouch ? 'orientationchange' : 'resize', onResizeHandle);
}

function onResizeHandle(e) {
    if (!resizeTicking) {
        resizeTicking = true;
        setTimeout(function() {
            var dom = document.getElementById(parentId);
            var { clientWidth, clientHeight } = dom.parentNode;
            var width = clientWidth;
            var height = ((9 * width) / 16) | 0;
            if (height > clientHeight) {
                height = clientHeight;
                width = ((clientHeight * 16) / 9) | 0;
            }
            // orientationchange
            if (e && e.type == 'orientationchange') {
                if (document.body.clientWidth > document.body.clientHeight) {
                    $('.operation-container').show();
                } else {
                    $('.operation-container').hide();
                    $('.menu-mask').hide();
                }
            }
            reloadView(width, height, dom);
            resizeTicking = false;
        }, 1000);
    }
}

function dispatchClickEvent(dom) {
    if (zegoConfig.isTouch) {
        var e1 = document.createEvent('Events');
        e1.initEvent('touchstart', true, true);
        var e2 = document.createEvent('Events');
        e2.initEvent('touchend', true, true);
        dom.dispatchEvent(e1);
        e2 = dom.dispatchEvent(e2);
        // ipad 14.2 非标准兼容处理
        if (e2) {
            e1 = document.createEvent('Events');
            e1.initEvent('click', true, true);
            e1 = dom.dispatchEvent(e1);
            dom.click && dom.click();
        }
    } else {
        // var e = document.createEvent('Events');
        // e.initEvent('click', true, true);
        // dom.dispatchEvent(e);
        dom.click();
    }
}

function saveImage() {
    if (!zegoWhiteboardView) return;
    if (zegoConfig.appSign) {
        showSaveDialog();
    } else {
        zegoWhiteboardView.snapshot().then(function(data) {
            var link = document.createElement('a');
            link.href = data.image;
            link.download = zegoWhiteboardView.getName() + seqMap.saveImg++ + '.png';
            dispatchClickEvent(link);
        });
    }
}

// 添加背景图片-本地
var myLocalBGIMG;

function uploadLocalBGIMG(event) {
    if (zegoConfig.appSign) {
        showOpenDialog().then(function(path) {
            myLocalBGIMG = {
                path
            };
        });
    } else {
        myLocalBGIMG = event.target.files[0];
    }
}

function setWhiteboardBg(type) {
    var model = $('#whiteboardBgImgModelSelect').val();
    if (!model) {
        toast('渲染类型不能为空！');
        return;
    }
    var url = $('#whiteboardBgImg').val() || $('#whiteboardBgImgUrlSelect').val();
    zegoWhiteboardView.setBackgroundImage(type === 1 ? url : myLocalBGIMG, +model, toast).catch(function(e) {
        if (e && e.code) {
            toast(e.code + '：' + (imageErrorTipsMap[e.code] || e.msg));
        }
    });
}

function clearBackgroundImage() {
    zegoWhiteboardView && zegoWhiteboardView.clearBackgroundImage();
}

function reloadView(width, height, dom) {
    width = width || +$('#parentWidth').val();
    height = height || +$('#parentHeight').val();
    if (!width || !height || width < 1 || height < 1) return toast('请输入有效的宽高值');

    dom = dom || document.getElementById(parentId);
    dom.style.cssText += `width:${width}px;height:${height}px;`;
    $('#parentWidthHeight').html(`容器宽高：${width},${height}`);

    if (zegoWhiteboardView) {
        // 动画100ms
        setTimeout(function() {
            zegoWhiteboardView.reloadView();
            $('#parentWidth').val('');
            $('#parentHeight').val('');
        }, 120);
    }
}

function cacheFile() {
    var fileID = $('#preloadFileID').val();
    fileID && zegoDocs.cacheFile(fileID);
}

/**
 * Mobile：不兼容 Element.requestFullscreen ，用样式和 DOM 来模拟全屏效果
 * Desktop：用 Element.requestFullscreen 实现，但是由于部分浏览器实现可能有问题，代码仅供参考
 *
 * @param exit 是否退出全屏
 *
 * @note 详见文档：https://developer.mozilla.org/zh-CN/docs/Web/API/Element/requestFullScreen
 */
function triggerFullscreen(exit) {
    new Promise(function(resolve, reject) {
        if (!zegoConfig.isTouch) {
            if (!exit) {
                supportRequestFullscreen(document.getElementById(parentId))
                    .then(function() {
                        toast('按 Esc 键退出全屏');
                        resolve();
                    })
                    .catch(reject);
            } else {
                resolve();
            }
        } else {
            reject();
        }
    })
        .catch(function() {
            // 浏览器不支持 requestFullscreen，用样式和 DOM 来模拟全屏效果
            triggerFullscreenForMobile(exit);
        })
        .then(function() {
            // 改变容器大小后，需要调用白板接口 reloadView
            if (zegoWhiteboardView) {
                // 动画100ms
                setTimeout(function() {
                    zegoWhiteboardView.reloadView();
                }, 120);
            }
        });
}
function supportRequestFullscreen(dom) {
    if (dom.requestFullscreen) {
        return dom.requestFullscreen();
    } else if (dom.webkitRequestFullScreen) {
        return dom.webkitRequestFullScreen();
    } else if (dom.mozRequestFullScreen) {
        return dom.mozRequestFullScreen();
    } else {
        return dom.msRequestFullscreen();
    }
}
function triggerFullscreenForMobile(exit) {
    if (!exit) {
        // 白板全屏
        $('.top-bar').hide();
        if (zegoConfig.isTouch) {
            $('.menu-mask').hide();
            $('.menu-btn').hide();
            $('.mobile_bottom_container').show();
        } else {
            toast('按 Esc 键退出全屏');
        }
        $('.operation-container').hide();
        $('.main-container').addClass('main-container_full');
        $('.whiteboard-area').addClass('whiteboard-area_full');
    } else {
        // 取消全屏
        $('.top-bar').show();
        $('.mobile_bottom_container').hide();
        if (!zegoConfig.isTouch || (zegoConfig.isTouch && document.body.clientWidth > document.body.clientHeight)) {
            $('.operation-container').show();
        }
        if (zegoConfig.isTouch && document.body.clientWidth < document.body.clientHeight) {
            $('.menu-btn').show();
        }
        $('.main-container').removeClass('main-container_full');
        $('.whiteboard-area').removeClass('whiteboard-area_full');
    }
}

function flipPage() {
    if (!zegoWhiteboardView) return;
    var page = +$('#flipPage').val();
    var totalPage = zegoWhiteboardView.getPageCount();
    if (page >= 1 && page <= totalPage) {
        var percent = (page - 1) / totalPage;
        var { direction } = zegoWhiteboardView.getCurrentScrollPercent();
        if (direction === 1) {
            zegoWhiteboardView.scroll(percent, 0);
        } else {
            zegoWhiteboardView.scroll(0, percent);
        }
    }
}

// 上传H5课件
function uploadH5Handle(event) {
    var h5Width = Math.max(+$('#h5Width').val(), 0);
    var h5Height = Math.max(+$('#h5Height').val(), 0);
    var h5PageCount = Math.max(+$('#h5PageCount').val(), 0);
    var h5ThumbnailList = $('#h5ThumbnailList')
        .val()
        .trim();
    if (!zegoDocs) {
        toast('请先初始化');
        return;
    }
    if (!(h5Width && h5Height && h5PageCount)) {
        toast('h5课件参数有误');
        return;
    }
    var config = {
        width: h5Width,
        height: h5Height,
        pageCount: h5PageCount,
        thumbnailList: h5ThumbnailList ? h5ThumbnailList.split(',') : null
    };
    if (zegoConfig.appSign) {
        showOpenDialog().then(function(path) {
            execUploadH5File(path, config);
        });
    } else {
        execUploadH5File(event.target.files[0], config);
    }
}

function execUploadH5File(file, config) {
    if (!file) {
        toast('请先选择文件');
        return;
    }
    console.warn('上传H5课件', config);
    zegoDocs
        .uploadH5File(file, config, toast)
        .then(function(res) {
            $('#h5file').val('');
            $('#fileID').val(res);
        })
        .catch(function(e) {
            $('#h5file').val('');
            $('#fileID').val('');
            toast(e);
        });
}
