package im.zego.whiteboardexample.widget.whiteboard

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.util.AttributeSet
import android.util.Size
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.children
import im.zego.zegodocs.ZegoDocsViewConstants
import im.zego.zegowhiteboard.ZegoWhiteboardView
import im.zego.zegowhiteboard.callback.IZegoWhiteboardViewScrollListener
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.sdk.ZegoSDKManager
import im.zego.whiteboardexample.util.Logger
import im.zego.whiteboardexample.util.SharedPreferencesUtil
import im.zego.whiteboardexample.util.dp2px

/**
 * 一个白板布局
 */
class WhiteboardContainer : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val TAG = "WhiteboardContainer"

    private var wbAspectWidth = 16
    private var wbAspectHeight = 9
    private var countChangeListener: (count: Int) -> Unit = {}
    private var scrollChangeListener: (Int, Int) -> Unit = { _, _ -> }
    private var whiteboardSelectListener: (Long) -> Unit = {}
    private var whiteboardClickListener: () -> Unit = {}

    private var abortGestureEvent: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_room_center, this, true)
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        Logger.i(TAG, "onViewAdded")
        countChangeListener.invoke(childCount)
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        Logger.i(TAG, "onViewRemoved,childCount:$childCount")
        countChangeListener.invoke(childCount)
    }

    fun setChildCountChangedListener(listener: (count: Int) -> Unit) {
        this.countChangeListener = listener
    }

    fun setWhiteboardScrollListener(listener: (Int, Int) -> Unit) {
        this.scrollChangeListener = listener
    }

    fun setWhiteboardSelectListener(listener: (Long) -> Unit) {
        this.whiteboardSelectListener = listener
    }

    fun getWhiteboardViewHolder(whiteboardID: Long): ZegoWhiteboardViewHolder? {
        val whiteboardView = children.firstOrNull { childView ->
            val zegoWhiteboardView = childView as ZegoWhiteboardViewHolder
            zegoWhiteboardView.hasWhiteboardID(whiteboardID)
        }
        return whiteboardView as? ZegoWhiteboardViewHolder
    }

    fun getWhiteboardViewHolder(fileID: String): ZegoWhiteboardViewHolder? {
        val whiteboardView = children.firstOrNull { childView ->
            val holder = childView as ZegoWhiteboardViewHolder
            fileID == holder.getFileID()
        }
        return whiteboardView as? ZegoWhiteboardViewHolder
    }

    fun removeWhiteboardViewHolder(whiteboardID: Long) {
        getWhiteboardViewHolder(whiteboardID)?.let {
            removeView(it)
        }
    }

    private fun getPureWhiteboardHolderCount(): Int {
        var count = 0
        children.forEach {
            val holder = it as ZegoWhiteboardViewHolder
            if (holder.isPureWhiteboard()) {
                count++
            }
        }
        return count
    }

    fun getFileWhiteboardHolderCount(): Int {
        var count = 0
        children.forEach {
            val holder = it as ZegoWhiteboardViewHolder
            if (holder.isFileWhiteboard()) {
                count++
            }
        }
        return count
    }

    fun selectWhiteboardViewHolder(whiteboardID: Long): ZegoWhiteboardViewHolder? {
        Logger.i(TAG, "start selectWhiteboardViewHolder:${whiteboardID},childCount:${childCount}")
        var result: ZegoWhiteboardViewHolder? = null
        
        if (whiteboardID == 0L) {
            // 如果传的是0，表示刚刚进房间后获取当前白板没有设置过
            children.forEachIndexed { index, view ->
                if (index == childCount - 1) {
                    val holder = view as ZegoWhiteboardViewHolder
                    view.visibility = View.VISIBLE
                    result = holder
                } else {
                    view.visibility = View.GONE
                }
            }
            
        } else {
            children.forEach {
                val holder = it as ZegoWhiteboardViewHolder
                // 若所选白板 ID 未在 Holder 列表中则不显示
                if (!holder.hasWhiteboardID(whiteboardID)) {
                    it.visibility = View.GONE
                } else {
                    // 若该白板 ID 未显示才去设置为显示状态
                    if (it.visibility != View.VISIBLE) {
                        it.visibility = View.VISIBLE
                    }
                    // 更新当前展示的白板 ID
                    if (it.currentWhiteboardID != whiteboardID) {
                        it.currentWhiteboardID = whiteboardID
                    }
                    result = it
                }
            }
            
            if (result == null) {
                // 如果设置了这个白板 ID 但是房间里没有找到，设置第一个子 view
                Logger.i(TAG, "selectWhiteboardViewHolder:${whiteboardID},result = null ")
                children.forEachIndexed { index, view ->
                    if (index == childCount - 1) {
                        val holder = view as ZegoWhiteboardViewHolder
                        view.visibility = View.VISIBLE
                        result = holder
                    } else {
                        view.visibility = View.GONE
                    }
                }
            }
        }
        
        if (result == null) {
            // 仍然没有找到，就什么也不做
            Logger.i(TAG, "selectWhiteboardViewHolder，no selected ")
        }
        
        result?.let {
            it.setWhiteboardScrollChangeListener(IZegoWhiteboardViewScrollListener { _, vertical ->
                val currentPage = if (it.isDynamicPPT()) {
                    it.calcDynamicPPTPage(vertical)
                } else {
                    it.getCurrentPage()
                }
                scrollChangeListener(currentPage, it.getPageCount())
            })
            whiteboardSelectListener.invoke(it.currentWhiteboardID)

            children.forEach { child ->
                val holder = child as ZegoWhiteboardViewHolder
                if (it != holder) {
                    holder.stopPlayPPTVideo()
                }
            }
        }

        return result
    }

    /**
     * 假设请求回来之后，view 已经 layout 完成了，这时候需要构建一个 16：9 的白板区域
     * 用屏幕的高度减去顶部和底部控件的高度得到白板父 view (也就是当前这个类) 的高度，计算出 16:9 的宽度
     * 宽度设置给白板父 view 的父 view，用来限制所有子 view 的宽度边界
     */
    fun resize(activity: Activity) {
        val deviceSize = Point()
        activity.windowManager.defaultDisplay.getSize(deviceSize)
        val rotation = activity.windowManager.defaultDisplay.rotation
        Logger.i(TAG, "resize:deviceSize:${deviceSize},rotation:${rotation}")
        val orientation = if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            Configuration.ORIENTATION_PORTRAIT
        } else if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
            Configuration.ORIENTATION_LANDSCAPE
        } else {
            Configuration.ORIENTATION_UNDEFINED
        }
        calcSize(orientation, deviceSize.x.toFloat(), deviceSize.y.toFloat())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Logger.d(TAG, "onMeasure:${measuredWidth},${measuredHeight}")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Logger.d(TAG, "onLayout:${width},${height}")
    }

    private fun calcSize(orientation: Int, deviceWidth: Float, deviceHeight: Float) {
        Logger.i(
            TAG,
            "calcSize,orientation:${orientation},deviceWidth:${deviceWidth},deviceHeight:${deviceHeight}," +
                    "container current,width:${this.width},height:${this.height}"
        )

        val selfWidth: Float
        val selfHeight: Float
        val parentWidth: Float
        val parentHeight: Float
        val topHeight = dp2px(context, 98f)
        val bottomHeight = dp2px(context, 20f)
        if (orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            selfWidth = deviceWidth
            selfHeight = selfWidth / (wbAspectWidth.toFloat() / wbAspectHeight.toFloat())

            parentWidth = deviceWidth
            parentHeight = (selfHeight + topHeight + bottomHeight)
        } else {
            selfHeight = (deviceHeight - topHeight - bottomHeight)
            selfWidth = selfHeight * wbAspectWidth / wbAspectHeight
            parentWidth = selfWidth
            parentHeight = deviceHeight
        }

        val parent = parent as ViewGroup
        val parentParams = parent.layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        parentParams.width = parentWidth.toInt()
        parentParams.height = parentHeight.toInt()
        parent.layoutParams = parentParams

        val params = layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        params.width = selfWidth.toInt()
        params.height = selfHeight.toInt()
        layoutParams = params

        Logger.i(
            TAG,
            "resize,calculated:width:${selfWidth},height:${selfHeight},parent:${parentParams.width},${parentParams.height}"
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Logger.i(TAG, "newConfig:$newConfig")

        val deviceWidth = dp2px(context, newConfig.screenWidthDp.toFloat())
        val deviceHeight = dp2px(context, newConfig.screenHeightDp.toFloat())

        calcSize(newConfig.orientation, deviceWidth, deviceHeight)
    }

    fun createPureWhiteboard(requestResult: (Int, ZegoWhiteboardViewHolder) -> Unit) {
        val count = getPureWhiteboardHolderCount()
        if (count < ZegoSDKManager.MAX_PURE_WB_COUNT) {
            val whiteboardViewHolder = ZegoWhiteboardViewHolder(context)
            val userName = SharedPreferencesUtil.getLastJoinName()
            val whiteboardName = "${userName}创建的白板${ZegoSDKManager.whiteboardNameIndex}"
            ZegoSDKManager.whiteboardNameIndex += 1
            whiteboardViewHolder.createPureWhiteboardView(
                wbAspectWidth * 5, wbAspectHeight, 5, whiteboardName
            ) { errorCode ->
                if (errorCode == 0) {
                    addView(whiteboardViewHolder)
                }
                requestResult.invoke(errorCode, whiteboardViewHolder)
            }
        } else {
            Toast.makeText(context, context.getString(R.string.max_wb_tips), Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun createFileWhiteBoardView(
        fileID: String,
        requestResult: (Int, ZegoWhiteboardViewHolder) -> Unit
    ) {
        Logger.i(TAG, "container createFileWhiteBoardView,fileID:${fileID}")
        val count = getFileWhiteboardHolderCount()
        var holder = getWhiteboardViewHolder(fileID)
        if (holder != null) {
            // 假如这个文件还在创建白板的过程中没有返回回来，这时候 currentWhiteboard 是空
            // 所以如果不是空才设置更新
            if (holder.currentWhiteboardID != 0L) {
                updateCurrentHolderToRoom(holder)
            } else {
                // 什么也不做
            }
        } else {
            if (count < ZegoSDKManager.MAX_FILE_WB_COUNT) {
                val viewHolder = ZegoWhiteboardViewHolder(context)
                addView(viewHolder)
                viewHolder.createDocsAndWhiteBoardView(fileID, Size(width, height)) { errorCode ->
                    if (errorCode == 0) {
                        Logger.i(TAG, "viewHolder.currentWhiteboardID: " + viewHolder.currentWhiteboardID)
                        selectWhiteboardViewHolder(viewHolder.currentWhiteboardID)
                    } else {
                        removeView(holder)
                        if (!viewHolder.isDocsViewLoadSuccessed()) {
                            // 加载文件失败
                            Toast.makeText(context, "加载文件白板失败，错误代码 $errorCode", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            // 创建白板失败
                            Toast.makeText(context, "创建文件白板失败，错误代码 $errorCode", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                    requestResult(errorCode, viewHolder)
                }
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.max_file_tips),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun onReceiveWhiteboardView(
        zegoWhiteboardView: ZegoWhiteboardView,
        processResult: (Int, Boolean, ZegoWhiteboardViewHolder) -> Unit
    ) {
        val model = zegoWhiteboardView.getWhiteboardViewModel()
        Logger.i(
            TAG, "onReceiveWhiteboardView:${model.whiteboardID}," +
                    "${model.name},${model.fileInfo.fileName}"
        )
        val fileID = model.fileInfo.fileID
        val fileType = model.fileInfo.fileType
        if (fileID.isNotEmpty()) {
            var holder = getWhiteboardViewHolder(fileID)
            val newHolder = (holder == null)
            if (holder == null || fileType != ZegoDocsViewConstants.ZegoDocsViewFileTypeELS) {
                holder = ZegoWhiteboardViewHolder(context)
                addView(holder)
            }
            holder.onReceiveFileWhiteboard(Size(width, height), zegoWhiteboardView)
            { errorCode, _ ->
                processResult(errorCode, newHolder, holder)
            }
        } else {
            ZegoWhiteboardViewHolder(context).also { holder ->
                holder.onReceivePureWhiteboardView(zegoWhiteboardView)
                holder.visibility = View.GONE
                addView(holder)
                processResult(0, true, holder)
            }
        }
    }

    fun deleteWhiteboard(
        whiteboardID: Long,
        requestResult: (Int, ZegoWhiteboardViewHolder, List<Long>) -> Unit
    ) {
        val whiteboardViewHolder = getWhiteboardViewHolder(whiteboardID)
        whiteboardViewHolder?.let { holder ->
            val list = holder.getWhiteboardIDList()
            holder.destroyWhiteboardView { errorCode ->
                if (errorCode == 0) {
                    removeView(holder)
                }
                requestResult.invoke(errorCode, holder, list)
            }
        }
    }

    // 这里需要特别小心，对于多个白板，他们可能只需要一个viewHolder，而load_file是一个异步的过程
    // 所以预先添加进去，后续根据文件ID来判断是不是需要创建一个新的viewHolder
    // 如果不是当前显示的白板，先不可见，否则后面加载成功后添加到container会挡住别的view
    fun onEnterRoomReceiveWhiteboardList(
        whiteboardViewList: MutableList<ZegoWhiteboardView>,
        processResult: (Int) -> Unit
    ) {
        var processedCount = 0
        var errorCodeResult = 0
        Logger.i(
            TAG,
            "onEnterRoomReceiveWhiteboardList.size:${whiteboardViewList.size}"
        )
        whiteboardViewList.forEach { whiteboardView ->
            onReceiveWhiteboardView(whiteboardView) { errorCode, _, holder ->
                if (errorCode != 0) {
                    errorCodeResult = errorCode
                }
                processedCount++
                if (processedCount == whiteboardViewList.size) {
                    processResult.invoke(errorCodeResult)
                }
            }
        }
    }

    fun updateCurrentHolderToRoom(holder: ZegoWhiteboardViewHolder) {
        if (holder.visibility != View.VISIBLE) {
            Logger.i(TAG, "find holder,is not show,sending select, ${holder.getCurrentWhiteboardMsg()}")
        } else {
            Logger.i(TAG, "find holder,is showing,just select")
        }
        selectWhiteboardViewHolder(holder.currentWhiteboardID)
    }

    fun setWhiteboardClickedListener(listener: () -> Unit) {
        this.whiteboardClickListener = listener
    }

    fun getWhiteboardViewHolderList(): List<ZegoWhiteboardViewHolder> {
        return children.map { it as ZegoWhiteboardViewHolder }.toList()
    }

    private var lastClickTime: Long = 0
    private var lastClickX = 0f
    private var lastCLickY = 0f
    private var isClick = false
    private var scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

    /**
     * 因为子 view 会拦截事件，所以这里简单处理了
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastClickX = event.x
                lastCLickY = event.y
                lastClickTime = System.currentTimeMillis()
                isClick = true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastClickX
                val dy = event.y - lastCLickY
                if (dx > scaledTouchSlop || dy > scaledTouchSlop) {
                    isClick = false
                }
            }
            MotionEvent.ACTION_UP -> {
                // do nothing
            }
        }

        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            abortGestureEvent = false
        }
        // 如果正在操作白板的时候，被取消了权限，停止手势的来停止白板的操作
        return if (abortGestureEvent) {
            true
        } else {
            super.dispatchTouchEvent(event)
        }

    }
}