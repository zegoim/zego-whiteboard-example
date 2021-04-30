package im.zego.whiteboardexample.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.adapter.ColorAdapter
import im.zego.whiteboardexample.sdk.docs.CacheHelper
import im.zego.whiteboardexample.sdk.docs.upload.UploadFileHelper
import im.zego.whiteboardexample.sdk.docs.upload.UploadPicHelper
import im.zego.whiteboardexample.sdk.rtc.IZegoRoomStateListener
import im.zego.whiteboardexample.sdk.rtc.VideoSDKManager
import im.zego.whiteboardexample.sdk.whiteboard.WhiteboardSDKManager
import im.zego.whiteboardexample.tool.SimpleTextWatcher
import im.zego.whiteboardexample.util.*
import im.zego.whiteboardexample.widget.OnRecyclerViewItemTouchListener
import im.zego.whiteboardexample.widget.dialog.LoadingDialog
import im.zego.whiteboardexample.widget.dialog.ZegoDialog
import im.zego.whiteboardexample.widget.dialog.dismissLoadingDialog
import im.zego.whiteboardexample.widget.dialog.showLoadingDialog
import im.zego.whiteboardexample.widget.popwindow.*
import im.zego.whiteboardexample.widget.whiteboard.ZegoWhiteboardViewHolder
import im.zego.zegodocs.ZegoDocsViewConstants
import im.zego.zegodocs.ZegoDocsViewCustomH5Config
import im.zego.zegowhiteboard.*
import im.zego.zegowhiteboard.callback.IZegoWhiteboardManagerListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_main_content.*
import kotlinx.android.synthetic.main.popwindow_select.view.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity() {

    private val TAG = "MainActivity"

    // 用于设置当前所选白板容器
    private val handler = Handler()

    // 加载 Dialog
    private lateinit var loadingDialog: LoadingDialog

    // 白板容器
    private var currentHolder: ZegoWhiteboardViewHolder? = null

    // 用于选择自定义图形
    var selectCustomImagePopWindow: SelectCustomImagePopWindow? = null

    // 用于选择背景类型
    var selectBackgroundFitModePopWindow: SelectBackgroundFitModePopWindow? = null

    // 用于选择背景
    var selectBackgroundPopWindow: SelectBackgroundPopWindow? = null
    var fitMode: ZegoWhiteboardViewImageFitMode =
        ZegoWhiteboardViewImageFitMode.ZegoWhiteboardViewImageFitModeCenter

    // 防止快速点击页面、步数、跳转、文本
    var lastClickPageChangeTime = 0L
    var lastClickPageJumpChangeTime = 0L
    var lastClickStepChangeTime = 0L
    var lastTextClickedTime: Long = 0

    // 获取白板列表是否结束
    var getListFinished = false

    // 刚刚进房间也会有 whiteboardAdd，remove 消息过来，这时候缓存一下，再和 getList 里面对比，删掉重复的
    var tempWbList = mutableListOf<ZegoWhiteboardView>()

    var selectGraffitiToolsPopWindow: SelectGraffitiToolsPopWindow? = null
    var isScrollAuthorized = true
    var whiteboardAuthInfo = HashMap<String, Int>()
    var currentOpMode = SelectOpModePopWindow.DRAW_SCALE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog = LoadingDialog(this, 0.8f)
        setContentView(R.layout.activity_main)
        initViews()
        // 请求白板列表
        requestWhiteboardList()
    }

    override fun onResume() {
        super.onResume()

        // 隐藏虚拟按键，并且全屏 tool
        hideBottomUIMenu()
        // 设置当前所选白板容器
        currentHolder?.let {
            handler.post {
                onWhiteboardHolderSelected(it)
            }
        }
    }

    private fun initViews() {
        // 初始化顶部栏切白板，切页等控制
        initTopLayout()
        // 初始化 Drawer 中已打开的列表
        initDrawerRight()
        // 初始化右边布局
        initRightLayout()
        // 白板 View 数据监听
        addWhiteboardViewListener()
        // 房间相关的监听
        setRoomListeners()



        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortrait()
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscape()
        }
    }

    /**
     * 初始化右边布局
     */
    private fun initRightLayout() {
        title_layout.setOnCheckedChangeListener { p0, checkedId ->
            when (checkedId) {
                R.id.tv_docs -> {
                    docs_layout.visibility = View.VISIBLE
                    draw_layout.visibility = View.GONE
                    whiteboard_layout.visibility = View.GONE
                }
                R.id.tv_whiteboard -> {
                    whiteboard_layout.visibility = View.VISIBLE
                    docs_layout.visibility = View.GONE
                    draw_layout.visibility = View.GONE
                }
                else -> {
                    draw_layout.visibility = View.VISIBLE
                    docs_layout.visibility = View.GONE
                    whiteboard_layout.visibility = View.GONE
                }
            }
        }
        container.setWhiteboardReloadFinishListener {
            refreshUI()
        }
        // 初始化右边绘制部分点击事件
        initRightDraw()
        // 初始化右边白板部分点击事件
        initRightWhiteboard()
        // 初始化右边文件部分点击事件
        initRightDocs()

        main_menu.setOnClickListener {
            if (main_end_layout.isVisible) {
                main_end_layout.visibility = View.GONE
            } else {
                main_end_layout.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 初始化右边绘制模块
     */
    private fun initRightDraw() {
        // 操作模式
//        draw_whiteboard_mode.text = SelectOpModePopWindow.DRAW_STRING
//        draw_whiteboard_mode.setOnClickListener {
//            val selectOpModePopWindow = SelectOpModePopWindow(this,currentOpMode)
//            selectOpModePopWindow.setOnConfirmClickListener { mode, modeString ->
//                currentHolder?.setOperationMode(mode)
//                currentOpMode = mode
//                draw_whiteboard_mode.text = modeString
//            }
//            selectOpModePopWindow.show(draw_whiteboard_mode,Gravity.BOTTOM)
//        }
        mode_none.setOnCheckedChangeListener { buttonView, isChecked ->
            setHolderOperationMode()
        }
        mode_scale.setOnCheckedChangeListener { buttonView, isChecked ->
            setHolderOperationMode()
        }
        mode_scroll.setOnCheckedChangeListener { buttonView, isChecked ->
            setHolderOperationMode()
        }
        mode_draw.setOnCheckedChangeListener { buttonView, isChecked ->
            setHolderOperationMode()
        }
        mode_none.isChecked = false
        mode_scale.isChecked = true
        mode_scroll.isChecked = false
        mode_draw.isChecked = true

        // 关闭/开启粗体
        text_style_bold.setOnCheckedChangeListener { _, b ->
            ZegoWhiteboardManager.getInstance().isFontBold = b
        }
        // 关闭/开启斜体
        text_style_italic.setOnCheckedChangeListener { _, b ->
            ZegoWhiteboardManager.getInstance().isFontItalic = b
        }
        // 颜色
        draw_color_recyclerview.let {
            val colorAdapter = ColorAdapter(this)
            colorAdapter.selectedColor = ZegoWhiteboardManager.getInstance().brushColor
            it.adapter = colorAdapter
            it.layoutManager = GridLayoutManager(this, 6)
            it.addOnItemTouchListener(object : OnRecyclerViewItemTouchListener(it) {
                override fun onItemClick(vh: RecyclerView.ViewHolder) {
                    val adapterPosition = vh.adapterPosition
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return
                    }
                    colorAdapter.selectedIndex = adapterPosition
                    colorAdapter.notifyDataSetChanged()
                    ZegoWhiteboardManager.getInstance().brushColor = colorAdapter.selectedColor
                }
            })
        }
        // 涂鸦工具
        draw_graffiti_tools_tv.setOnClickListener {
            currentHolder?.let {
                selectGraffitiToolsPopWindow =
                    SelectGraffitiToolsPopWindow(
                        this,
                        draw_graffiti_tools_tv.text.toString(),
                        it.isDisplayedByWebView()
                    )
                selectGraffitiToolsPopWindow!!.setOnConfirmClickListener { str ->
                    draw_graffiti_tools_tv.text = str
                    unSelectOtherChild(str)
                }
                selectGraffitiToolsPopWindow!!.show(draw_graffiti_tools_tv)
            }
        }
        // 字号大小
        draw_text_size_tv.text = ZegoWhiteboardManager.getInstance().fontSize.toString()
        draw_text_size_tv.setOnClickListener {
            val selectFontSizePopWindow =
                SelectFontSizePopWindow(this, draw_text_size_tv.text.toString().toInt())
                    .also {
                        it.setOnConfirmClickListener { str ->
                            draw_text_size_tv.text = str.toString()
                        }
                    }
            selectFontSizePopWindow.show(draw_text_size_tv)
        }
        // 笔画粗细
        draw_brush_size_tv.text = ZegoWhiteboardManager.getInstance().brushSize.toString()
        draw_brush_size_tv.setOnClickListener {
            val selectBrushSizePopWindow =
                SelectBrushSizePopWindow(this, draw_brush_size_tv.text.toString().toInt())
                    .also {
                        it.setOnConfirmClickListener { str ->
                            draw_brush_size_tv.text = str.toString()
                        }
                    }
            selectBrushSizePopWindow.show(draw_brush_size_tv)
        }
        // 自定义图形
        selectCustomImagePopWindow = SelectCustomImagePopWindow(this).also {
            it.setOnConfirmClickListener { imageData ->
                // 更换 url 设置值, 告知 SDK 当前所选 Image URL
                currentHolder?.let { holder ->
                    holder.addImage(
                        ZegoWhiteboardViewImageType.ZegoWhiteboardViewImageCustom,
                        imageData.url,
                        0,
                        0
                    ) { errorCode ->
                        when (errorCode) {
                            0 -> {
                                draw_custom_image_tv.text = imageData.imageName
                            }
                            ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageSizeLimit -> {
                                Toast.makeText(this, "图片大小不能超过500k，请重新选择", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageTypeNotSupport -> {
                                Toast.makeText(this, "图片格式暂不支持", Toast.LENGTH_SHORT).show()
                            }
                            ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicIllegalAddress -> {
                                Toast.makeText(this, "非法图片URL", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(
                                    this,
                                    "${errorCode}: " + getString(R.string.draw_input_url),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                }
            }
        }
        draw_custom_image_tv.setOnClickListener {
            if (currentHolder == null) {
                Toast.makeText(this, "当前没有白板，请添加白板", Toast.LENGTH_SHORT).show()
            } else {
                selectCustomImagePopWindow?.show(draw_custom_image_tv)
            }
        }
        // 上传自定义图形
//        draw_custom_image_url_et.setText("https://iconfont.alicdn.com/s/01822ee5-1c99-4303-bf30-d4176283356c_origin.svg")
//        draw_custom_image_url_et.setText("https://cdn.magdeleine.co/wp-content/uploads/2020/11/46523722622_693a76786c_k-1400x933.jpg")
        draw_custom_image_url_confirm_btn.setOnClickListener {
            val url = draw_custom_image_url_et.text.toString()
            if (url.isEmpty() || !url.contains('/')) {
                ToastUtils.showCenterToast(getString(R.string.draw_input_url))
                return@setOnClickListener
            }
            if (url.isNotEmpty()) {
                currentHolder?.addImage(
                    ZegoWhiteboardViewImageType.ZegoWhiteboardViewImageCustom,
                    url,
                    0,
                    0
                ) { errorCode ->
                    when (errorCode) {
                        0 -> {
                            selectCustomImagePopWindow?.addImage(url)
                        }
                        ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageSizeLimit -> {
                            Toast.makeText(
                                this,
                                "${errorCode}: 图片大小不能超过500k，请重新添加",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                        ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageTypeNotSupport -> {
                            Toast.makeText(this, "${errorCode}: 图片格式暂不支持", Toast.LENGTH_SHORT)
                                .show()
                        }
                        ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicIllegalAddress -> {
                            Toast.makeText(this, "${errorCode}: 非法图片URL", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(
                                this,
                                "${errorCode}: " + getString(R.string.draw_input_url),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }

                }
            }
        }
        // 本地上传，要先调用上传接口，再 addImage，
        draw_local_upload_image_btn.setOnClickListener {
            val x = draw_upload_image_x_et.text.toString()
            val y = draw_upload_image_y_et.text.toString()
            if (x.isNotEmpty() && y.isNotEmpty()) {
                if (currentHolder == null) {
                    Toast.makeText(this, "当前没有白板，请添加白板", Toast.LENGTH_SHORT).show()
                } else {
                    UploadPicHelper.startChoosePicture(this)
                }
            } else {
                Toast.makeText(this, "参数错误，请填写坐标参数", Toast.LENGTH_SHORT).show()
            }
        }
        // 上传图片确定，url，直接 addImage
//        draw_upload_image_x_et.setText("1")
//        draw_upload_image_y_et.setText("1")
//        draw_upload_image_url_et.setText("https://p.upyun.com/demo/webp/webp/gif-0.webp")
        draw_upload_image_confirm_btn.setOnClickListener {
            val x = draw_upload_image_x_et.text.toString()
            val y = draw_upload_image_y_et.text.toString()
            val url = draw_upload_image_url_et.text.toString()
//            val url = "https://p.upyun.com/demo/webp/webp/gif-0.webp"
//            val url = "https://docservice-storage-test.zego.im/a541dd1c4ad28fa165ad9b9d6b0c8a28/incoming/3616a20f2f91e428c1c985dfdfe21567?.png "
            if (x.isNotEmpty() && y.isNotEmpty()) {
                if (currentHolder == null) {
                    Toast.makeText(this, "当前没有白板，请添加白板", Toast.LENGTH_SHORT).show()
                } else {
                    currentHolder?.addImage(
                        ZegoWhiteboardViewImageType.ZegoWhiteboardViewImageGraphic,
                        url,
                        x.toInt(),
                        y.toInt()
                    ) { errorCode ->
                        when (errorCode) {
                            0 -> {

                            }
                            ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageSizeLimit -> {
                                Toast.makeText(
                                    this,
                                    "${errorCode}: 图片大小不能超过10M，请重新选择",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                            ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageTypeNotSupport -> {
                                Toast.makeText(this, "${errorCode}: 图片格式暂不支持", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicIllegalAddress -> {
                                Toast.makeText(this, "${errorCode}: 非法图片URL", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            else -> {
                                Toast.makeText(
                                    this,
                                    "${errorCode}: " + getString(R.string.draw_input_url),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "请填写坐标参数", Toast.LENGTH_SHORT).show()
            }
//            val url = "https://avatars1.githubusercontent.com/u/13362002?s=400&u=f334344ba16774d46b7774557cbfd8f23914aa32&v=4"
//                "http://pic.5tu.cn/uploads/allimg/201808/pic_5tu_thumb_201808180825318223.jpg"

//                "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=1847302647,2281910151&fm=26&gp=0.jpg"
//                "http://iconfont.alicdn.com/s/fdec9020-3aa0-43db-99d2-7f6e199f1e78_origin.svg"
        }
        //设置背景的显示模式的弹窗
        selectBackgroundFitModePopWindow = SelectBackgroundFitModePopWindow(this).also {
            it.setOnConfirmClickListener { mSelectMode ->
                //更换背景图片模式
                fitMode = mSelectMode.mode
                draw_background_mode_tv.text = mSelectMode.modeName
            }
        }
        //设置背景的显示模式
        draw_background_mode_tv.setOnClickListener {
            selectBackgroundFitModePopWindow?.show(draw_background_mode_tv)
        }
        //设置背景的弹窗
        selectBackgroundPopWindow = SelectBackgroundPopWindow(this).also {
            it.setOnConfirmClickListener { mSelectUrl ->
                //更换背景图片
                setBackgroundForUrl(mSelectUrl.url, false)
                draw_background_name_tv.text = mSelectUrl.imageName
            }
        }

        draw_background_name_tv.setOnClickListener {
            selectBackgroundPopWindow?.show(draw_background_name_tv)
        }

        //设置url为背景
        draw_upload_background_confirm_btn.setOnClickListener {
            val url = draw_upload_background_url_et.text.toString()
            setBackgroundForUrl(url, false)

        }
        //设置本地图片为背景
        draw_local_upload_background_btn.setOnClickListener {
            if (currentHolder == null) {
                Toast.makeText(this, "当前没有白板，请添加白板", Toast.LENGTH_SHORT).show()
            } else {
                UploadPicHelper.startChooseBackground(this)
            }

        }
        //  清除背景
        draw_clean_background_btn.setOnClickListener {
            if (currentHolder == null) {
                Toast.makeText(this, "当前没有白板，请添加白板", Toast.LENGTH_SHORT).show()
            } else {
                currentHolder?.clearBackgroundImage { errorCode ->
                    Log.d(TAG, "clearBackgroundImage() called with: errorCode = $errorCode")
                    if (errorCode != 0) {
                        ToastUtils.showCenterToast("errorCode = $errorCode")
                    }
                }
            }
        }


        // 清空当前页
        draw_empty_current_page_btn.setOnClickListener {
            currentHolder?.clearCurrentPage { p0 ->
                if (p0 != 0) {
                    ToastUtils.showCenterToast("errorCode:$p0")
                }
            }
        }
        // 清空所有页
        draw_empty_all_page_btn.setOnClickListener {
            currentHolder?.clear { p0 ->
                if (p0 != 0) {
                    ToastUtils.showCenterToast("errorCode:$p0")
                }
            }
        }
        // 删除选中图元
        draw_delete_selected_graphics_btn.setOnClickListener {
            currentHolder?.deleteSelectedGraphics { p0 ->
                if (p0 != 0) {
                    ToastUtils.showCenterToast("errorCode:$p0")
                }
            }
        }

        // 撤销
        draw_undo_btn.setOnClickListener {
            currentHolder?.undo()
        }
        // 重做
        draw_redo_btn.setOnClickListener {
            currentHolder?.redo()
        }
        // 文本内容确认
        draw_confirm_btn.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTextClickedTime > 1000) {
                var x = draw_incoming_text_x_et.text.toString()
                if (x.isEmpty()) {
                    x = "0"
                }
                var y = draw_incoming_text_y_et.text.toString()
                if (y.isEmpty()) {
                    y = "0"
                }
                currentHolder?.addText(
                    draw_text_content_et.text.toString(),
                    x.toInt(),
                    y.toInt()
                ) { p0 ->
                    ToastUtils.showCenterToast("errorCode:$p0")
                }
                draw_graffiti_tools_tv.text = getString(R.string.draw_graffiti_tools_selector)
                unSelectOtherChild(getString(R.string.draw_graffiti_tools_selector))
                ZegoWhiteboardManager.getInstance().toolType =
                    ZegoWhiteboardConstants.ZegoWhiteboardViewToolSelector
                lastTextClickedTime = currentTime
            }
        }
        // 更改文本框的默认值
        draw_default_text_keyboard_et.setText(ZegoWhiteboardManager.getInstance().customText)
        draw_default_text_keyboard_et.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                ZegoWhiteboardManager.getInstance().customText = s.toString()
            }
        })
    }

    private fun setHolderOperationMode() {
        val noneMode =
            if (mode_none.isChecked) ZegoWhiteboardConstants.ZegoWhiteboardOperationModeNone else 0
        val scaleMode =
            if (mode_scale.isChecked) ZegoWhiteboardConstants.ZegoWhiteboardOperationModeZoom else 0
        val scrollMode =
            if (mode_scroll.isChecked) ZegoWhiteboardConstants.ZegoWhiteboardOperationModeScroll else 0
        val drawMode =
            if (mode_draw.isChecked) ZegoWhiteboardConstants.ZegoWhiteboardOperationModeDraw else 0
        val mode = noneMode or scaleMode or scrollMode or drawMode
        currentHolder?.setOperationMode(mode)
        currentOpMode = mode
    }

    private fun setBackgroundForUrl(url: String, cleanUrlText: Boolean) {


        if (currentHolder == null) {
            Toast.makeText(this, "当前没有白板，请添加白板", Toast.LENGTH_SHORT).show()
        } else {

            currentHolder?.setBackgroundImage(
                url,
                fitMode
            ) { errorCode ->
                if (cleanUrlText) {
                    draw_upload_background_url_et.setText("")
                }
                when (errorCode) {
                    0 -> {
                        Toast.makeText(this, "${errorCode}: 背景设置成功", Toast.LENGTH_SHORT)
                            .show()
                    }
                    ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageSizeLimit -> {
                        Toast.makeText(
                            this,
                            "${errorCode}: 图片大小不能超过10M，请重新选择",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageTypeNotSupport -> {
                        Toast.makeText(this, "${errorCode}: 图片格式暂不支持", Toast.LENGTH_SHORT)
                            .show()
                    }
                    ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicIllegalAddress -> {
                        Toast.makeText(this, "${errorCode}: 非法图片URL", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        Toast.makeText(
                            this,
                            "errorCode = ${errorCode} ",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
    }

    /**
     * 返回当前控件的状态，设置为选中，并且取消选中其他控件
     */
    private fun unSelectOtherChild(it: String) {
        currentHolder?.setDocsScaleEnable(it != getString(R.string.draw_graffiti_tools_click))
        when (it) {
            getString(R.string.draw_graffiti_tools_text) -> {
                currentHolder?.addTextEdit { p0 ->
                    if (p0 != 0) {
                        ToastUtils.showCenterToast("errorCode:$p0")
                    } else {
                        draw_graffiti_tools_tv.text =
                            getString(R.string.draw_graffiti_tools_selector)
                    }
                }
            }
            else -> {
            }
        }
    }

    /**
     * 初始化右边白板模块
     */
    private fun initRightWhiteboard() {
        // 创建纯白板
        add_whiteboard.setOnClickListener {
            container.createPureWhiteboard { errorCode, holder ->
                if (errorCode == 0) {
                    drawer_whiteboard_list.addWhiteboard(holder.getCurrentWhiteboardModel())
                    holder.visibility = View.GONE
                    container.updateCurrentHolderToRoom(holder)
                }
            }
        }

        refreshUI()
        resize_whiteboard_confirm_btn.setOnClickListener {
            var width = 0
            var height = 0
            try {
                width = whiteboard_width_et.text.toString().toInt()
                height = whiteboard_height_et.text.toString().toInt()
            } catch (e: Exception) {
            }

            if (width <= 0 || height <= 0) {
                ToastUtils.showCenterToast("参数非法，请重新输入")
                return@setOnClickListener
            }

            currentHolder?.resizeLayout(Size(width, height))
        }

        setting_upload_log.setOnClickListener {
            VideoSDKManager.uploadLog()
        }
    }

    public fun refreshUI() {
        max_whiteboard_container_tv.visibility = View.GONE
        container.post {
            max_whiteboard_container_tv.text = String.format(
                resources.getString(R.string.whiteboard_max_container),
                container.width,
                container.height
            )

            currentHolder?.let {
                if (it.currentWhiteboardSize == Size(0, 0)) {
                    current_whiteboard_container_tv.text = String.format(
                        resources.getString(R.string.whiteboard_current_container),
                        container.width,
                        container.height
                    )
                    current_whiteboard_size_tv.text = String.format(
                        resources.getString(R.string.whiteboard_current_size),
                        container.width,
                        container.height
                    )
                } else {
                    current_whiteboard_container_tv.text = String.format(
                        resources.getString(R.string.whiteboard_current_container),
                        it.width,
                        it.height
                    )
                    current_whiteboard_size_tv.text = String.format(
                        resources.getString(R.string.whiteboard_current_size),
                        it.currentWhiteboardSize.width,
                        it.currentWhiteboardSize.height
                    )
                }
            }

            whiteboard_width_et.setText("")
            whiteboard_height_et.setText("")

            whiteboard_width_et.filters =
                arrayOf<InputFilter>(InputFilterMinMax(1, container.width))
            whiteboard_height_et.filters =
                arrayOf<InputFilter>(InputFilterMinMax(1, container.height))
        }
    }

    inner class InputFilterMinMax(private val min: Int, private val max: Int) : InputFilter {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            try {
                val input: Int =
                    (dest.subSequence(0, dstart).toString() + source + dest.subSequence(
                        dend,
                        dest.length
                    )).toInt()
                if (isInRange(min, max, input))
                    return null
            } catch (nfe: NumberFormatException) {
            }
            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

    /**
     * 初始化右边文件模块
     */
    private fun initRightDocs() {
        // 缩略图按钮
        thumbnail.setOnClickListener {
            // 有缩略图才去显示
            if (currentHolder != null) {
                if (currentHolder!!.getThumbnailUrlList().isNotEmpty()) {
                    ZegoViewAnimationUtils.startRightViewAnimation(docs_preview_list_parent, true)
                    if (currentHolder != null) {
                        docs_preview_list.setSelectedPage(currentHolder!!.getCurrentPage() - 1)
                    }
                } else {
                    ToastUtils.showCenterToast("该文件没有缩略图")
                }
            }
        }
        // 缩略图列表
        docs_preview_list.setSelectedListener { oldPage, newPage ->
            currentHolder?.let {
                if ((isScrollAuthorized) || it.isPureWhiteboard()) {
                    it.flipToPage(newPage + 1) { errorCode ->
                        if (errorCode != 0) {
                            ToastUtils.showCenterToast("errorCode:$errorCode")
                        }
                    }
                }
            }
        }
        docs_preview_list.setCloseBtnListener {
            docs_preview_list_parent.visibility = View.GONE
        }
        // 上传动态文件
        upload_dynamic.setOnClickListener {
            upload_state_tv.text = ""
            UploadFileHelper.uploadFile(
                this,
                ZegoDocsViewConstants.ZegoDocsViewRenderTypeDynamicPPTH5
            )
        }
        // 上传静态文件
        upload_static.setOnClickListener {
            upload_state_tv.text = ""
            UploadFileHelper.uploadFile(
                this,
                ZegoDocsViewConstants.ZegoDocsViewRenderTypeVectorAndIMG
            )
        }

        h5_width.setText("960")
        h5_height.setText("540")
        h5_pageCount.setText("5")
        upload_H5.setOnClickListener {
            upload_state_tv.text = ""
            val config = ZegoDocsViewCustomH5Config()

            config.width = if (h5_width.text.isNullOrEmpty()) {
                0
            } else {
                h5_width.text.toString().toInt()
            }
            config.height = if (h5_height.text.isNullOrEmpty()) {
                0
            } else {
                h5_height.text.toString().toInt()
            }
            config.pageCount = if (h5_pageCount.text.isNullOrEmpty()) {
                0
            } else {
                h5_pageCount.text.toString().toInt()
            }

            //zip里面缩略图的路径
            if (config.pageCount != 0) {
                config.thumbnailList = Array(config.pageCount) {
                    "thumbnails/${it + 1}.jpeg"
                }
            }

            UploadFileHelper.uploadH5File(this, config)
        }
        // 取消上传
        upload_cancel.setOnClickListener {
            UploadFileHelper.cancelUploadFile { errorCode: Int ->
                if (errorCode != 0) {
                    cancel_upload_state_tv.text =
                        getString(R.string.docs_upload_cancel_fail, errorCode.toString())
                } else {
                    cancel_upload_state_tv.text = getString(R.string.docs_upload_cancel_success)
                }
            }
        }
//        cache_url_et.setText("zc-WuRI15UJ5I4hf")
//        cache_url_et.setText("I9_9AXlII0hcA7jT")
//        cache_url_et.setText("95mZjwmxrp3IyzIS")
//        cache_url_et.setText("wfE6HPVaSNWY4a7r")
//        cache_url_et.setText("_Q_s9NfXFqOy_K9X")
        cache_url_et.setText("dTV7GNb1Ke99fg58")
        // 缓存
        cache_btn.setOnClickListener {
            val fileId = cache_url_et.text.toString()
            if (fileId.isEmpty()) {
                ToastUtils.showCenterToast(getString(R.string.docs_cache_file_id_null))
                return@setOnClickListener
            }
            cancel_cache.visibility = View.VISIBLE
            CacheHelper.cacheFile(
                this,
                fileId
            ) { errorCode: Int, state: Int, uploadPercent: Float ->
                Logger.i(
                    TAG,
                    "MainActivity cacheFile(fileID):${fileId}, state: $state, errorCode: $errorCode"
                )

                if (errorCode != 0) {
                    cancel_cache.visibility = View.INVISIBLE
                    cache_state_tv.text = getString(
                        R.string.docs_cache_fail,
                        fileId,
                        errorCode.toString()
                    )
                } else {
                    if (uploadPercent == 100f) {
                        cancel_cache.visibility = View.INVISIBLE
                    }
                    cache_state_tv.text =
                        getString(R.string.docs_cache_success, fileId, uploadPercent.toString())
                }
            }
        }

        cancel_cache.setOnClickListener {
            CacheHelper.cancelCache(this) { errorCode: Int ->

                if (errorCode != 0) {
                    cache_state_tv.text = getString(
                        R.string.docs_cancel_cache_fail,
                        errorCode.toString()
                    )
                } else {
                    cancel_cache.visibility = View.INVISIBLE
                    cache_state_tv.text = getString(R.string.docs_cancel_cache_success)
                }
            }
        }

        query_cache.setOnClickListener {
            val fileId = cache_url_et.text.toString()
            if (fileId.isNullOrBlank()) {
                ToastUtils.showCenterToast(getString(R.string.docs_cache_file_id_null))
                return@setOnClickListener
            }
            CacheHelper.queryFileCached(this, fileId) { errorCode: Int, exist: Boolean ->
                if (errorCode != 0) {
                    cache_state_tv.text = getString(R.string.docs_query_cache_fail)
                } else {
                    val existname = if (exist) {
                        getString(R.string.docs_cache_exist)
                    } else {
                        getString(
                            R.string.docs_cache_no_exist
                        )
                    };
                    cache_state_tv.text = getString(R.string.docs_query_cache_success, existname)
                }
            }
        }

        load_file_id.setOnClickListener {
            val fileId = cache_url_et.text.toString()
            if (fileId.isNullOrBlank()) {
                ToastUtils.showCenterToast(getString(R.string.docs_cache_file_id_null))
                return@setOnClickListener
            }
            CacheHelper.queryFileCached(this, fileId) { errorCode: Int, exist: Boolean ->
                if (errorCode != 0) {
                    cache_state_tv.text = getString(R.string.docs_query_cache_fail)
                } else {
                    val existname = if (exist) {
                        loadDocsFile(fileId)
                        getString(R.string.docs_cache_exist)
                    } else {
                        loadDocsFile(fileId)
                        getString(
                            R.string.docs_cache_no_exist
                        )
                    };
                    cache_state_tv.text = getString(R.string.docs_load_file, existname)
                }
            }
        }

        clear_all_cache.setOnClickListener {
            CacheHelper.clearAllCached(this@MainActivity)
            cache_state_tv.text = getString(R.string.docs_clear_all_cache)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(
            TAG,
            "onActivityResult() called with: requestCode = [$requestCode], resultCode = [$resultCode]"
        )
        if (requestCode == UploadFileHelper.REQUEST_CODE_UPLOAD_FILE || requestCode == UploadFileHelper.REQUEST_CODE_UPLOAD_H5) {
            // 上传文件回调
            UploadFileHelper.onActivityResult(
                this,
                requestCode,
                resultCode,
                data
            ) { errorCode: Int, state: Int, fileID: String?, percent: Float ->
                Log.d(
                    TAG,
                    "onActivityResult() called with: errorCode = [$errorCode], state = [$state], fileID = [$fileID],percent:$percent"
                )
                if (errorCode != 0) {
                    upload_state_tv.text =
                        getString(R.string.docs_upload_dynamic_fail, errorCode.toString())
                } else {
                    if (state == ZegoDocsViewConstants.ZegoDocsViewUploadStateUpload) {
                        upload_state_tv.text =
                            getString(R.string.docs_upload_dynamic_percent, percent.toString())
                        if (percent == 100f) {
                            upload_state_tv.text = getString(R.string.docs_upload_dynamic_convert)
                        }
                    } else if (state == ZegoDocsViewConstants.ZegoDocsViewUploadStateConvert) {
                        val string = getString(R.string.docs_upload_convert_success, fileID)
                        upload_state_tv.text = string
                        fileID?.run {
                            loadDocsFile(fileID)
                        }
                    }
                }
            }
        } else if (requestCode == UploadPicHelper.REQUEST_CODE_FOR_CHOOSE_PICTURE) {
            UploadPicHelper.handleActivityResult(this, requestCode, resultCode, data) { filePath ->
                val x = draw_upload_image_x_et.text.toString()
                val y = draw_upload_image_y_et.text.toString()
                if (x.isNotEmpty() && y.isNotEmpty()) {
                    currentHolder?.let { holder ->
                        holder.addImage(
                            ZegoWhiteboardViewImageType.ZegoWhiteboardViewImageGraphic,
                            filePath,
                            x.toInt(),
                            y.toInt()
                        ) { errorCode ->
                            when (errorCode) {
                                0 -> {
                                    Toast.makeText(this, "${errorCode}: 上传成功", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageSizeLimit -> {
                                    Toast.makeText(
                                        this,
                                        "${errorCode}: 图片大小不能超过10M，请重新选择",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                                ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageTypeNotSupport -> {
                                    Toast.makeText(
                                        this,
                                        "${errorCode}: 图片格式暂不支持",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicIllegalAddress -> {
                                    Toast.makeText(
                                        this,
                                        "${errorCode}: 非法图片URL",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    Toast.makeText(
                                        this,
                                        "${errorCode}: 上传失败，请重试",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        }
                    }
                } else {
                    Toast.makeText(this, "请填写坐标参数", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == UploadPicHelper.REQUEST_CODE_FOR_CHOOSE_BACKGROUND) {
            UploadPicHelper.handleActivityResult(this, requestCode, resultCode, data) { filePath ->

                currentHolder?.let { holder ->
                    holder.setBackgroundImage(
                        filePath,
                        fitMode,
                    ) { errorCode ->
                        when (errorCode) {
                            0 -> {
                                Toast.makeText(this, "${errorCode}: 上传成功", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageSizeLimit -> {
                                Toast.makeText(
                                    this,
                                    "${errorCode}: 图片大小不能超过10M，请重新选择",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                            ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageTypeNotSupport -> {
                                Toast.makeText(
                                    this,
                                    "${errorCode}: 图片格式暂不支持",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicIllegalAddress -> {
                                Toast.makeText(
                                    this,
                                    "${errorCode}: 非法图片URL",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                Toast.makeText(
                                    this,
                                    "${errorCode}: 上传失败，请重试",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * 加载文件
     * @param fileID 文件 ID
     */
    private fun loadDocsFile(fileID: String) {
        // 创建文件白板
        container.createFileWhiteBoardView(fileID) { errorCode, holder ->
            Logger.i(TAG, "createFileWhiteBoardView errorCode:$errorCode")
            if (errorCode == 0) {
                // 创建成功，添加白板
                drawer_whiteboard_list.addWhiteboard(holder.getCurrentWhiteboardModel())
                holder.setDocsViewAuthInfo(whiteboardAuthInfo)
            }
        }
    }

    /**
     * 初始化顶部栏切白板，切页等控制
     */
    private fun initTopLayout() {
        // 设置 Title
        main_top_room_name.text = getString(R.string.class_title, CONFERENCE_ID)
        // 退出课堂
        main_top_exit_btn.setOnClickListener { showExitClassDialog() }
        // 白板名称
        main_top_whiteboard_name.setOnClickListener {
            showRightDrawer(drawer_whiteboard_list)
        }
        // Excel 表格页名称
        main_top_sheet_name.setOnClickListener {
            showRightDrawer(drawer_excel_list)
            currentHolder?.let { holder ->
                drawer_excel_list.updateList(holder.getExcelSheetNameList())
            }
        }
        // 上一页
        main_page_prev.setOnClickListener {
            if (System.currentTimeMillis() - lastClickPageChangeTime < 500) {
                return@setOnClickListener
            }
            lastClickPageChangeTime = System.currentTimeMillis()
            currentHolder?.let {
                if ((isScrollAuthorized) || it.isPureWhiteboard()) {
                    currentHolder?.flipToPrevPage() { errorCode ->
                        if (errorCode != 0) {
                            ToastUtils.showCenterToast("errorCode:$errorCode")
                        }
                    }
                }
            }
        }
        // 下一页s
        main_page_next.setOnClickListener {
            if (System.currentTimeMillis() - lastClickPageChangeTime < 500) {
                return@setOnClickListener
            }
            lastClickPageChangeTime = System.currentTimeMillis()
            currentHolder?.let {
                if ((isScrollAuthorized) || it.isPureWhiteboard()) {
                    currentHolder?.flipToNextPage() { errorCode ->
                        if (errorCode != 0) {
                            ToastUtils.showCenterToast("errorCode:$errorCode")
                        }
                    }
                }
            }
        }
        // 跳到指定页数
        jump_btn.setOnClickListener {
            if (System.currentTimeMillis() - lastClickPageJumpChangeTime < 500) {
                return@setOnClickListener
            }
            lastClickPageJumpChangeTime = System.currentTimeMillis()
            currentHolder?.let {
                if ((isScrollAuthorized) || it.isPureWhiteboard()) {
                    currentHolder?.flipToPage(
                        main_page_to_index.text.toString().toIntOrNull() ?: 0
                    ) { errorCode ->
                        if (errorCode != 0) {
                            ToastUtils.showCenterToast("errorCode:$errorCode")
                        }
                    }
                }
            }
        }
        // 上一步
        main_step_prev.setOnClickListener {
            if (System.currentTimeMillis() - lastClickStepChangeTime < 500) {
                return@setOnClickListener
            }
            lastClickStepChangeTime = System.currentTimeMillis()
            currentHolder?.let {
                if (isScrollAuthorized) {
                    currentHolder?.previousStep {
                        if (it != 0) {
                            ToastUtils.showCenterToast("errorCode:$it")
                        }
                    }
                }
            }
        }
        // 下一步
        main_step_next.setOnClickListener {
            if (System.currentTimeMillis() - lastClickStepChangeTime < 500) {
                return@setOnClickListener
            }
            lastClickStepChangeTime = System.currentTimeMillis()
            currentHolder?.let {
                if (isScrollAuthorized) {
                    currentHolder?.nextStep {
                        if (it != 0) {
                            ToastUtils.showCenterToast("errorCode:$it")
                        }
                    }
                }
            }

        }
        userid_tv.setText("UID:${VideoSDKManager.getUserID()}")
    }

    /**
     * 初始化 Drawer 中已打开的列表
     */
    private fun initDrawerRight() {
        layout_main_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        layout_main_drawer.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerClosed(drawerView: View) {
                layout_drawer_right.children.forEach {
                    it.visibility = View.GONE
                }
            }

            override fun onDrawerOpened(drawerView: View) {
            }
        })

        // 已打开白板列表
        drawer_whiteboard_list.let { wbList ->
            wbList.drawerParent = layout_main_drawer
            wbList.setWhiteboardItemSelectedListener {
                // 获取当前所选白板 ID
                val holder = container.getWhiteboardViewHolder(it!!.whiteboardID)

                // 更新当前白板为所选白板
                holder?.let {
                    container.updateCurrentHolderToRoom(holder)
                }
                layout_main_drawer.closeDrawer(GravityCompat.END)
            }

            wbList.setWhiteboardItemDeleteListener {
                container.deleteWhiteboard(it) { errorCode, deleteHolder, IDList ->
                    if (errorCode == 0) {
                        drawer_whiteboard_list.removeWhiteboard(it)
                        if (deleteHolder.visibility == View.VISIBLE) {
                            // 获取下一个白板 ID
                            val nextSelectID = wbList.getNextSelectID(it)
                            val nextHolder = container.getWhiteboardViewHolder(nextSelectID)
                            // 更新当前白板为下一个白板
                            nextHolder?.let {
                                container.updateCurrentHolderToRoom(nextHolder)
                            }
                            // 更新相关视图
                            updatePreviewRelations()
                        }
                    }
                }
            }
        }

        // 已打开的 Excel 文件 Sheet 列表
        drawer_excel_list.setExcelClickedListener {
            currentHolder?.let { holder ->
                layout_main_drawer.closeDrawer(GravityCompat.END)
                holder.selectExcelSheet(it) { name, whiteboardID ->
                    main_top_sheet_name.text = name
                    container.selectWhiteboardViewHolder(whiteboardID)
                }
            }
        }
    }

    /**
     * 隐藏虚拟按键，并且全屏 tool
     */
    private fun hideBottomUIMenu() {
        // for new api versions.
        val decorView = window.decorView

        // SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 配合 SYSTEM_UI_FLAG_FULLSCREEN 一起使用，效果使得状态栏出现的时候不会挤压activity高度
        // SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION 配合 SYSTEM_UI_FLAG_HIDE_NAVIGATION 一起使用，效果使得导航栏出现的时候不会挤压activity高度
        val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideBottomUIMenu()
        }
    }

    override fun onBackPressed() {
        showExitClassDialog()
    }

    /**
     * 退出房间 Dialog
     */
    private fun showExitClassDialog() {
        ZegoDialog.Builder(this)
            .setTitle(R.string.leave)
            .setMessage(R.string.exit_room_ensure)
            .setPositiveButton(R.string.button_confirm) { dialog, _ ->
                dialog.dismiss()
                // 退出房间
                VideoSDKManager.exitRoom()
                finish()
            }
            .setNegativeButton(R.string.button_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 添加白板 view 的 listener
     */
    private fun addWhiteboardViewListener() {
        WhiteboardSDKManager.setWhiteboardCountListener(
            object : IZegoWhiteboardManagerListener {
                override fun onWhiteboardAdded(zegoWhiteboardView: ZegoWhiteboardView) {
                    val model = zegoWhiteboardView.whiteboardViewModel
                    Logger.i(TAG, "onWhiteboardAdded:${model.name}")
                    if (getListFinished) {
                        container.onReceiveWhiteboardView(zegoWhiteboardView) { errorCode, newHolder, holder ->
                            // 不管创建和加载是否成功，都要显示出来
                            updatePreviewRelations()
                            selectDefaultChild()
                            if (newHolder) {
                                drawer_whiteboard_list.addWhiteboard(model)
                            }
                            holder.setDocsViewAuthInfo(whiteboardAuthInfo)
                        }
                    } else {
                        tempWbList.add(zegoWhiteboardView)
                    }
                }

                override fun onWhiteboardRemoved(whiteboardID: Long) {
                    if (currentHolder?.currentWhiteboardID == whiteboardID) {
                        updatePreviewRelations()
                        selectDefaultChild()
                    }
                    container.removeWhiteboardViewHolder(whiteboardID)
                    drawer_whiteboard_list.removeWhiteboard(whiteboardID)
                }

                override fun onError(errorCode: Int) {
                    Log.d(TAG, "onError() called with: errorCode = $errorCode")
                    ToastUtils.showCenterToast("errorCode:$errorCode")
                }

                override fun onWhiteboardAuthChanged(authInfo: HashMap<String, Int>) {
                    super.onWhiteboardAuthChanged(authInfo)
                    Log.d(TAG, "onWhiteboardAuthChanged() called with: authInfo = $authInfo")
//                    auth_whiteboard.setText(authInfo.toString())
                    // 从白板的回到中获取到 滚动 的权限状态，更新相关的操作权限
                    isScrollAuthorized = authInfo["scroll"] == 1
                    whiteboardAuthInfo.putAll(authInfo)
                    container.getWhiteboardViewHolderList().forEach {
                        it.setDocsViewAuthInfo(whiteboardAuthInfo)
                    }
                }

                override fun onWhiteboardGraphicAuthChanged(authInfo: HashMap<String, Int>) {
                    super.onWhiteboardGraphicAuthChanged(authInfo)
                    Log.d(TAG, "onWhiteboardGraphicAuthChanged() called with: authInfo = $authInfo")
//                    auth_graph.setText(authInfo.toString())
                }
            })

        with(container) {
            setChildCountChangedListener { count: Int ->
                if (count == 0) {
                    main_top_whiteboard_name.text = ""
                    main_top_page_layout.visibility = View.GONE
                    main_top_sheet_name.visibility = View.GONE
                    main_top_step_layout.visibility = View.GONE
                    main_top_jump_page_layout.visibility = View.GONE
                    currentHolder = null
                }
            }

            setWhiteboardSelectListener {
                Logger.i(TAG, "container onWhiteboardSelectListener,${it}")
                val holder = container.getWhiteboardViewHolder(it)!!
                onWhiteboardHolderSelected(holder)
                updatePreviewRelations()
            }

            setWhiteboardScrollListener { currentPage: Int, pageCount: Int ->
                Logger.i(
                    TAG,
                    "setWhiteboardScrollListener(),currentPage:${currentPage},pageCount:${pageCount}, ${currentHolder?.getCurrentPage()}"
                )
                currentHolder?.let { _ ->
                    main_page_index.text =
                        "%s/%s".format(currentPage.toString(), pageCount.toString())
                    docs_preview_list.setSelectedPage(currentPage - 1)
                }


            }

        }
    }

    /**
     * 若选中点击工具，切换到其他非动态 PPT 格式，如静态 ppt、pdf、doc、docx 等
     * 则默认选中画笔工具
     */
    fun selectDefaultChild() {
        draw_graffiti_tools_tv.text = getString(R.string.draw_graffiti_tools_pen)
        unSelectOtherChild(getString(R.string.draw_graffiti_tools_pen))
        ZegoWhiteboardManager.getInstance().toolType =
            ZegoWhiteboardConstants.ZegoWhiteboardViewToolPen
    }

    private fun onWhiteboardHolderSelected(holder: ZegoWhiteboardViewHolder) {
        Log.d(
            TAG,
            "onWhiteboardHolderSelected() called with: holder = $holder,currentHolder:$currentHolder"
        )
        val holderChanged = currentHolder != holder
        // onResume 触发的重复设置不再更新，主要是控件的状态不要重置为默认状态。
        currentHolder = holder
        refreshUI()

        main_top_whiteboard_name.text = holder.getCurrentWhiteboardName()

        if (holder.isPureWhiteboard() || holder.isDocsViewLoadSuccessed()) {
            main_page_index.text = "%s/%s".format(
                holder.getCurrentPage().toString(),
                holder.getPageCount().toString()
            )
        } else {
            main_page_index.text = ""
        }

        Logger.i(TAG, "holder.isExcel():${holder.isExcel()}")
        if (holder.isExcel()) {
            main_top_sheet_name.text = holder.getCurrentWhiteboardModel().fileInfo.fileName
        }

        main_top_sheet_name.visibility = if (holder.isExcel()) View.VISIBLE else View.GONE
        main_top_jump_page_layout.visibility =
            if (holder.isExcel()) View.GONE else View.VISIBLE
        main_top_page_layout.visibility = if (holder.isExcel()) View.GONE else View.VISIBLE
        main_top_step_layout.visibility =
            if (holder.isDisplayedByWebView()) View.VISIBLE else View.GONE

        if (holderChanged) {
            mode_none.isChecked = false
            mode_scale.isChecked = true
            mode_scroll.isChecked = false
            mode_draw.isChecked = true

            selectDefaultChild()
            holder.setOperationMode(SelectOpModePopWindow.DRAW_SCALE)

            // 选中白板的时候，要设置当前的自定义图形
            selectCustomImagePopWindow?.let {
                holder.addImage(
                    ZegoWhiteboardViewImageType.ZegoWhiteboardViewImageCustom,
                    it.mSelectImage.url,
                    0,
                    0
                ) { errorCode ->
                    when (errorCode) {
                        0 -> {
                            draw_custom_image_tv.text = it.mSelectImage.imageName
                        }
                        ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageSizeLimit -> {
                            Toast.makeText(
                                this,
                                "${errorCode}: 图片大小不能超过500k，请重新选择",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                        ZegoWhiteboardConstants.ZegoWhiteboardViewErrorGraphicImageTypeNotSupport -> {
                            Toast.makeText(this, "${errorCode}: 图片格式暂不支持", Toast.LENGTH_SHORT)
                                .show()
                        }
                        else -> {
                            Toast.makeText(
                                this,
                                "${errorCode}: " + getString(R.string.draw_input_url),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            }
        }
        selectGraffitiToolsPopWindow?.dismiss()
        // currentWhiteboard有可能不等于List里面的白板，所以遍历一下
        holder.getWhiteboardIDList().forEach {
            drawer_whiteboard_list.setSelectedWhiteboard(it)
        }

    }

    /**
     * 开始监听房间内的消息
     */
    private fun setRoomListeners() {
        VideoSDKManager.setRoomStateListener(object : IZegoRoomStateListener {
            override fun onConnected(errorCode: Int, roomID: String) {
                dismissLoadingDialog(loadingDialog)
            }

            override fun onDisconnect(errorCode: Int, roomID: String) {
                dismissLoadingDialog(loadingDialog)
                VideoSDKManager.exitRoom()
                finish()
            }

            override fun connecting(errorCode: Int, roomID: String) {
                showLoadingDialog(loadingDialog, getString(R.string.network_temp_broken_reconnect))
            }
        })
    }

    /**
     * 更新预览相关的 view，调用时机
     * 1、新增白板
     * 2、切换白板
     * 3、删除白板
     */
    private fun updatePreviewRelations() {
        if (currentHolder != null && currentHolder!!.getThumbnailUrlList().isNotEmpty()) {
            // 有缩略图，显示预览按钮
            docs_preview_list.setThumbnailUrlList(currentHolder!!.getThumbnailUrlList())
        } else {
            // 没有缩略图,不显示预览按钮
            docs_preview_list.setThumbnailUrlList(ArrayList())
        }
        // 业务需求，隐藏缩略图列表
        docs_preview_list_parent.visibility = View.INVISIBLE
    }

    /**
     * 请求白板列表
     */
    private fun requestWhiteboardList() {
        WhiteboardSDKManager.getWhiteboardViewList { errorCode, whiteboardViewList ->
            Logger.i(
                TAG,
                "requestWhiteboardList:errorCode;$errorCode,whiteboardViewList:${whiteboardViewList.size}"
            )
            container.resize(this)
            if (errorCode == 0) {
                if (whiteboardViewList.isEmpty()) {
                    container.createPureWhiteboard { createErrorCode, holder ->
                        if (createErrorCode == 0) {
                            drawer_whiteboard_list.addWhiteboard(holder.getCurrentWhiteboardModel())
                            holder.visibility = View.GONE
                            container.updateCurrentHolderToRoom(holder)
                            getListFinished = true
                        }
                    }
                } else {
                    val list = mutableListOf<ZegoWhiteboardView>()
                    list.addAll(whiteboardViewList)
                    Logger.d(TAG, "tempWbList.size:${tempWbList.size}")
                    tempWbList.forEach {
                        val tempWhiteboardID = it.whiteboardViewModel.whiteboardID
                        val firstOrNull = list.firstOrNull { item ->
                            tempWhiteboardID == item.whiteboardViewModel.whiteboardID
                        }
                        if (firstOrNull == null) {
                            list.add(it)
                        } else {
                            Logger.i(TAG, "already added :${it.whiteboardViewModel.name}")
                        }
                    }
                    tempWbList.clear()

                    list.forEach {
                        val model = it.whiteboardViewModel
                        val fileType = model.fileInfo.fileType
                        if (fileType != ZegoDocsViewConstants.ZegoDocsViewFileTypeELS) {
                            drawer_whiteboard_list.addWhiteboard(model)
                        } else {
                            if (!drawer_whiteboard_list.containsFileID(model)) {
                                drawer_whiteboard_list.addWhiteboard(model)
                            }
                        }
                    }

                    container.onEnterRoomReceiveWhiteboardList(list) { resultCode ->
                        if (resultCode == 0) {
                            val holderList = container.getWhiteboardViewHolderList()
                            holderList.forEach {
                                it.setDocsViewAuthInfo(whiteboardAuthInfo)
                            }
                            currentHolder?.let {
                                onWhiteboardHolderSelected(it)
                            }
                            Logger.i(
                                TAG,
                                "process Enter List finished,resultCode = $resultCode,holderList:${holderList.size}"
                            )
                        } else {
                            Logger.i(TAG, "process Enter List finished,resultCode = $resultCode")
                        }
                    }

                    getListFinished = true
                }
            } else {
                getListFinished = true
                Toast.makeText(
                    this,
                    "获取白板列表失败，errorCode = $errorCode",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showRightDrawer(drawerChild: View) {
        layout_main_drawer.openDrawer(GravityCompat.END)
        layout_drawer_right.children.forEach {
            it.visibility = if (it == drawerChild) View.VISIBLE else View.GONE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (this.getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            setLandscape()
        } else if (this.getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            setPortrait()
        }
    }

    fun setLandscape() {
        main_menu.visibility = View.GONE
        main_end_layout.visibility = View.VISIBLE

        var mainWhiteboardLayoutParams: ConstraintLayout.LayoutParams =
            ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.MATCH_PARENT)
        mainWhiteboardLayoutParams.bottomToBottom = 0
        mainWhiteboardLayoutParams.endToStart = R.id.main_end_layout
        mainWhiteboardLayoutParams.horizontalBias = 0.5f
        mainWhiteboardLayoutParams.horizontalChainStyle = 2
        mainWhiteboardLayoutParams.startToStart = 0
        mainWhiteboardLayoutParams.topToTop = 0
        main_whiteboard_layout.layoutParams = mainWhiteboardLayoutParams

        var mainEndLayoutParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            dp2px(this, 200F).toInt(),
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        mainEndLayoutParams.startToEnd = R.id.main_whiteboard_layout
        mainEndLayoutParams.endToEnd = 0
        mainEndLayoutParams.topToTop = 0
        mainEndLayoutParams.bottomToBottom = 0
        main_end_layout.layoutParams = mainEndLayoutParams

        var mainTopLayoutParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            dp2px(this, 98F).toInt()
        )
        mainTopLayoutParams.bottomToTop = R.id.v_container_top
        mainTopLayoutParams.topToBottom = R.id.main_menu
        mainTopLayoutParams.leftToLeft = 0
        main_top_layout.layoutParams = mainTopLayoutParams

        currentHolder?.resizeLayout(
            Size(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    fun setPortrait() {
        main_menu.visibility = View.VISIBLE
        main_end_layout.visibility = View.GONE

        var mainWhiteboardLayoutParams: ConstraintLayout.LayoutParams =
            ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.MATCH_PARENT)
        mainWhiteboardLayoutParams.bottomToBottom = 0
        mainWhiteboardLayoutParams.endToEnd = 0
        mainWhiteboardLayoutParams.verticalBias = 0f
        mainWhiteboardLayoutParams.verticalChainStyle = 2
        mainWhiteboardLayoutParams.startToStart = 0
        mainWhiteboardLayoutParams.topToTop = 0
        main_whiteboard_layout.layoutParams = mainWhiteboardLayoutParams

        var mainEndLayoutParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            dp2px(this, 200F).toInt(),
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        mainEndLayoutParams.rightToRight = 0
        mainEndLayoutParams.topToTop = 0
        mainEndLayoutParams.bottomToBottom = 0

        main_end_layout.layoutParams = mainEndLayoutParams

        var mainTopLayoutParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            dp2px(this, 98F).toInt()
        )
        mainTopLayoutParams.bottomToTop = R.id.v_container_top
        mainTopLayoutParams.topToBottom = R.id.main_menu
        mainTopLayoutParams.leftToLeft = 0
        main_top_layout.layoutParams = mainTopLayoutParams

        currentHolder?.resizeLayout(
            Size(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

    }

}