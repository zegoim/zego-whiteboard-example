package im.zego.whiteboardexample.widget.whiteboard

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.util.AppLogger
import im.zego.whiteboardexample.util.ToastUtils
import im.zego.whiteboardexample.widget.whiteboard.callback.IWhiteboardSizeChangedListener
import im.zego.zegodocs.*
import im.zego.zegowhiteboard.ZegoWhiteboardManager
import im.zego.zegowhiteboard.ZegoWhiteboardView
import im.zego.zegowhiteboard.ZegoWhiteboardViewImageFitMode
import im.zego.zegowhiteboard.ZegoWhiteboardViewImageType
import im.zego.zegowhiteboard.callback.IZegoWhiteboardExecuteListener
import im.zego.zegowhiteboard.callback.IZegoWhiteboardViewScaleListener
import im.zego.zegowhiteboard.callback.IZegoWhiteboardViewScrollListener
import im.zego.zegowhiteboard.model.ZegoWhiteboardViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.round

/**
 * 白板容器, 一个文件对应一个容器，包含 docsview 和白板
 * 如果是 excel，一个 docsView 可能会对应多个白板
 * 其他类型的文件，一个 docsView 只有一个白板
 */
class ZegoWhiteboardViewHolder : FrameLayout {
    val TAG = "WhiteboardViewHolder"

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var whiteboardViewList: MutableList<ZegoWhiteboardView> = mutableListOf()
    private var zegoDocsView: ZegoDocsView? = null
    private var fileLoadSuccessed = false
    private var whiteboardViewAddFinished = false
    private var currentWhiteboardSize: Size = Size(0, 0)

    private var internalScrollListener: IZegoWhiteboardViewScrollListener =
        IZegoWhiteboardViewScrollListener { horizontalPercent, verticalPercent ->
            outScrollListener?.onScroll(horizontalPercent, verticalPercent)
        }
    private var outScrollListener: IZegoWhiteboardViewScrollListener? = null
    private var reloadFinishListener: IWhiteboardSizeChangedListener? = null

    /**
     * 当前显示的白板ID
     */
    var currentWhiteboardID = 0L
        set(value) {
            field = value
            AppLogger.d(TAG, "set currentWhiteboardID:${value}")
            var selectedView: ZegoWhiteboardView? = null
            whiteboardViewList.forEach {
                val viewModel = it.getWhiteboardViewModel()
                if (viewModel.whiteboardID == value) {
                    it.visibility = View.VISIBLE
                    selectedView = it
                } else {
                    it.visibility = View.GONE
                }
                AppLogger.d(
                    TAG,
                    "whiteboardViewList: ${viewModel.whiteboardID}:${viewModel.fileInfo.fileName}"
                )
            }
            selectedView?.let {
                AppLogger.d(TAG, "selectedView:${it.whiteboardViewModel.fileInfo.fileName}")
                val viewModel = it.whiteboardViewModel
                if (zegoDocsView != null && isExcel()) {
                    val fileName = viewModel.fileInfo.fileName
                    val sheetIndex = getExcelSheetNameList().indexOf(fileName)
                    zegoDocsView!!.switchSheet(sheetIndex, IZegoDocsViewLoadListener { loadResult ->
                        AppLogger.d(TAG, "loadResult = $loadResult")
                        if (loadResult == 0) {
                            AppLogger.i(
                                TAG, "switchSheet,sheetIndex:$sheetIndex," +
                                        "visibleSize:${zegoDocsView!!.getVisibleSize()}" +
                                        "contentSize:${zegoDocsView!!.getContentSize()}"
                            )
                            viewModel.aspectWidth = zegoDocsView!!.getContentSize().width
                            viewModel.aspectHeight = zegoDocsView!!.getContentSize().height

                            connectDocsViewAndWhiteboardView(it)

                            zegoDocsView!!.scaleDocsView(
                                it.getScaleFactor(),
                                it.getScaleOffsetX(),
                                it.getScaleOffsetY()
                            )
                        }
                    })
                }
            }
        }

    fun setDocsScaleEnable(selected: Boolean) {
        zegoDocsView?.isScaleEnable = selected
    }

    private var currentWhiteboardView: ZegoWhiteboardView?
        private set(value) {}
        get() {
            return whiteboardViewList.firstOrNull {
                it.whiteboardViewModel.whiteboardID == currentWhiteboardID
            }
        }

    fun hasWhiteboardID(whiteboardID: Long): Boolean {
        val firstOrNull = whiteboardViewList.firstOrNull {
            it.whiteboardViewModel.whiteboardID == whiteboardID
        }
        return firstOrNull != null
    }

    fun getWhiteboardIDList(): List<Long> {
        return whiteboardViewList.map { it.whiteboardViewModel.whiteboardID }
    }

    fun isFileWhiteboard(): Boolean {
        val fileID = getFileID()
        return fileID != null && fileID.isNotBlank()
    }

    fun isPureWhiteboard(): Boolean {
        return getFileID().isNullOrBlank()
    }

    fun getFileID(): String? {
        return when {
            zegoDocsView != null -> {
                zegoDocsView!!.getFileID()
            }
            whiteboardViewList.isNotEmpty() -> {
                val fileInfo = whiteboardViewList.first().getWhiteboardViewModel().fileInfo
                if (fileInfo.fileID.isEmpty()) return null else fileInfo.fileID
            }
            else -> {
                null
            }
        }
    }

    fun getExcelSheetNameList(): ArrayList<String> {
        return if (isExcel() && isDocsViewLoadSuccessed()) {
            zegoDocsView!!.sheetNameList
        } else {
            ArrayList()
        }
    }

    fun switchExcelSheet(sheetIndex: Int, selectResult: (String, Long) -> Unit) {
        if (sheetIndex < 0 || sheetIndex > getExcelSheetNameList().size - 1) {
            return
        }
        if (isExcel() && isDocsViewLoadSuccessed()) {
            val firstOrNull = whiteboardViewList.firstOrNull {
                it.whiteboardViewModel.fileInfo.fileName == getExcelSheetNameList()[sheetIndex]
            }
            firstOrNull?.let {
                val model = it.whiteboardViewModel
                AppLogger.i(
                    TAG,
                    "selectSheet,fileName：${model.fileInfo.fileName}，${model.whiteboardID}"
                )
                currentWhiteboardID = model.whiteboardID
                selectResult(model.fileInfo.fileName, model.whiteboardID)
            }
        }
    }

    fun getVisibleSize() = currentWhiteboardSize

    fun isExcel(): Boolean {
        return getFileType() == ZegoDocsViewConstants.ZegoDocsViewFileTypeELS
    }

    private fun isPPT(): Boolean {
        return getFileType() == ZegoDocsViewConstants.ZegoDocsViewFileTypePPT
    }

    private fun isDisplayedByWebView(): Boolean {
        return getFileType() == ZegoDocsViewConstants.ZegoDocsViewFileTypeDynamicPPTH5 ||
                getFileType() == ZegoDocsViewConstants.ZegoDocsViewFileTypeCustomH5
    }

    fun isSupportClickTool() = isDisplayedByWebView()

    fun isSupportStepControl() = isDisplayedByWebView()

    fun getThumbnailUrlList(): ArrayList<String> {
        val urls = ArrayList<String>()
        if (zegoDocsView != null) {
            return zegoDocsView!!.getThumbnailUrlList()
        }
        return urls
    }


    private fun getFileType(): Int {
        return when {
            zegoDocsView != null && isDocsViewLoadSuccessed() -> {
                zegoDocsView!!.getFileType()
            }
            whiteboardViewList.isNotEmpty() -> {
                // 任意一个白板，包含的是同样的 fileInfo
                whiteboardViewList.first().getWhiteboardViewModel().fileInfo.fileType
            }
            else -> {
                ZegoDocsViewConstants.ZegoDocsViewFileTypeUnknown
            }
        }
    }

    fun getCurrentWhiteboardName(): String? {
        return getCurrentWhiteboardModel().name
    }

    /**
     * 获取当前展示的sheet名称
     */
    fun getCurrentSheetName() : String? {
        return if (!isExcel()) {
            null
        } else {
            val whiteboardViewModel = getCurrentWhiteboardModel()
            whiteboardViewModel.fileInfo.fileName
        }
    }

    fun getCurrentWhiteboardModel(): ZegoWhiteboardViewModel {
        return currentWhiteboardView!!.whiteboardViewModel
    }

    fun getCurrentWhiteboardMsg(): String {
        return "modelMessage:name:${getCurrentWhiteboardModel().name},whiteboardID:${getCurrentWhiteboardModel().whiteboardID}," +
                "fileInfo:${getCurrentWhiteboardModel().fileInfo.fileName}" +
                "hori:${getCurrentWhiteboardModel().horizontalScrollPercent},vertical:${getCurrentWhiteboardModel().verticalScrollPercent}"
    }

    fun inputText(listener:IZegoWhiteboardExecuteListener) {
        currentWhiteboardView?.addTextEdit(listener)
    }

    fun addText(text: String, positionX: Int, positionY: Int, listener: IZegoWhiteboardExecuteListener) {
        currentWhiteboardView?.addText(text, positionX, positionY,listener)
    }

    fun undo() {
        currentWhiteboardView?.undo()
    }

    fun redo() {
        currentWhiteboardView?.redo()
    }

    fun clearCurrentPage(listener:IZegoWhiteboardExecuteListener) {
        val curPageRectF = if (isPureWhiteboard()) {
            currentWhiteboardView?.let {
                val width = it.width.toFloat()
                val height = it.height.toFloat()
                val pageOffsetX = width * (getCurrentPage() - 1)
                val pageOffsetY = 0F

                RectF(
                    pageOffsetX,
                    pageOffsetY,
                    (pageOffsetX + width),
                    (pageOffsetY + height)
                )
            }

        } else {
            zegoDocsView!!.currentPageInfo!!.rect
        }

        AppLogger.i(TAG, "clearCurrentPage: ${curPageRectF.toString()}")
        currentWhiteboardView?.clear(curPageRectF!!,listener)
    }

    fun setOperationMode(opMode:Int) {
        currentWhiteboardView?.setWhiteboardOperationMode(opMode)
    }

    private fun scrollTo(horizontalPercent: Float, verticalPercent: Float, currentStep: Int = 1,listener: IZegoWhiteboardExecuteListener?) {
        AppLogger.d(
            TAG,
            "scrollTo() called with: horizontalPercent = $horizontalPercent, verticalPercent = $verticalPercent, currentStep = $currentStep"
        )
        if (getFileID() != null) {
            if (isDocsViewLoadSuccessed()) {
                currentWhiteboardView?.scrollTo(horizontalPercent, verticalPercent, currentStep,listener)
            }
        } else {
            currentWhiteboardView?.scrollTo(horizontalPercent, verticalPercent, currentStep,listener)
        }
        internalScrollListener.onScroll(horizontalPercent, verticalPercent)
    }

    fun clearAllPage(listener:IZegoWhiteboardExecuteListener) {
        currentWhiteboardView?.clear(listener)
    }

    private fun addWhiteboardView(zegoWhiteboardView: ZegoWhiteboardView) {
        val model = zegoWhiteboardView.whiteboardViewModel
        AppLogger.i(
            TAG, "addWhiteboardView:${model.whiteboardID},${model.name},${model.fileInfo.fileName}"
        )
        this.whiteboardViewList.add(zegoWhiteboardView)

        addView(
            zegoWhiteboardView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).also {
                it.gravity = Gravity.CENTER
            }
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged() called with: w = $w, h = $h, oldw = $oldw, oldh = $oldh")
//        if (oldw != 0 && oldh != 0) {
            reload(Size(w, h))
//        }
    }

    private fun connectDocsViewAndWhiteboardView(zegoWhiteboardView: ZegoWhiteboardView) {
        zegoDocsView?.let { docsview ->
            Log.d(TAG, "connectDocsViewAndWhiteboardView VisibleSize = ${docsview.visibleSize.width},${docsview.visibleSize.height}")
            // 修正各平台的差异，自己的白板和自己的文件大小一致，不管别人传过来的宽高比
//            val viewModel = zegoWhiteboardView.whiteboardViewModel
//            viewModel.aspectWidth = zegoDocsView!!.getContentSize().width
//            viewModel.aspectHeight = zegoDocsView!!.getContentSize().height

            if (docsview.getVisibleSize().height != 0 || docsview.getVisibleSize().width != 0) {
                zegoWhiteboardView.setVisibleRegion(zegoDocsView!!.getVisibleSize())
            }

            zegoWhiteboardView.setScrollListener { horizontalPercent, verticalPercent ->
                AppLogger.d(
                    TAG,
                    "ScrollListener.onScroll,horizontalPercent:${horizontalPercent},verticalPercent:${verticalPercent}"
                )
                if (isDisplayedByWebView()) {
                    val page = getRelativePageInWebView(verticalPercent)
                    val model = zegoWhiteboardView.whiteboardViewModel
                    val stepChanged = docsview.currentStep != model.pptStep
                    val pageChanged = docsview.currentPage != page
                    AppLogger.i(
                        TAG,
                        "page:${page},step:${model.pptStep},stepChanged:$stepChanged,pageChanged:$pageChanged"
                    )
                    docsview.flipPage(page, model.pptStep) { result ->
                        AppLogger.i(TAG, "docsview.flipPage() : result = $result")
                    }
                    internalScrollListener.onScroll(horizontalPercent, verticalPercent)
                } else {
                    docsview.scrollTo(verticalPercent) { complete ->
                        Log.d(
                            TAG,
                            "setScrollListener() called with: complete = $complete,docsview:${docsview.verticalPercent}"
                        )
                        internalScrollListener.onScroll(0f, verticalPercent)
                    }
                }

            }

            if (isDisplayedByWebView()) {
                // 这些需要在第一页加载出来才能
                // 对于动态PPT，H5可以自行播放动画，需要同步给白板，再同步给其他端的用户
                docsview.setAnimationListener(IZegoDocsViewAnimationListener {
                    if (windowVisibility == View.VISIBLE) {
                        zegoWhiteboardView.playAnimation(it)
                    }
                })

                docsview.setStepChangeListener(object : IZegoDocsViewCurrentStepChangeListener {
                    override fun onChanged() {
                    }

                    override fun onStepChangeForClick() {
                        // 动态PPT，直接点击H5，触发翻页、步数变化
                        AppLogger.d(TAG, "onStepChangeForClick() called，scroll to ${docsview.verticalPercent}")
                        scrollTo(0f, docsview.verticalPercent, docsview.currentStep){
                            if(it != 0){
                                ToastUtils.showCenterToast("errorCode:$it")
                            }
                        }
                    }
                })
            }
            // 对于动态PPT，其他端有播放动画，需要同步给docsView进行播放动画
            zegoWhiteboardView.setAnimationListener { animation ->
                AppLogger.d(TAG, "setAnimationListener() called")
                docsview.playAnimation(animation)
            }
            zegoWhiteboardView.setScaleListener(IZegoWhiteboardViewScaleListener { scaleFactor, transX, transY ->
//            Logger.d(TAG,"scaleFactor:$scaleFactor,transX:$transX,transY:$transY")
                docsview.scaleDocsView(scaleFactor, transX, transY)
            })
        }

        post {
            val model = zegoWhiteboardView.whiteboardViewModel
            val horPercent = model.horizontalScrollPercent
            val verPercent = model.verticalScrollPercent
            val currentStep = model.pptStep
            if (isDisplayedByWebView() && isDocsViewLoadSuccessed()) {
                // 此处是首次加载，要跳转到到文件对应页。完成后需要判断是否播动画
                zegoDocsView?.let {
                    val targetPage = getRelativePageInWebView(verPercent)
                    AppLogger.d(TAG, "horPercent:$horPercent,verPercent:$verPercent,targetPage:$targetPage,currentStep:$currentStep")
                    it.flipPage(targetPage, currentStep) { result ->
                        if (result) {
                            zegoWhiteboardView.whiteboardViewModel.h5Extra?.let { h5Extra ->
                                it.playAnimation(h5Extra)
                            }
                        }
                    }
                }
            } else {
                zegoDocsView?.scrollTo(verPercent){complete ->
                    Log.d(
                            TAG,
                            "connectDocsViewAndWhiteboardView() called with: complete = $complete,docsview:${zegoDocsView?.verticalPercent}"
                    )
                    internalScrollListener.onScroll(0f, verPercent)
                }

            }
        }
    }

    private fun getRelativePageInWebView(verticalPercent: Float): Int {
        return if (isDisplayedByWebView()) {
            if (isDocsViewLoadSuccessed()) {
                val page = round(verticalPercent * zegoDocsView!!.pageCount).toInt() + 1
                page
            } else {
                // 没加载完成不能传1，否则第一页没有这么多step，会导致循环
                0
            }
        } else {
            throw IllegalArgumentException("only used for dynamic PPT")
        }
    }

    private fun onPureWhiteboardViewAdded(zegoWhiteboardView: ZegoWhiteboardView) {
        val model = zegoWhiteboardView.getWhiteboardViewModel()
        currentWhiteboardID = model.whiteboardID
        zegoWhiteboardView.setScrollListener(IZegoWhiteboardViewScrollListener { horizontalPercent, verticalPercent ->
            internalScrollListener.onScroll(horizontalPercent, verticalPercent)
        })
    }

    /**
     * 添加纯白板
     */
    fun onReceivePureWhiteboardView(zegoWhiteboardView: ZegoWhiteboardView) {
        zegoWhiteboardView.setBackgroundColor(resources.getColor(R.color.whiteboard_background_color))
        addWhiteboardView(zegoWhiteboardView)
        onPureWhiteboardViewAdded(zegoWhiteboardView)
        whiteboardViewAddFinished = true
    }

    /**
     * 创建纯白板，aspectWidth，aspectHeight:宽高比
     */
    fun createPureWhiteboardView(
        aspectWidth: Int, aspectHeight: Int, pageCount: Int,
        whiteboardName: String, requestResult: (Int) -> Unit
    ) {
        val data = ZegoWhiteboardViewModel()
        data.aspectHeight = aspectHeight
        data.aspectWidth = aspectWidth
        data.name = subString(whiteboardName)
        data.pageCount = pageCount
        ZegoWhiteboardManager.getInstance().createWhiteboardView(data)
        { errorCode, zegoWhiteboardView ->
            AppLogger.d(
                TAG,
                "createPureWhiteboardView,name:${data.name},errorCode:${errorCode}"
            )
            if (errorCode == 0 && zegoWhiteboardView != null) {
                onReceivePureWhiteboardView(zegoWhiteboardView)
            } else {
                Toast.makeText(context, "创建白板失败，错误码:$errorCode", Toast.LENGTH_LONG).show()
            }
            requestResult.invoke(errorCode)
        }
    }

    /**
     * 创建白板的name字段对长度有限制，这里统一限制为 <=128 字符
     */
    private fun subString(content: String): String {
        val bytes: ByteArray = content.toByteArray()
        return if (bytes.size > 128) {
            // 将字符串限制为小于128字符
            var newString = String(bytes.copyOfRange(0, 128))
            // 但 bytes -> string 过程中，string会有特殊处理，将缺失的byte用特殊符号补齐
            while (newString.toByteArray().size > 128){
                // 这样导致 string 的字符可能又大于了128字符
                // 所以我们丢弃掉 string 的最后一个字，重新计算
                newString = newString.dropLast(1)
            }
            return newString
        } else {
            content
        }
    }

    fun destroyWhiteboardView(requestResult: (Int) -> Unit) {
        if (isExcel()) {
            var count = whiteboardViewList.size
            var success = true
            var code = 0
            whiteboardViewList.forEach {
                val whiteboardID = it.getWhiteboardViewModel().whiteboardID
                ZegoWhiteboardManager.getInstance().destroyWhiteboardView(whiteboardID)
                { errorCode, _ ->
                    //因为所有的回调都是在主线程，所以不用考虑多线程
                    count--
                    if (errorCode != 0) {
                        success = false
                        code = errorCode
                    }
                    if (count == 0) {
                        if (!success) {
                            Toast.makeText(context, "删除白板失败:错误码:$code", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            unloadFile()
                            fileLoadSuccessed = false
                        }
                        requestResult.invoke(errorCode)
                    }
                }
            }
        } else {
            ZegoWhiteboardManager.getInstance().destroyWhiteboardView(currentWhiteboardID)
            { errorCode, _ ->
                if (errorCode != 0) {
                    Toast.makeText(context, "删除白板失败:错误码:$errorCode", Toast.LENGTH_LONG).show()
                } else {
                    unloadFile()
                    fileLoadSuccessed = false
                }
                requestResult.invoke(errorCode)
            }
        }
    }

    /**
     * 收到文件白板
     */
    fun onReceiveFileWhiteboard(
        estimatedSize: Size,
        zegoWhiteboardView: ZegoWhiteboardView,
        processResult: (Int, ZegoWhiteboardViewHolder) -> Unit
    ) {
        val fileInfo = zegoWhiteboardView.whiteboardViewModel.fileInfo
        AppLogger.d(
            TAG,
            "onReceiveFileWhiteboard() called with: estimatedSize = $estimatedSize, zegoWhiteboardView = ${fileInfo.fileName}"
        )
        addWhiteboardView(zegoWhiteboardView)
        if (zegoDocsView != null) {
            zegoWhiteboardView.visibility = View.GONE
            processResult(0, this)
        } else {
            val fileID = fileInfo.fileID
            visibility = View.GONE
            currentWhiteboardID = zegoWhiteboardView.whiteboardViewModel.whiteboardID
            createDocsViewAndLoadFile(fileID,estimatedSize) { errorCode: Int, _: ZegoDocsView ->
                if (errorCode == 0) {
                    // excel要等到load完才设置，因为要 switchSheet
                    if (isExcel()) {
                        currentWhiteboardID =
                            zegoWhiteboardView.getWhiteboardViewModel().whiteboardID
                    } else {
                        connectDocsViewAndWhiteboardView(zegoWhiteboardView)
                    }
                    processResult.invoke(errorCode, this)
                } else {
                    Toast.makeText(context, "加载文件失败，错误代码 $errorCode", Toast.LENGTH_LONG).show()
                    processResult.invoke(errorCode, this)
                }
            }
        }
    }

    /**
     * 加载白板view
     */
    private fun createDocsViewAndLoadFile(
        fileID: String,size:Size,
        requestResult: (Int, ZegoDocsView) -> Unit
    ) {
        AppLogger.i(
            TAG,
            "loadFileWhiteBoardView,start loadFile fileID:${fileID}"
        )
        val docsView = ZegoDocsView(context)
        this.zegoDocsView = docsView
        docsView.setEstimatedSize(size.width, size.height)
        addView(
            zegoDocsView, 0, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        zegoDocsView?.loadFile(fileID, "") { errorCode: Int ->
            fileLoadSuccessed = errorCode == 0
            if (errorCode == 0) {
                AppLogger.i(
                    TAG,
                    "loadDocsFile fileID:${fileID} success,getVisibleSize:${zegoDocsView!!.visibleSize}," +
                            "contentSize:${zegoDocsView!!.contentSize}," + "name:${zegoDocsView!!.fileName}," +
                            "sheetList:${zegoDocsView!!.sheetNameList}"
                )
                zegoDocsView!!.setBackgroundColor(resources.getColor(R.color.whiteboard_background_color))
            } else {
                AppLogger.i(
                    TAG,
                    "loadDocsFile fileID:${fileID} failed，errorCode：${errorCode}"
                )
            }
            requestResult.invoke(errorCode, zegoDocsView!!)
        }
    }

    fun createDocsAndWhiteBoardView(
        fileID: String, size:Size,createResult: (Int) -> Unit
    ) {
        createDocsViewAndLoadFile(fileID,size)
        { errorCode, docsView ->
            if (errorCode == 0) {
                if (isExcel()) {
                    createExcelWhiteboardViewList(docsView, createResult)
                } else {
                    createWhiteBoardViewInner(docsView, 0, createResult)
                }
            } else {
                createResult(errorCode)
                Toast.makeText(context, "加载文件失败，错误代码 $errorCode", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun isDocsViewLoadSuccessed(): Boolean {
        return fileLoadSuccessed
    }

    private fun isWhiteboardViewAddFinished(): Boolean {
        return whiteboardViewAddFinished
    }

    private fun createExcelWhiteboardViewList(
        docsView: ZegoDocsView,
        requestResult: (Int) -> Unit
    ) {
        val sheetCount = getExcelSheetNameList().size
        var processCount = 0
        var resultCode = 0
        for (index in 0 until sheetCount) {
            createWhiteBoardViewInner(docsView, index) { code ->
                if (code != 0) {
                    resultCode = code
                }
                processCount++
                if (processCount == sheetCount) {
                    switchExcelSheet(0) { _, _ ->
                        whiteboardViewAddFinished = true
                        requestResult.invoke(resultCode)
                    }
                }
            }
        }
    }

    private fun createWhiteBoardViewInner(
        docsView: ZegoDocsView, index: Int,
        requestResult: (Int) -> Unit
    ) {
        val data = ZegoWhiteboardViewModel()
        data.aspectWidth = docsView.contentSize.width
        data.aspectHeight = docsView.contentSize.height
        data.name = subString(docsView.fileName!!)
        data.pageCount = docsView.pageCount
        data.fileInfo.fileID = docsView.fileID!!
        data.fileInfo.fileType = docsView.fileType
        if (isExcel()) {
            data.fileInfo.fileName = docsView.sheetNameList[index]
        }

        ZegoWhiteboardManager.getInstance().createWhiteboardView(data)
        { errorCode, zegoWhiteboardView ->
            AppLogger.d(
                TAG,
                "createWhiteboardView,name:${data.name},fileName:${data.fileInfo.fileName}"
            )
            if (errorCode == 0 && zegoWhiteboardView != null) {
                addWhiteboardView(zegoWhiteboardView)
                if (!isExcel()) {
                    currentWhiteboardID =
                        zegoWhiteboardView.getWhiteboardViewModel().whiteboardID
                    connectDocsViewAndWhiteboardView(zegoWhiteboardView)
                    whiteboardViewAddFinished = errorCode == 0
                }
            } else {
                Toast.makeText(
                    context,
                    "创建白板失败，错误码:$errorCode",
                    Toast.LENGTH_LONG
                ).show()
            }
            requestResult.invoke(errorCode)
        }
    }

    fun flipToPage(targetPage: Int,listener: IZegoWhiteboardExecuteListener?) {
        AppLogger.i(TAG, "targetPage:${targetPage}")
        if (zegoDocsView != null && getFileID() != null && isDocsViewLoadSuccessed()) {
            zegoDocsView!!.flipPage(targetPage) { result ->
                AppLogger.i(TAG, "it.flipToPage() result:$result")
                if (result) {
                    scrollTo(0f, zegoDocsView!!.getVerticalPercent(),1,listener )
                }
            }
        } else {
            scrollTo((targetPage - 1).toFloat() / getPageCount(), 0f,1,listener)
        }
    }

    /**
     * 此处的page是从1开始的
     */
    fun flipToPrevPage(listener: IZegoWhiteboardExecuteListener?): Int {
        val currentPage = getCurrentPage()
        val targetPage = if (currentPage - 1 <= 0) 1 else currentPage - 1
        if (targetPage != currentPage) {
            flipToPage(targetPage,listener)
        }
        return targetPage
    }

    fun flipToNextPage(listener: IZegoWhiteboardExecuteListener?): Int {
        val currentPage = getCurrentPage()
        val targetPage =
            if (currentPage + 1 > getPageCount()) getPageCount() else currentPage + 1
        if (targetPage != currentPage) {
            flipToPage(targetPage,listener)
        }
        return targetPage
    }

    fun previousStep(listener: IZegoWhiteboardExecuteListener?) {
        AppLogger.d(TAG, "previousStep() called,fileLoadSuccessed:${isDocsViewLoadSuccessed()}")
        if (getFileID() != null && isDisplayedByWebView() && isDocsViewLoadSuccessed()) {
            zegoDocsView?.let {
                it.previousStep(IZegoDocsViewScrollCompleteListener { result ->
                    AppLogger.d(TAG, "previousStep:result = $result")
                    if (result) {
                        scrollTo(0f, it.getVerticalPercent(), it.getCurrentStep(),listener)
                    }
                })
            }
        }
    }

    fun nextStep(listener: IZegoWhiteboardExecuteListener?) {
        AppLogger.i(TAG, "nextStep() called,fileLoadSuccessed:${isDocsViewLoadSuccessed()}")
        if (getFileID() != null && isDisplayedByWebView() && isDocsViewLoadSuccessed()) {
            zegoDocsView?.let {
                it.nextStep(IZegoDocsViewScrollCompleteListener { result ->
                    AppLogger.i(TAG, "nextStep:result = $result")
                    if (result) {
                        scrollTo(0f, it.getVerticalPercent(), it.getCurrentStep(),listener)
                    }
                })
            }
        }
    }

    fun getPageCount(): Int {
        return if (getFileID() != null) {
            zegoDocsView!!.getPageCount()
        } else {
            getCurrentWhiteboardModel().pageCount
        }
    }

    /**
     * 第二页滚动到一半，才认为是第二页
     */
    fun getCurrentPage(): Int {
        return if (isPureWhiteboard()) {
            val percent = currentWhiteboardView!!.getHorizontalPercent()
            val currentPage = round(percent * getPageCount()).toInt() + 1
            AppLogger.i(TAG, "getCurrentPage,percent:${percent},currentPage:${currentPage}")
            if (currentPage < getPageCount()) {
                currentPage
            } else {
                getPageCount()
            }
        } else if (isDisplayedByWebView()) {
            if (currentWhiteboardView == null) {
                1
            } else {
                getRelativePageInWebView(currentWhiteboardView!!.verticalPercent)
            }
        } else {
            zegoDocsView!!.getCurrentPage()
        }
    }

    fun setWhiteboardScrollChangeListener(listener: IZegoWhiteboardViewScrollListener) {
        outScrollListener = listener
    }

    fun setSizeChangedListener(listenerWhiteboard: IWhiteboardSizeChangedListener?) {
        reloadFinishListener = listenerWhiteboard
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 保底处理
        unloadFile()
        fileLoadSuccessed = false
    }

    fun addImage(
        type: ZegoWhiteboardViewImageType,
        address: String,
        positionX: Int,
        positionY: Int,
        addResult: (errorCode: Int) -> Unit
    ) {
        currentWhiteboardView?.addImage(
            type, positionX, positionY, address
        ) {
            addResult(it)
        }
    }

    fun setBackgroundImage(address :String,mode :ZegoWhiteboardViewImageFitMode,result: (errorCode: Int) -> Unit){
        currentWhiteboardView?.setBackgroundImage(address,mode){
            result(it)
        }
    }

    fun clearBackgroundImage(result: (errorCode: Int) -> Unit){
        currentWhiteboardView?.clearBackgroundImage{
            result(it)
        }
    }

    fun clearSelected(listener:IZegoWhiteboardExecuteListener) {
        currentWhiteboardView?.deleteSelectedGraphics(listener)
    }

    /**
     * 停止当前正在播放的视频
     */
    fun stopPlayPPTVideo() {
        if (isDisplayedByWebView()) {
            zegoDocsView?.let {
                it.stopPlay(it.currentPage)
            }
        }
    }

    /**
     * 将文件从视图中卸载
     */
    private fun unloadFile() {
        zegoDocsView?.unloadFile()
    }

    private fun reload(size: Size) {
        if (zegoDocsView != null) {
            zegoDocsView?.let {
                it.reloadFile { loadCode ->
                    if (loadCode == 0) {
                        AppLogger.d(TAG, "reload visibleRegion:${it.visibleSize}")
                        currentWhiteboardSize = it.visibleSize
                        currentWhiteboardView?.setVisibleRegion(it.visibleSize)
                        reloadFinishListener?.onSizeChanged(this)
                    }
                }
            }
        } else {
            val model = getCurrentWhiteboardModel()
            val aspectWidth = model.aspectWidth / model.pageCount.toFloat()
            val aspectHeight = model.aspectHeight
            //宽高比
            val aspectRatio = aspectWidth / aspectHeight
            val showSize = calcShowSize(size, aspectRatio)
            AppLogger.d(TAG, "reload pure whiteboard: aspectRatio=$aspectRatio, showSize=$showSize")
            currentWhiteboardSize = showSize
            currentWhiteboardView?.setVisibleRegion(showSize)
            reloadFinishListener?.onSizeChanged(this)
        }
    }

    private fun calcShowSize(parentSize: Size, aspectRatio: Float): Size {
        return if (aspectRatio > parentSize.width.toFloat() / parentSize.height) {
            // 填充宽
            Size(parentSize.width, (parentSize.width.toFloat() / aspectRatio).toInt())
        } else {
            // 填充高
            Size(ceil(aspectRatio * parentSize.height).toInt(), parentSize.height)
        }
    }

    fun setDocsViewAuthInfo(authInfo: HashMap<String, Int>){
        zegoDocsView?.setOperationAuth(authInfo)
    }
}