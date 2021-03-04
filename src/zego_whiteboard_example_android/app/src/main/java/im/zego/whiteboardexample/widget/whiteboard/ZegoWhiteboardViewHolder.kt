package im.zego.whiteboardexample.widget.whiteboard

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import im.zego.zegodocs.*
import im.zego.zegowhiteboard.ZegoWhiteboardManager
import im.zego.zegowhiteboard.ZegoWhiteboardView
import im.zego.zegowhiteboard.ZegoWhiteboardViewImageType
import im.zego.zegowhiteboard.callback.IZegoWhiteboardViewScaleListener
import im.zego.zegowhiteboard.callback.IZegoWhiteboardViewScrollListener
import im.zego.zegowhiteboard.model.ZegoWhiteboardViewModel
import im.zego.whiteboardexample.util.CONFERENCE_ID
import im.zego.whiteboardexample.util.Logger
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

    // 动态 PPT 是转成 H5 进行加载的，首次会加载第 1 页第 1 步，此时不需要同步给其他端的。通过这个字段来过滤
    private var firstFlipPage = true

    private var internalScrollListener: IZegoWhiteboardViewScrollListener =
        IZegoWhiteboardViewScrollListener { horizontalPercent, verticalPercent ->
            outScrollListener?.onScroll(horizontalPercent, verticalPercent)
        }
    private var outScrollListener: IZegoWhiteboardViewScrollListener? = null

    /**
     * 当前显示的白板ID
     */
    var currentWhiteboardID = 0L
        set(value) {
            field = value
            Logger.d(TAG, "set currentWhiteboardID:${value}")
            var selectedView: ZegoWhiteboardView? = null
            whiteboardViewList.forEach {
                val viewModel = it.getWhiteboardViewModel()
                if (viewModel.whiteboardID == value) {
                    it.visibility = View.VISIBLE
                    selectedView = it
                } else {
                    it.visibility = View.GONE
                }
                Logger.d(
                    TAG,
                    "whiteboardViewList: ${viewModel.whiteboardID}:${viewModel.fileInfo.fileName}"
                )
            }
            selectedView?.let {
                Logger.d(TAG, "selectedView:${it.whiteboardViewModel.fileInfo.fileName}")
                val viewModel = it.whiteboardViewModel
                if (zegoDocsView != null && isExcel()) {
                    val fileName = viewModel.fileInfo.fileName
                    val sheetIndex = getExcelSheetNameList().indexOf(fileName)
                    zegoDocsView!!.switchSheet(sheetIndex, IZegoDocsViewLoadListener { loadResult ->
                        Logger.d(TAG, "loadResult = $loadResult")
                        if (loadResult == 0) {
                            Logger.i(
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
        zegoDocsView?.isScaleEnable = !selected
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
        return getFileID() != null
    }

    fun isPureWhiteboard(): Boolean {
        return getFileID() == null
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

    fun getExcelSheetNameList(): MutableList<String> {
        return if (isExcel() && isDocsViewLoadSuccessed()) {
            zegoDocsView!!.sheetNameList
        } else {
            mutableListOf()
        }
    }

    fun selectExcelSheet(sheetIndex: Int, selectResult: (String, Long) -> Unit) {
        if (sheetIndex < 0 || sheetIndex > getExcelSheetNameList().size - 1) {
            return
        }
        if (isExcel() && isDocsViewLoadSuccessed()) {
            val firstOrNull = whiteboardViewList.firstOrNull {
                it.whiteboardViewModel.fileInfo.fileName == getExcelSheetNameList()[sheetIndex]
            }
            firstOrNull?.let {
                val model = it.whiteboardViewModel
                Logger.i(
                    TAG,
                    "selectSheet,fileName：${model.fileInfo.fileName}，${model.whiteboardID}"
                )
                currentWhiteboardID = model.whiteboardID
                selectResult(model.fileInfo.fileName, model.whiteboardID)
            }
        }
    }

    fun isExcel(): Boolean {
        return getFileType() == ZegoDocsViewConstants.ZegoDocsViewFileTypeELS
    }

    fun isDynamicPPT(): Boolean {
        return getFileType() == ZegoDocsViewConstants.ZegoDocsViewFileTypeDynamicPPTH5
    }

    fun isPPT(): Boolean {
        return getFileType() == ZegoDocsViewConstants.ZegoDocsViewFileTypePPT
    }

    fun hasThumbUrl(): Boolean {
        var docType = getFileType()
        return docType == ZegoDocsViewConstants.ZegoDocsViewFileTypeDynamicPPTH5 ||
                docType == ZegoDocsViewConstants.ZegoDocsViewFileTypePPT ||
                docType == ZegoDocsViewConstants.ZegoDocsViewFileTypePDF ||
                docType == ZegoDocsViewConstants.ZegoDocsViewFileTypePDFAndImages
    }

    fun getThumbnailUrlList(): ArrayList<String> {
        var urls = ArrayList<String>()
        if (hasThumbUrl() && zegoDocsView != null) {
            return zegoDocsView!!.getThumbnailUrlList()
        }
        return urls
    }


    fun getFileType(): Int {
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

    fun supportDragWhiteboard(): Boolean {
        return !(isPureWhiteboard() || isDynamicPPT() || isPPT())
    }

    fun getCurrentWhiteboardName(): String? {
        return getCurrentWhiteboardModel().name
    }

    fun getCurrentWhiteboardModel(): ZegoWhiteboardViewModel {
        return currentWhiteboardView!!.whiteboardViewModel
    }

    fun getCurrentWhiteboardMsg(): String {
        return "modelMessage:name:${getCurrentWhiteboardModel().name},whiteboardID:${getCurrentWhiteboardModel().whiteboardID}," +
                "fileInfo:${getCurrentWhiteboardModel().fileInfo.fileName}" +
                "hori:${getCurrentWhiteboardModel().horizontalScrollPercent},vertical:${getCurrentWhiteboardModel().verticalScrollPercent}"
    }

    fun addTextEdit() {
        currentWhiteboardView?.addTextEdit(context)
    }

    fun undo() {
        currentWhiteboardView?.undo()
    }

    fun redo() {
        currentWhiteboardView?.redo()
    }

    fun clearCurrentPage() {
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

        Logger.i(TAG, "clearCurrentPage: ${curPageRectF.toString()}")
        currentWhiteboardView?.clear(curPageRectF!!)
    }

    fun setCanDraw(canDraw: Boolean) {
        currentWhiteboardView?.setCanDraw(canDraw)
    }

    fun scrollTo(horizontalPercent: Float, verticalPercent: Float, currentStep: Int = 1) {
        Logger.d(
            TAG,
            "scrollTo() called with: horizontalPercent = $horizontalPercent, verticalPercent = $verticalPercent, currentStep = $currentStep"
        )
        if (getFileID() != null) {
            if (isDocsViewLoadSuccessed()) {
                currentWhiteboardView?.scrollTo(horizontalPercent, verticalPercent, currentStep)
            }
        } else {
            currentWhiteboardView?.scrollTo(horizontalPercent, verticalPercent, currentStep)
        }
    }

    fun clear() {
        currentWhiteboardView?.clear()
    }

    private fun addDocsView(docsView: ZegoDocsView, estimatedSize: Size) {
        Logger.d(TAG, "addDocsView, estimatedSize:$estimatedSize")
        docsView.setEstimatedSize(estimatedSize.width, estimatedSize.height)
        this.zegoDocsView = docsView
        addView(
            zegoDocsView, 0, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun addWhiteboardView(zegoWhiteboardView: ZegoWhiteboardView) {
        val model = zegoWhiteboardView.whiteboardViewModel
        Logger.i(
            TAG, "addWhiteboardView:${model.whiteboardID},${model.name},${model.fileInfo.fileName}"
        )
        this.whiteboardViewList.add(zegoWhiteboardView)

        addView(
            zegoWhiteboardView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged() called with: w = $w, h = $h, oldw = $oldw, oldh = $oldh")
        reload()
    }

    private fun connectDocsViewAndWhiteboardView(zegoWhiteboardView: ZegoWhiteboardView) {
        Logger.i(TAG, "connectDocsViewAndWhiteboardView...")
        zegoDocsView?.let { docsview ->
            if (docsview.getVisibleSize().height != 0 || docsview.getVisibleSize().width != 0) {
                zegoWhiteboardView.setVisibleRegion(zegoDocsView!!.getVisibleSize())
            }
            zegoWhiteboardView.setScrollListener { horizontalPercent, verticalPercent ->
                Logger.d(
                    TAG,
                    "ScrollListener.onScroll,horizontalPercent:${horizontalPercent},verticalPercent:${verticalPercent}"
                )
                if (isDynamicPPT()) {
                    val page = calcDynamicPPTPage(verticalPercent)
                    val model = zegoWhiteboardView.whiteboardViewModel
                    val stepChanged = docsview.currentStep != model.pptStep
                    val pageChanged = docsview.currentPage != page
                    Logger.i(
                        TAG,
                        "page:${page},step:${model.pptStep},stepChanged:$stepChanged,pageChanged:$pageChanged"
                    )
                    docsview.flipPage(page, model.pptStep) { result ->
                        Logger.i(TAG, "docsview.flipPage() : result = $result")
                    }
                    internalScrollListener.onScroll(horizontalPercent, verticalPercent)
                } else {
                    docsview.scrollTo(verticalPercent) { complete ->
                        Log.d(
                            TAG,
                            "connectDocsViewAndWhiteboardView() called with: complete = $complete,docsview:${docsview.verticalPercent}"
                        )
                        internalScrollListener.onScroll(0f, verticalPercent)
                    }
                }

            }

            if (isDynamicPPT()) {
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
                        Logger.d(TAG, "onStepChangeForClick() called")
                        scrollTo(0f, docsview.verticalPercent, docsview.currentStep)
                    }
                })
            }
            // 对于动态PPT，其他端有播放动画，需要同步给docsView进行播放动画
            zegoWhiteboardView.setAnimationListener { animation ->
                Logger.d(TAG, "setAnimationListener() called")
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
            Logger.d(TAG, "horPercent:$horPercent,verPercent:$verPercent,currentStep:$currentStep")
            if (isDynamicPPT()) {
                // 此处是首次加载，要跳转到到文件对应页。完成后需要判断是否播动画
                zegoDocsView?.let {
                    val targetPage = calcDynamicPPTPage(verPercent)
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

    fun calcDynamicPPTPage(verticalPercent: Float): Int {
        return if (isDynamicPPT()) {
            if (isDocsViewLoadSuccessed()) {
                val page = round(verticalPercent * zegoDocsView!!.pageCount).toInt() + 1
                page
            } else {
                1
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
        zegoWhiteboardView.setBackgroundColor(Color.parseColor("#f4f5f8"))
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
        data.name = whiteboardName
        data.pageCount = pageCount
        data.roomId = CONFERENCE_ID
        ZegoWhiteboardManager.getInstance().createWhiteboardView(data)
        { errorCode, zegoWhiteboardView ->
            Logger.d(
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
                            zegoDocsView?.unloadFile()
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
                    zegoDocsView?.unloadFile()
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
        Logger.d(
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
            loadFileWhiteBoardView(fileID, estimatedSize) { errorCode: Int, _: ZegoDocsView ->
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
    private fun loadFileWhiteBoardView(
        fileID: String,
        estimatedSize: Size,
        requestResult: (Int, ZegoDocsView) -> Unit
    ) {
        Logger.i(
            TAG,
            "loadFileWhiteBoardView,start loadFile fileID:${fileID},estimatedSize:${estimatedSize}"
        )
        ZegoDocsView(context).let {
            addDocsView(it, estimatedSize)
            it.loadFile(fileID, "", IZegoDocsViewLoadListener { errorCode ->
                fileLoadSuccessed = errorCode == 0
                if (errorCode == 0) {
                    Logger.i(
                        TAG,
                        "loadFileWhiteBoardView loadFile fileID:${fileID} success,getVisibleSize:${it.getVisibleSize()}," +
                                "contentSize:${it.getContentSize()}," + "name:${it.getFileName()}" +
                                "nameList:${it.getSheetNameList()}"
                    )
                    it.setBackgroundColor(Color.parseColor("#f4f5f8"))
                } else {
                    Logger.i(
                        TAG,
                        "loadFileWhiteBoardView loadFile fileID:${fileID} failed，errorCode：${errorCode}"
                    )
                }
                requestResult.invoke(errorCode, it)
            })
        }
    }

    fun createDocsAndWhiteBoardView(
        fileID: String, estimatedSize: Size, createResult: (Int) -> Unit
    ) {
        loadFileWhiteBoardView(fileID, estimatedSize)
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

    fun isWhiteboardViewAddFinished(): Boolean {
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
                    selectExcelSheet(0) { _, _ ->
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
        data.name = docsView.fileName!!
        data.pageCount = docsView.pageCount
        data.fileInfo.fileID = docsView.fileID!!
        data.fileInfo.fileType = docsView.fileType
        if (isExcel()) {
            data.fileInfo.fileName = docsView.sheetNameList[index]
        }
        data.roomId = CONFERENCE_ID

        ZegoWhiteboardManager.getInstance().createWhiteboardView(data)
        { errorCode, zegoWhiteboardView ->
            Logger.d(
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

    fun flipToPage(targetPage: Int) {
        Logger.i(TAG, "targetPage:${targetPage}")
        if (zegoDocsView != null && getFileID() != null && isDocsViewLoadSuccessed()) {
            zegoDocsView!!.flipPage(targetPage) { result ->
                Logger.i(TAG, "it.flipToPage() result:$result")
                if (result) {
                    scrollTo(0f, zegoDocsView!!.getVerticalPercent())
                }
            }
        } else {
            scrollTo((targetPage - 1).toFloat() / getPageCount(), 0f)
        }
    }

    /**
     * 此处的page是从1开始的
     */
    fun flipToPrevPage(): Int {
        val currentPage = getCurrentPage()
        val targetPage = if (currentPage - 1 <= 0) 1 else currentPage - 1
        if (targetPage != currentPage) {
            flipToPage(targetPage)
        }
        return targetPage
    }

    fun flipToNextPage(): Int {
        val currentPage = getCurrentPage()
        val targetPage =
            if (currentPage + 1 > getPageCount()) getPageCount() else currentPage + 1
        if (targetPage != currentPage) {
            flipToPage(targetPage)
        }
        return targetPage
    }

    fun previousStep() {
        Logger.d(TAG, "previousStep() called,fileLoadSuccessed:${isDocsViewLoadSuccessed()}")
        if (getFileID() != null && isDynamicPPT() && isDocsViewLoadSuccessed()) {
            zegoDocsView?.let {
                it.previousStep(IZegoDocsViewScrollCompleteListener { result ->
                    Logger.d(TAG, "previousStep:result = $result")
                    if (result) {
                        scrollTo(0f, it.getVerticalPercent(), it.getCurrentStep())
                    }
                })
            }
        }
    }

    fun nextStep() {
        Logger.i(TAG, "nextStep() called,fileLoadSuccessed:${isDocsViewLoadSuccessed()}")
        if (getFileID() != null && isDynamicPPT() && isDocsViewLoadSuccessed()) {
            zegoDocsView?.let {
                it.nextStep(IZegoDocsViewScrollCompleteListener { result ->
                    Logger.i(TAG, "nextStep:result = $result")
                    if (result) {
                        scrollTo(0f, it.getVerticalPercent(), it.getCurrentStep())
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
        return if (getFileID() != null) {
            zegoDocsView!!.getCurrentPage()
        } else {
            val percent = currentWhiteboardView!!.getHorizontalPercent()
            val currentPage = round(percent * getPageCount()).toInt() + 1
            Logger.i(TAG, "getCurrentPage,percent:${percent},currentPage:${currentPage}")
            return if (currentPage < getPageCount()) currentPage else getPageCount()
        }
    }

    fun setWhiteboardScrollChangeListener(listener: IZegoWhiteboardViewScrollListener) {
        outScrollListener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 保底处理
        zegoDocsView?.unloadFile()
        fileLoadSuccessed = false
    }

    fun addText(text: String, positionX: Int, positionY: Int) {
        currentWhiteboardView?.addText(text, positionX, positionY)
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

    fun enableUserOperation(enable: Boolean) {
        currentWhiteboardView?.enableUserOperation(enable)
    }

    fun deleteSelectedGraphics() {
        currentWhiteboardView?.deleteSelectedGraphics()
    }

    /**
     * 停止当前正在播放的视频
     */
    fun stopPlayPPTVideo() {
        if (isDynamicPPT()) {
            zegoDocsView?.let {
                it.stopPlay(it.currentPage)
            }
        }
    }

    /**
     * 加载文件
     * @param fileID 文件 ID
     */
    fun loadDocsFile(fileID: String, listener: IZegoDocsViewLoadListener) {
        zegoDocsView?.loadFile(fileID, "", IZegoDocsViewLoadListener { errorCode: Int ->
            Logger.d(TAG, "onLoadFile() called with: errorCode = [$errorCode]")
            Logger.d(
                TAG,
                "onLoadFile() called with: docsView.isScaleEnable() = [" + zegoDocsView?.isScaleEnable + "]"
            )
            listener.onLoadFile(errorCode)
        })
    }

    /**
     * 将文件从视图中卸载
     */
    fun unloadFile() {
        zegoDocsView?.unloadFile()
    }

    fun reload() {
        Log.d(TAG, "reload() called")
        if (zegoDocsView != null) {
            zegoDocsView?.reloadFile(IZegoDocsViewLoadListener { loadCode ->
                if (loadCode == 0) {
                    Logger.d(TAG, "visibleRegion:${zegoDocsView!!.getVisibleSize()}")
                    currentWhiteboardView?.setVisibleRegion(zegoDocsView!!.visibleSize)
                }
            })
        }
    }

}