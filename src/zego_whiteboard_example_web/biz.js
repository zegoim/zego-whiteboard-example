var dynamicPPTHD = zegoEnv.dynamicPPTHD;
var pptStepMode = zegoEnv.pptStepMode;
var idName = createUserID();
var parentId = 'mywhiteboard';
var web_token = '';
var zegoWhiteboard,
  zegoWhiteboardView, // 当前激活白板实例对象
  zegoDocs,
  zegoDocsView,
  zegoWhiteboardViewList = [], // 白板/文件 展示下拉框
  userIDList = [],
  isLogin = false,
  WBNameIndex = 1, // 白板索引
  fileHash,
  netImgUrlList = [], //自定义图形下拉框图片list
  netBgImgUrlList = [], //白板背景图
  isRemote,
  myFile, // 上传文档对象
  _seq = 0;

if(!appID){
  alert('请在demo_config.js文件中填写你的appID！')
  window.location.href = './login.html'
}
var imageErrorTipsMap = {
  3000002: '参数错误',
  3000005: '下载失败',
  3030008: '图片大小超过限制，请重新选择',
  3030009: '图片格式暂不支持',
  3030010: 'url地址错误或无效'
};

// 获取token
$.ajaxSettings.async = false;
$.get(
  tokenUrl,
  {
    app_id: appID,
    id_name: idName
  },
  function (token) {
    if (!token) {
      toast('get token failed');
    } else {
      web_token = token;
    }
  },
  'text'
);
$.get(
  'https://storage.zego.im/goclass/config_images.json',
  function (data) {
    netImgUrlList = data['whiteboard_custom_images'];
    netBgImgUrlList = data['whiteboard_bg_images'];
  },
  'json'
);
$.ajaxSettings.async = true;
// $.toastDefaults.position = 'top-center';

// 初始化
init();
// 登录 - 房间
openRoom(idName, zegoRoomInfo.roomid, web_token);
$('#roomid').html(zegoRoomInfo.roomid);

// 切换面板
$('#panel-change').on('click', 'button', function () {
  var index = $(this).index();
  $('#panel').find('.panel').siblings().hide().eq(index).show();
});

// 监听sdk回调
function listen() {
  zegoWhiteboard.on('roomUserUpdate', (roomID, type, list) => {
    if (type == 'ADD') {
      list.forEach((v) => userIDList.push(v.userID));
    } else if (type == 'DELETE') {
      list.forEach((v) => {
        const id = v.userID;
        const index = userIDList.findIndex((item) => id == item);
        if (index != -1) {
          userIDList.splice(index, 1);
        }
      });
    }
    $('#idNames').html('房间所有用户ID：' + userIDList.toString());
  });
  zegoWhiteboard.on('whiteboardAuthChange', (data) => {
    $('#userViewAuth').html(`白板：${data.scale ? '缩放' : ''},${data.scroll ? '翻页' : ''}`);
  });
  zegoWhiteboard.on('whiteboardGraphicAuthChange', (data) => {
    $('#userGraphicAuth').html(
      `图元：${data.create ? '创建' : ''},${data.clear ? '清空' : ''},${data.update ? '编辑' : ''},${
        data.move ? '移动' : ''
      },${data.delete ? '擦除' : ''}`
    );
  });
  zegoWhiteboard.on('error', (e) => {
    console.error('on error', e);
    toast(e.code + '：' + e.msg);
  });
  zegoWhiteboard.on('viewAdd', (wbView) => {
    console.log('on viewAdd', wbView);
    const id = wbView.getID();
    if (!zegoWhiteboardViewList.some((v) => v.getID() == id)) {
      zegoWhiteboardViewList.unshift(wbView);
      updateRemoteView();
    }
  });
  zegoWhiteboard.on('viewRemoved', (whiteboardID) => {
    console.log('on viewRemoved', whiteboardID);
    const index = zegoWhiteboardViewList.findIndex((v) => v.getID() == whiteboardID);
    if (index !== -1) {
      zegoWhiteboardViewList.splice(index, 1);
      updateRemoteView();
    }
  });
  zegoWhiteboard.on('viewScroll', ({ id, horizontalPercent, verticalPercent, page }) => {
    console.log('on viewScroll', id, horizontalPercent, verticalPercent, page);
    $('#curPage').html(page);
  });
  zegoDocs.on('onUpload', async function (res) {
    var ZegoDocsViewUploadState = {
      1: '上传中',
      2: '已上传',
      4: '排队中',
      8: '转换中',
      16: '转换成功',
      32: '转换失败',
      64: '取消上传'
    };
    if (res.status === 1 && res.uploadPercent) {
      console.log(`文件${ZegoDocsViewUploadState[res.status]}，进度${res.uploadPercent}% :`, res);
    } else {
      console.log(`文件${ZegoDocsViewUploadState[res.status]}:`, res);
    }
    fileHash = res.fileHash;
  });
  zegoDocs.on('onLoadFile', async function (res) {
    console.log('onLoadFile', res);
    /**
     * 创建文件白板流程：
     *  1，上传文件
     *  2，加载文件
     *  3，文件加载成功的回调（onLoadFile）中根据回调返回的值创建对应的普通白板
     */
    createFileWBView(res);
    $('#totalPage').html(res.pageCount);
  });
}
function createUserID() {
  var userID = sessionStorage.getItem('zegouid') || 'web' + new Date().getTime();
  sessionStorage.setItem('zegouid', userID);
  return userID;
}
// sdk初始化
async function init() {
  // 互动白板
  zegoWhiteboard = new ZegoExpressEngine(appID, server);

  zegoWhiteboard.setLogConfig({ logLevel: 1 });
  zegoWhiteboard.setDebugVerbose(false);
  // 文件转码
  console.log('isDocTestEnv', isDocTestEnv);
  zegoDocs = new ZegoExpressDocs({
    appID: appID,
    userID: idName,
    token: web_token,
    isTestEnv: isDocTestEnv
  });

  // 设置上传动态ppt文件清晰度
  zegoDocs.setConfig('dynamicPPTHD', dynamicPPTHD);
  /**
   * 设置动态PPT步数切页模式
   * Note: 1 默认模式，正常上一步和下一步
   * Note: 2 在页中的第一步执行上一步时，不跳转，页中的最后一步执行下一步时，不跳转。
   */
  zegoDocs.setConfig('pptStepMode', pptStepMode);
  if (zegoEnv.docs_env == 3) {
    console.log('文件环境连接alpha');
    zegoDocs.setConfig('set_alpha_env', 'true');
  }

  listen();
  // 设置字体
  zegoEnv.font_family == 1 ? '' : (document.getElementById(parentId).style.fontFamily = 'ZgFont');

  updateNetOption();
  onKeydownHandle();
  resetParentWidthHeight();
  console.log('互动白板 sdk 版本:', zegoWhiteboard.getVersion());
  console.log('文件转码 sdk 版本:', zegoDocs.getVersion());
}
// 离开房间
function leaveRoom() {
  toast('leave room');
  isLogin = false;
  zegoWhiteboard.logoutRoom();
  userIDList.shift();
  location.href = './login.html';
}
// 进入房间
function openRoom(idName, roomID, token) {
  if (isLogin) {
    return leaveRoom();
  }

  if (!roomID) {
    toast('roomID错误！');
    return;
  }

  if (!token) {
    toast('token错误！');
    return;
  }

  if (!idName) {
    toast('idName错误！');
    return;
  }

  //login
  async function startLogin() {
    try {
      await zegoWhiteboard.loginRoom(
        roomID,
        token,
        {
          userID: idName,
          userName: 'nick' + idName
        },
        {
          maxMemberCount: 10,
          userUpdate: true
        }
      );
      userIDList.unshift(idName);
      $('#idNames').html('房间所有用户ID：' + userIDList.toString());
    } catch (error) {
      console.log(error);
    }
  }
  startLogin();
}

$(document).ready(function () {
  $('#leaveRoom').click(function () {
    leaveRoom();
  });
  // 创建普通白板
  $('#createView').click(async function () {
    try {
      /**
       *tips：多端同步时，aspectWidth、aspectHeight请与白板容器尺寸比例保持一致
       *若父容器 宽:高=w:h
       *创建单页白板 aspectWidth=w，aspectHeight=h
       *创建m页横向白板 aspectWidth=w*m，aspectHeight=h
       *创建m页纵向白板 aspectWidth=w，aspectHeight=h*m
       *创建文件白板，不区分方向，固定 aspectWidth=w，aspectHeight=h*m
       */
      var dom = $('#' + parentId);
      var options = {
        roomID: zegoRoomInfo.roomid,
        name: `${zegoRoomInfo.username}创建的白板${WBNameIndex++}`,
        aspectWidth: 5 * (dom.width() | 0),
        aspectHeight: dom.height() | 0,
        pageCount: 5
      };
      zegoWhiteboardView = await zegoWhiteboard.createView(options);
      await zegoWhiteboard.attachView(zegoWhiteboardView, parentId);
      setOperationModeState();
      zegoWhiteboardViewList.unshift(zegoWhiteboardView);
      updateRemoteView();
      zegoDocsView = null;
      $('#filename').html(zegoWhiteboardView.getName());
      $('#curPage').html(zegoWhiteboardView.getCurrentPage());
      $('#totalPage').html(zegoWhiteboardView.getPageCount());
    } catch (error) {
      console.log(error);
    }
  });
  // 上一页
  $('#previousPage').click(async function () {
    const currentPage = zegoWhiteboardView.getCurrentPage();
    const totalPage = zegoWhiteboardView.getPageCount();
    if (currentPage <= 1 || totalPage <= 1) return;
    const percent = (currentPage - 2) / totalPage;
    const { direction } = zegoWhiteboardView.getCurrentScrollPercent();
    if (direction === 1) {
      zegoWhiteboardView.scroll(percent, 0);
    } else {
      zegoWhiteboardView.scroll(0, percent);
    }
  });
  // 下一页
  $('#nextPage').click(async function () {
    const currentPage = zegoWhiteboardView.getCurrentPage();
    const totalPage = zegoWhiteboardView.getPageCount();
    if (currentPage >= totalPage || totalPage <= 1) return;
    const percent = currentPage / totalPage;
    const { direction } = zegoWhiteboardView.getCurrentScrollPercent();
    if (direction === 1) {
      zegoWhiteboardView.scroll(percent, 0);
    } else {
      zegoWhiteboardView.scroll(0, percent);
    }
  });

  /**
   * tip:
   * 如果是对文件白板进行翻页操作，仅需对白板实例进行scroll滚动即可，无需另外对文件实例进行翻页操作。
   */
  // 上一步
  $('#previousStep').click(async function () {
    zegoDocsView && zegoDocsView.previousStep();
  });
  // 下一步
  $('#nextStep').click(async function () {
    zegoDocsView && zegoDocsView.nextStep();
  });
  // 跳转指定页
  $('#flipPage').keypress(function (e) {
    var page = $('#flipPage').val();
    if (e.which == 13 && page >= 1) {
      const totalPage = zegoWhiteboardView.getPageCount();
      const percent = (page - 1) / totalPage;
      const { direction } = zegoWhiteboardView.getCurrentScrollPercent();
      if (direction === 1) {
        zegoWhiteboardView.scroll(percent, 0);
      } else {
        zegoWhiteboardView.scroll(0, percent);
      }
    }
  });

  $('#undo').click(function () {
    zegoWhiteboardView.undo();
  });

  $('#redo').click(function () {
    zegoWhiteboardView.redo();
  });

  $('#clear').click(function () {
    zegoWhiteboardView.clear();
  });

  $('#clearCurrentPage').click(function () {
    zegoWhiteboardView.clearCurrentPage();
  });

  $('#setBrushColor').click(function () {
    const color = $('#brushColor').val();
    zegoWhiteboardView.setBrushColor(color);
  });

  $('#setBackgroundColor').click(function () {
    const color = $('#backgroundColor').val();
    zegoWhiteboardView.setBackgroundColor(color);
  });

  $('#addtext').click(function () {
    if (zegoWhiteboardView.getToolType() !== 2) toast('该功能仅在工具类型为文本时才生效');
    var text = $('#addtext_val').val();
    var x = +$('#addtext_x').val();
    var y = +$('#addtext_y').val();
    zegoWhiteboardView && zegoWhiteboardView.addText(text, x, y);
  });
});

// 设置工具类型
$('#tooltype').change(function () {
  if (!zegoWhiteboardView) return;
  var type = $('#tooltype').val();
  if (type !== 'drag') {
    if (type == 512) {
      zegoWhiteboardView.addImage(1, 0, 0, $('#netImgSelect').val());
    }
    // 选择橡皮擦，批量删除图元
    // if (type == 64) {
    //   deleteSelectedGraphics();
    // }
    zegoWhiteboardView.setToolType(+type);
  } else {
    zegoWhiteboardView.setToolType(null);
  }
});

$('#netImgSelect').change(function () {
  if (!zegoWhiteboardView) return;
  var url = $('#netImgSelect').val();
  zegoWhiteboardView.addImage(1, 0, 0, url);
});

// 删除选中图元
function deleteSelectedGraphics() {
  zegoWhiteboardView && zegoWhiteboardView.deleteSelectedGraphics();
}

// 设置字体大小
$('#textsize').change(function () {
  var val = $('#textsize').val();
  zegoWhiteboardView && zegoWhiteboardView.setTextSize(Number(val));
});

// 设置画笔粗细
$('#brushsize').change(function () {
  var val = $('#brushsize').val();
  zegoWhiteboardView && zegoWhiteboardView.setBrushSize(Number(val));
});
// 设置白板是否允许滚动、绘制、缩放
$('#disableOperatio,#enableOperatioScroll,#enableOperatioDraw,#enableOperatioZoom').on('change', function () {
  if (!zegoWhiteboardView) return;
  var none = Boolean($('#disableOperatio').prop('checked')) ? 1 : 0;
  var scroll = Boolean($('#enableOperatioScroll').prop('checked')) ? 2 : 0;
  var draw = Boolean($('#enableOperatioDraw').prop('checked')) ? 4 : 0;
  var zoom = Boolean($('#enableOperatioZoom').prop('checked')) ? 8 : 0;
  var val = none | scroll | draw | zoom || 14;
  console.log('setWhiteboardOperationMode', val);
  zegoWhiteboardView.setWhiteboardOperationMode(val);
});

$('#destoryAll').click(function () {
  zegoWhiteboard.getViewList().then((wbViewList) => {
    wbViewList.forEach((item) => {
      zegoWhiteboard.destroyView(item);
    });
  });
});

async function getViewList() {
  try {
    zegoWhiteboardViewList = await zegoWhiteboard.getViewList();
    zegoWhiteboardViewList.reverse();
    updateRemoteView();
  } catch (error) {
    console.log(error);
  }
}

// sheet列表
function getAllSheet(res) {
  const { sheets, fileID } = res;
  const excelSheetHtml = sheets.map((sheet, ind) => {
    return '<option value=' + fileID + ',' + sheet + '>' + sheet + '</option>';
  });
  $('#excelView').html('<option>--</option>' + excelSheetHtml.join(''));
  $('#excelView').val(fileID + ',' + res.fileName);
}

// 创建其余的白板sheet
async function createRestSheetWb(res) {
  const { sheets, fileID } = res;
  for (const sheetName of sheets) {
    const ind = sheets.indexOf(sheetName);
    if (ind > 0) {
      const sheetWbView = await zegoWhiteboard.createView({
        roomID: zegoRoomInfo.roomid,
        name: res.name,
        aspectWidth: res.width,
        aspectHeight: res.height,
        pageCount: res.pageCount,
        fileInfo: {
          fileID: res.fileID,
          fileName: sheetName,
          fileType: res.fileType,
          authKey: res.authKey
        }
      });
      zegoWhiteboardViewList.unshift(sheetWbView);
      // 更新白板列表
      updateRemoteView();
    }
  }
}

function updateRemoteView() {
  if (zegoWhiteboardViewList.length) {
    const optionsList = [];
    let noReatIds = {}; // 不重复的fileId列表
    zegoWhiteboardViewList.forEach(function (wbViewItem) {
      const id = wbViewItem.getID();
      const fileInfo = wbViewItem.getFileInfo();
      // 纯白板
      if (!fileInfo) {
        optionsList.push('<option value=' + id + '>' + id + '-' + wbViewItem.getName() + '</option>');
      }
      // 文件白板
      // 过滤excel(同一个excel文件下多个sheet,只存一个options)
      if (fileInfo && !noReatIds[fileInfo.fileID]) {
        noReatIds[fileInfo.fileID] = 1;
        optionsList.push('<option value=' + id + '>' + id + '-' + wbViewItem.getName() + '</option>');
      }
    });
    $('#remoteView').html('<option>--</option>' + optionsList.join(''));
    if (zegoWhiteboardView) {
      $('#remoteView').val(zegoWhiteboardView.getID());
    }
  } else {
    $('#remoteView').html('');
  }
}

/**
 * @desc: 加载远程白板
 */
async function selectRemoteView(whiteboardID) {
  var id = whiteboardID ? whiteboardID : $('#remoteView').val();
  initSheetList();
  if (id) {
    zegoWhiteboardView = zegoWhiteboardViewList.filter(function (v) {
      return id === v.getID();
    })[0];
    if (!zegoWhiteboardView) {
      toast('远端白板不存在');
      return;
    }

    var fileInfo = zegoWhiteboardView.getFileInfo();
    if (fileInfo) {
      console.warn('fileInfo', fileInfo);
      fileID = fileInfo.fileID;
      isRemote = true;
      zegoDocsView = zegoDocs.createView(parentId, id, fileInfo.fileName);
      zegoDocsView.loadFile(fileInfo.fileID, fileInfo.authKey);
      console.log('docsview selectRemoteView', fileInfo.fileName, id);
    } else {
      zegoDocsView = null;
      await zegoWhiteboard.attachView(zegoWhiteboardView, parentId);
      setOperationModeState();
    }
    $('#filename').html(zegoWhiteboardView.getName());
    $('#curPage').html(zegoWhiteboardView.getCurrentPage());
    $('#totalPage').html(zegoWhiteboardView.getPageCount());
  }
}

// 关联白板操作模式状态
function setOperationModeState() {
  $('#disableOperatio').prop('checked', false);
  $('#enableOperatioScroll').prop('checked', false);
  $('#enableOperatioDraw').prop('checked', false);
  $('#enableOperatioZoom').prop('checked', false);
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
function setZoom() {
  var val = Number($('#zoom').val() / 100).toFixed(2);
  zegoWhiteboardView && zegoWhiteboardView.setScaleFactor(val);
}

// 销毁view
function destroyView() {
  zegoWhiteboard.destroyView(zegoWhiteboardView);
}

// 销毁全部白板
function destroyAllView() {
  zegoWhiteboard.getViewList().then((wbViewList) => {
    wbViewList.forEach((item) => {
      zegoWhiteboard.destroyView(item);
    });
  });
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
// 上传静态文件
function uploadStaticHandle() {
  myFile = event.target.files[0];
  if (!zegoDocs) {
    toast('请先初始化');
    return;
  }
  if (!myFile) {
    toast('请先选择文件');
    return;
  }
  zegoDocs
    .uploadFile(myFile, 3, { _seq: ++_seq })
    .then(async (uploadResult) => {
      toast('上传成功');
      myFile = null;
      console.log('uploadResult', uploadResult);
      createFileView(uploadResult.fileID);
      $('#staticFile').val('');
    })
    .catch((error) => {
      console.log(error);
      if (error.message) toast(`文件上传失败，${error.message}。`);
      $('#staticFile').val('');
    });
}
// 上传动态文件
function uploadDynamicHandle() {
  console.warn('上传动态文件');
  myFile = event.target.files[0];
  if (!zegoDocs) {
    toast('请先初始化');
    return;
  }
  if (!myFile) {
    toast('请先选择文件');
    return;
  }
  zegoDocs
    .uploadFile(myFile, 6, { _seq: ++_seq })
    .then(async (uploadResult) => {
      toast('上传成功');
      createFileView(uploadResult.fileID);
      $('#dynamicFile').val('');
    })
    .catch((error) => {
      console.log(error);
      if (error.message) toast(`文件上传失败，${error.message}。`);
      $('#dynamicFile').val('');
    });
}

function selectExcel(event) {
  const value = $('#excelView').val();
  const fileID = value.split(',')[0];
  const sheetName = value.split(',')[1];
  createFileView(fileID, sheetName);
}
// 创建文件
async function createFileView(fileID, sheetName) {
  fileID = fileID || $('#fileID').val();
  if (!fileID) return;
  const matchedView = zegoWhiteboardViewList.find((item) => {
    const fileInfo = item.getFileInfo() || {};
    // 寻找excel中匹配的sheet文件。
    if (sheetName) return fileInfo && fileInfo.fileID === fileID && fileInfo.fileName === sheetName;
    return fileID === fileInfo.fileID;
  });
  // 如果是已经存在的文件白板
  if (!!matchedView) {
    return selectRemoteView(matchedView.whiteboardID);
  }

  // createView 入参一个sheetName，表示想要loadFile该sheet。
  zegoDocsView = zegoDocs.createView(parentId, undefined, sheetName);
  try {
    const res = await zegoDocsView.loadFile(fileID, '');
  } catch (e) {
    console.error(e);
  }
}

// 创建文件白板
async function createFileWBView(res) {
  if (isRemote) {
    await zegoWhiteboard.attachView(zegoWhiteboardView, res.viewID);
    setOperationModeState();
    isRemote = false;
  } else {
    try {
      // 提前创建其余的所有sheet 的白板view
      if (res.fileType === 4) {
        await createRestSheetWb(res);
      }
      const view = await zegoWhiteboard.createView({
        roomID: zegoRoomInfo.roomid,
        name: res.fileType === 4 ? res.name : res.fileName,
        aspectWidth: res.width,
        aspectHeight: res.height,
        pageCount: res.pageCount,
        fileInfo: {
          fileID: res.fileID,
          fileName: res.fileName,
          fileType: res.fileType,
          authKey: res.authKey
        }
      });
      zegoWhiteboardViewList.unshift(view);
      zegoWhiteboardView = view;
      // 更新白板列表
      updateRemoteView();
      await zegoWhiteboard.attachView(zegoWhiteboardView, res.viewID);
      setOperationModeState();
    } catch (error) {
      console.error('createFileWBView error', error);
    }
  }

  initSheetList();
  if (res.fileType === 4) {
    getAllSheet(res);
  }
}

function initSheetList() {
  // 初始化sheet列表
  $('#excelView').html('<option>--</option>');
}

//取消上传
async function cancelUpload() {
  var fn = zegoDocs.cancelUpload || zegoDocs.cancelUploadFile;
  var res = await fn.call(zegoDocs, fileHash);
  if (res.code === 0) toast('取消上传操作成功');
  $('#staticFile').val('');
  $('#dynamicFile').val('');
}

// 删除文件
function deleteFile() {}

//获取缩略图
function getThumbnailUrlList() {
  if (!zegoDocsView) {
    toast('请先加载文件');
    return;
  }
  const type = zegoDocsView.getFileType();
  // 仅支持PDF，PPT，动态PPT 文件格式
  const supportType = [1, 8, 512];
  if (supportType.includes(type)) {
    var thumbnailUrlList = zegoDocsView.getThumbnailUrlList();
    if (thumbnailUrlList.length > 0) {
      var imgs = thumbnailUrlList.map(function (v, i) {
        return '<span>' + (i + 1) + '</span><img src="' + v + '"></img>';
      });
      $('#thumbnail-list').html(imgs.join(''));
      $('.thumbnail').show();
    }
  } else {
    toast('获取缩略图仅支持PDF，PPT，动态PPT 文件格式');
  }
}

function closeThumbnail() {
  $('.thumbnail').hide();
}

// 添加图片-本地
var myLocalIMG;
function uploadLocalIMG() {
  myLocalIMG = event.target.files[0];
}

$('#localImg').blur(function () {
  var str = $('#localImg').val().trim();
  if (str) {
    $('#uploadFile').val('');
    myLocalIMG = str;
  }
});

/**
 * @desc: 添加图片
 * @param {0本地，1网络} type
 */
function addImage(type) {
  if (!zegoWhiteboardView) return;
  var positionX, positionY, address;
  if (type == 1) {
    positionX = 0;
    positionY = 0;
    address = $('#netImg').val().trim();
  } else {
    positionX = $('#imgpositionX').val();
    positionY = $('#imgpositionY').val();
    positionX = positionX && +positionX;
    positionY = positionY && +positionY;
    address = myLocalIMG || '';
  }
  console.warn(type, positionX, positionY, address);
  zegoWhiteboardView
    .addImage(type, positionX, positionY, address, function (res) {
      console.log('上传图片进度', res);
    })
    .then((res) => {
      if (type == 1) {
        $('#netImg').val('');
        updateNetImgList(res);
        updateNetOption();
      }
    })
    .catch((e) => {
      if (e && e.code) {
        console.error(e);
        toast(e.code + '：' + (imageErrorTipsMap[e.code] || e.msg));
      }
    });
}
// 处理图片名字&更新本地数据
function updateNetImgList(res) {
  var tempImgName = res.replace(/(.*\/)*([^.]+).*/gi, '$2');
  console.warn(res.replace(/(.*\/)*([^.]+).*/gi, '$2'));
  netImgUrlList.unshift({ id: res, name: tempImgName });
}
// 更新 自定义图片、白板背景图 下拉框数据
function updateNetOption() {
  var options = netImgUrlList.map(function (v) {
    return '<option value="' + v.id + '">' + v.name + '</option>';
  });
  $('#netImgSelect').html(options.join(''));
  options = netBgImgUrlList.map(function (v) {
    return '<option value="' + v.id + '">' + v.name + '</option>';
  });
  $('#whiteboardBgImgUrlSelect').html(options.join(''));
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

function toast(message) {
  $('.toast').toast('show');
  $('#error_msg').html(message);
}

function onKeydownHandle() {
  window.addEventListener('keydown', (event) => {
    var e = event || window.event || arguments.callee.caller.arguments[0];
    if (!e) return;
    switch (e.keyCode) {
      case 8: // 监听backspace按键，批量删除选中图元
      case 46: // 监听Delete按键，批量删除选中图元
        deleteSelectedGraphics();
        break;
      default:
        break;
    }
  });
}

function resetParentWidthHeight() {
  const dom = document.getElementById(parentId);
  let w = dom.parentNode.clientWidth - 32;
  w = Math.min(w, 1067);
  h = Math.ceil((w * 9) / 16);
  dom.style.cssText += `width:${w}px;height:${h}px;`;
}

async function saveImage() {
  const wbname = zegoWhiteboardView.getName();
  const wbPageIndex = zegoWhiteboardView.getCurrentPage();
  const data = await zegoWhiteboardView.snapshot({ userData: '11' });
  let filename = `${wbname}`;
  const save_link = document.createElementNS('http://www.w3.org/1999/xhtml', 'a');
  save_link.href = data.image;
  let downloadFilename = filename.endsWith('png') ? filename : filename + '.png';
  save_link.download = downloadFilename;

  const event = document.createEvent('MouseEvents');
  event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
  save_link.dispatchEvent(event);
}
// 添加背景图片-本地
var myLocalBGIMG;
function uploadLocalBGIMG() {
  myLocalBGIMG = event.target.files[0];
}

function setWhiteboardBg(type) {
  var model = $('#whiteboardBgImgModelSelect').val();
  if (!model) {
    toast('渲染类型不能为空！');
    return;
  }
  var url = $('#whiteboardBgImg').val() || $('#whiteboardBgImgUrlSelect').val();
  zegoWhiteboardView
    .setBackgroundImage(type === 1 ? url : myLocalBGIMG, +model, function (res) {
      console.log('设置白板背景图进度', res);
    })
    .then((res) => {
      console.log(res);
    })
    .catch((e) => {
      if (e && e.code) {
        console.error(e);
        toast(e.code + '：' + (imageErrorTipsMap[e.code] || e.msg));
      }
    });
}

function clearBackgroundImage() {
  zegoWhiteboardView.clearBackgroundImage();
}
