package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appContext
import com.box.common.appViewModel
import com.box.common.data.GameValuationCommitRequest
import com.box.common.data.model.ModDataBean
import com.box.common.data.model.ModImageUriBean
import com.box.common.data.model.ModUserInfo
import com.box.common.data.model.ModValuationCommitBean
import com.box.common.data.model.UploadResponseString
import com.box.common.eventViewModel
import com.box.common.network.apiService
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.utils.ext.logsE
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.BR.modData
import com.box.mod.BR.modImageUriBean
import com.box.mod.R
import com.box.mod.databinding.ModFragmentGujiaRealBinding
import com.box.mod.databinding.ModItemCustomFormPicBinding
import com.box.mod.databinding.ModItemGujiaGameBinding
import com.box.mod.ui.activity.ModActivityLogin
import com.box.mod.ui.adapter.ModCustomViewAdapter
import com.box.mod.ui.xpop.ModXPopupCenterGuJiaCommit
import com.box.mod.view.xpop.ModXPopupCenterPermissions
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.TimeUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt
import kotlin.random.Random
import com.box.com.R as RC


class ModFragmentGuJiaReal :
    BaseTitleBarFragment<ModFragmentGuJiaReal.Model, ModFragmentGujiaRealBinding>() {
    // ModFragmentGuJiaReal.kt (大约在 37 行)
    private var isInitialSelectionHandled = false
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                val position = mViewModel.pic.get()
                if (position >= 0 && position < upPicList.size) {
                    upPicList[position].files = uri
                    upPicAdapter.notifyItemChanged(position)
                } else {
                    Toaster.show("图片位置索引错误")
                }
            } else {
                Toaster.show("未选择任何图片")
            }
        }

    private val commitViewAdapter = ModCustomViewAdapter()
    var upPicList: MutableList<ModImageUriBean> = MutableList(8) {
        ModImageUriBean(null)
    }

    private val upPicAdapter = ModGameUpPicAdapter()

    private val commitRequest = GameValuationCommitRequest()

    var mHandler: Handler = Handler(Looper.getMainLooper())

    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_gujia_real

    companion object {
        fun newInstance(): ModFragmentGuJiaReal {
            return ModFragmentGuJiaReal()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }
        MMKVConfig.userInfo?.let { savedUser ->
            eventViewModel.isLogin.value = true
            appViewModel.modUserInfo.value = savedUser
        }
        /**********************************************************************************/
        mDataBinding.tabLayout.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            if (fromUser) {
                // 清空数据 ---
                clearData()
                //获取新的输入列表
                mViewModel.clickGameItem.value =
                    mViewModel.gameList.value?.get(toIndex) ?: ModDataBean()
                // 重新请求数据
                mViewModel.getCustomFromData(mViewModel.clickGameItem.value?.id.toString())
            }
        }
        mDataBinding.tabLayout.apply {
            configTabLayoutConfig {
                tabMinScale = 0.9f
                tabMaxScale = 1f
                tabEnableGradientScale = true
                tabEnableGradientColor = true
                tabEnableIndicatorGradientColor = true
                onSelectViewChange = { fromView, selectViewList, reselect, fromUser ->
                    val toView = selectViewList.first()
                    fromView?.findViewById<CardView>(R.id.card_view_root)
                        ?.setCardBackgroundColor("#FFFFFF".toColorInt())
                    toView.findViewById<CardView>(R.id.card_view_root)
                        ?.setCardBackgroundColor("#66C5FF".toColorInt())
                    fromView?.findViewById<LinearLayout>(R.id.textRootView)?.background =
                        (resources.getDrawable(R.drawable.gujia_item_bg, null))
                    toView.findViewById<LinearLayout>(R.id.textRootView)?.background =
                        (resources.getDrawable(R.drawable.mod_gujia_item_selector, null))
                    fromView?.findViewById<TextView>(R.id.name)
                        ?.setTextColor("#253E75".toColorInt())
                    toView.findViewById<TextView>(R.id.name)?.setTextColor("#FFFFFF".toColorInt())
                }
            }
        }

        /**********************************************************************************/

        mDataBinding.recyclerView2.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = commitViewAdapter
        }

        /**********************************************************************************/
        mDataBinding.recyclerView3.run {
            layoutManager = GridLayoutManager(context, 4)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 8).toInt()))
            adapter = upPicAdapter
        }

        upPicAdapter.setOnItemClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModImageUriBean
            if (mViewModel.modUserInfo.value == null) {
                ModActivityLogin.start(appContext)
                Toaster.show("请先登录")
            } else {
                selectPhoto(position)
            }

        }

        upPicAdapter.setList(upPicList)


    }

    override fun isStatusBarEnabled(): Boolean {
        return false
    }

    override fun getTitleBar(): TitleBar {
        return mDataBinding.titleBar
    }


    override fun createObserver() {
        mViewModel.gameListResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    mViewModel.gameList.value = data
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        mViewModel.customFromResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data != null) {
                        commitViewAdapter.updateList(data)
                    } else {
                        commitViewAdapter.setList(null)
                    }
                    commitViewAdapter.resetAnimationState()
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        mViewModel.gameList.observe(this) {
            mDataBinding.tabLayout.removeAllViews()
            val inflater = LayoutInflater.from(context)
            it.forEach { item ->
                val binding = DataBindingUtil.inflate<ModItemGujiaGameBinding>(
                    inflater,
                    R.layout.mod_item_gujia_game,
                    mDataBinding.tabLayout,
                    false
                )
                binding.modData = item
                binding.executePendingBindings()
                mDataBinding.tabLayout.addView(binding.root)
            }

            if (it.isNotEmpty()) {
                if (!isInitialSelectionHandled) {
                    isInitialSelectionHandled = true
                    //获取 EventViewModel 中的指令值
                    val targetIndex = eventViewModel.guJiaCurrentItem.value
                    //如果有有效的指令值，则使用它；否则使用默认值 0
                    val finalIndex =
                        if (targetIndex != null && targetIndex >= 0 && targetIndex < it.size) {
                            targetIndex
                        } else {
                            0 // 默认选中第一个 Tab
                        }
                    // 执行选中 Tab 和请求数据的操作
                    handleTabSelectionAndRequest(finalIndex, it)
                    //清空 EventViewModel 中的指令，避免下次返回时误选中
                    eventViewModel.guJiaCurrentItem.value = null
                }
            }
        }

        eventViewModel.guJiaCurrentItem.observe(this) { targetIndex ->
            val gameList = mViewModel.gameList.value
            // 确保 Tab 列表已加载完毕且列表非空，且 targetIndex 有效
            if (isInitialSelectionHandled && gameList != null && gameList.isNotEmpty() && targetIndex != null) {
                val finalIndex = if (targetIndex >= 0 && targetIndex < gameList.size) {
                    targetIndex
                } else {
                    0 // 安全回退到默认索引
                }
                // 执行选中 Tab 和请求数据的操作
                handleTabSelectionAndRequest(finalIndex, gameList)
                // 重置 UnPeekLiveData，防止下次 Fragment Resume 时重复触发
                eventViewModel.guJiaCurrentItem.value = null
            }
        }

        /*****************图片上传完成***************/
        mViewModel.uploadFileState.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE("上传成功，原始数据: ${GsonUtils.toJson(data)}")
                    mViewModel.uploadedFileNameList.value = data?.fileNames.toSafeMutableList()
                    //val allFileNames = data?.flatMap { it.fileNames }?.toMutableList()
                    // 将合并后的文件名列表赋值给 uploadedUrls
                    //mViewModel.uploadedFileNameList.value = allFileNames
                    //logsE("已提取的文件名列表: $allFileNames")
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }


        mViewModel.uploadedFileNameList.observe(this) { fileNamesList ->
            val gameItem = mViewModel.clickGameItem.value
            val gameId = gameItem?.id.toString()
            val gameName = gameItem?.name.toString()
            val isGameIdOne = gameId == "1"

            commitRequest.apply {
                createTime = TimeUtils.getNowString()
                characteristic = mViewModel.shuomingText.get()
                fileNames = fileNamesList
                this.gameId = gameId
                this.gameName = if (isGameIdOne) froms[0].content else gameName
                userId = MMKVConfig.userInfo?.userId.toString()
            }

            mViewModel.postValuationCommit(commitRequest)
            logsE("提交的原始数据: ${GsonUtils.toJson(commitRequest)}")
            showCommitPop()
            eventViewModel.updateMessage.value = true
        }


        mViewModel.valuationCommitList.observe(this) {
            commitRequest.froms = it
        }

        appViewModel.modUserInfo.observe(this) {
            mViewModel.modUserInfo.value = it
        }


    }

    /**
     * 封装选中 Tab 和延迟请求数据的逻辑
     */
    private fun handleTabSelectionAndRequest(index: Int, gameList: List<ModDataBean>) {
        // 设置 Tab 选中项
        mDataBinding.tabLayout.setCurrentItem(index, true)
        //延迟 500ms（0.5 秒）执行数据请求
        Handler(Looper.getMainLooper()).postDelayed({
            // 确保列表非空且索引有效
            if (gameList.size > index) {
                mViewModel.clickGameItem.value = gameList[index]
                mViewModel.getCustomFromData(gameList[index].id.toString())
            }
        }, 500)
    }

    /**
     * 用于将逗号分隔的字符串转换为 MutableList<String>
     *
     * @param separator 用于分隔字符串的字符，默认为逗号 ","
     * @return 包含分隔后元素的 MutableList<String>，如果原始字符串为 null 或空，返回空的 MutableList
     */
    fun String?.toSafeMutableList(separator: String = ","): MutableList<String> {
        // 1. 如果原始字符串是 null，返回空的 MutableList
        val list = this?.split(separator)
            // 2. 移除每个元素前后的空格
            ?.map { it.trim() }
            // 3. 过滤掉空字符串（例如分隔符前后没有内容）
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        // 4. 将最终的 List<String> 转换为 MutableList<String>
        return list.toMutableList()
    }

    override fun lazyLoadData() {
        mViewModel.getGameListData()
    }

    override fun onNetworkStateChanged(it: NetState) {
    }

    fun showCommitPop() {
        XPopup.Builder(context)
            .dismissOnTouchOutside(false)
            .dismissOnBackPressed(false)
            .isDestroyOnDismiss(true)
            .hasStatusBar(true)
            .isLightStatusBar(true)
            .animationDuration(5)
            .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
            .hasNavigationBar(true)
            .asCustom(
                ModXPopupCenterGuJiaCommit(
                    mActivity,
                    ""
                ) {
                    clearData()
                })
            .show()
    }

    fun clearData() {
        mViewModel.shuomingText.set("")
        upPicList = MutableList(8) { ModImageUriBean(null) }
        upPicAdapter.setList(upPicList)
        //mViewModel.getCustomFromData(mViewModel.clickGameItem.value?.id.toString())
    }

    /**********************************************Click**************************************************/
    inner class ProxyClick {

        fun confirm() {
            if (mViewModel.modUserInfo.value == null) {
                ModActivityLogin.start(appContext)
                return
            }
            val formItemsList = commitViewAdapter.data
            // 遍历列表，收集或校验数据
            val commitDataMap = mutableMapOf<String, String>()
            var errorIndex = -1 // 用于保存第一个出错的 item 索引
            var validationError: String? = null
            for ((index, item) in formItemsList.withIndex()) {
                // item.content 包含用户输入的最新值
                val value = item.content
                if (value.isNullOrBlank()) {
                    validationError = "${item.title} 不能为空" // 或 "未填写${item.title}"
                    errorIndex = index // 记录第一个错误的索引
                    break // 发现第一个错误即停止
                }
                // 将字段标题和输入值存入 Map
                commitDataMap[item.title] = value
            }

            if (validationError != null && errorIndex != -1) {
                // 如果有错误，显示提示信息
                Toaster.show(validationError)
                // 滚动到出错的位置
                mDataBinding.recyclerView2.smoothScrollToPosition(errorIndex)
                // 延迟执行动画，给滚动一点时间
                mHandler.postDelayed({
                    // 尝试获取滚动后的 item view
                    val view =
                        mDataBinding.recyclerView2.layoutManager?.findViewByPosition(errorIndex)
                    view?.startAnimation(
                        AnimationUtils.loadAnimation(
                            appContext,
                            RC.anim.shake_anim // 使用 RC alias
                        )
                    )
                }, 100) // 100毫秒延迟
                return // 停止执行
            }

            val uploadedFilesCount = upPicList.count { it.files != null }
            if (uploadedFilesCount < 3) {
                Toaster.show("请至少上传3张图片")
                return
            }
            mViewModel.valuationCommitList.value = commitViewAdapter.data
            mViewModel.uploadCommitFiles(appContext, upPicList)
        }

    }


    private fun selectPhoto(pic: Int) {
        if (!MMKVConfig.permissionsAlbum) {
            XPopup.Builder(context)
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .isLightStatusBar(true)
                .animationDuration(5)
                .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                .hasNavigationBar(true)
                .asCustom(
                    ModXPopupCenterPermissions(mActivity, "相册", "用于实现图片选择功能", {
                        MMKVConfig.permissionsAlbum = true
                        mViewModel.pic.set(pic)
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {

                    })
                .show()
        } else {
            mViewModel.pic.set(pic)
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

    }

    /**********************************************Model**************************************************/
    class ModGameListAdapter :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemGujiaGameBinding>>(
            R.layout.mod_item_gujia_game
        ) {

        override fun convert(
            holder: BaseDataBindingHolder<ModItemGujiaGameBinding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.let {
                it.setVariable(modData, item)
                it.executePendingBindings()
            }
        }
    }

    class ModGameUpPicAdapter :
        BaseQuickAdapter<ModImageUriBean, BaseDataBindingHolder<ModItemCustomFormPicBinding>>(
            R.layout.mod_item_custom_form_pic
        ) {

        override fun convert(
            holder: BaseDataBindingHolder<ModItemCustomFormPicBinding>,
            item: ModImageUriBean
        ) {
            holder.dataBinding?.let {
                it.setVariable(modImageUriBean, item)
                it.executePendingBindings()
            }
        }
    }

    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "游戏账号估值") {
        val modUserInfo = MutableLiveData<ModUserInfo>()
        var uploadedFileNameList = MutableLiveData<MutableList<String>>()
        var valuationCommitList = MutableLiveData<MutableList<ModValuationCommitBean>>()
        var uploadFileState = MutableLiveData<ModResultStateWithMsg<UploadResponseString>>()
        var shuomingText = StringObservableField("")
        var clickGameItem = MutableLiveData<ModDataBean>()
        var gameList = MutableLiveData<MutableList<ModDataBean>>()
        var gameListResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        var customFromResult =
            MutableLiveData<ModResultStateWithMsg<MutableList<ModValuationCommitBean>>>()
        private var customFromDataJob: Job? = null

        fun getGameListData() {
            modRequestWithMsg({
                apiService.getValuationCommitGameList()
            }, gameListResult)
        }

        fun getCustomFromData(id: String) {
            // 在发起新请求之前，取消上一个
            customFromDataJob?.cancel()
            // 使用 viewModelScope.launch 启动新协程，并保存它的 Job
            // 这样当 cancel() 被调用时，这个协程块会被取消
            customFromDataJob = viewModelScope.launch {
                modRequestWithMsg({
                    apiService.getValuationCommitGameFrom(id)
                }, customFromResult)
            }
        }

        var postValuationCommitResult = MutableLiveData<ModResultStateWithMsg<Any>>()

        fun postValuationCommit(request: GameValuationCommitRequest) {
            modRequestWithMsg(
                { apiService.postValuationCommit(request) },
                postValuationCommitResult,
                isShowDialog = true,
            )
        }

        /**
         * 将 Uri 转换为 Retrofit 上传所需的 MultipartBody.Part
         * @param context Context 实例
         * @param uri 文件内容的 Uri
         * @param partName 表单字段名，应与 API 定义的字段名一致（此处为 "files"）
         */
        fun prepareFilePart(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/*"
            val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}")
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            val requestBody = tempFile.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
        }

        fun uploadPics(context: Context, picList: MutableList<ModImageUriBean>) {
            val fileParts = picList
                .filter { it.files != null }
                .mapNotNull {
                    prepareFilePart(context, it.files!!, "files")
                }

            if (fileParts.isNotEmpty()) {
                viewModelScope.launch {
                    try {
                        val response = apiService.uploadFiles(fileParts)
                    } catch (e: Exception) {
                        e.toString()
                    }
                }
            }
        }


        /**
         * 执行文件上传操作，并返回上传后的文件URL列表
         * @param context Fragment/Activity 的 Context
         * @param picList 包含待上传文件的 ModImageUriBean 列表
         */
        fun uploadCommitFiles(context: Context, picList: MutableList<ModImageUriBean>) {
            // 1. 过滤并准备 MultipartBody.Part 列表（此部分代码与上一步相同）
            val fileParts = picList
                .filter { it.files != null }
                // 假设 prepareFilePart 是可用的辅助函数
                .mapNotNull {
                    prepareFilePart(context, it.files!!, "files")
                }

            if (fileParts.isEmpty()) {
                // 如果没有文件需要上传，直接发送成功状态（空列表）
                uploadFileState.value = ModResultStateWithMsg.onAppSuccess(
                    null,
                    message = "无需上传文件"
                )
                return
            }
            modRequestWithMsg({
                apiService.uploadFiles(fileParts)
            }, uploadFileState)
        }


        var pic = IntObservableField(0)
        var gameName = StringObservableField()
        var gameNickName = StringObservableField()
        var gameServerName = StringObservableField()
        var gamePrice = StringObservableField()
        var pic1Uri = ObservableField<Uri>()
        var pic2Uri = ObservableField<Uri>()
        var pic3Uri = ObservableField<Uri>()

        fun clearData() {
            gameName.set("")
            gameNickName.set("")
            gameServerName.set("")
            gamePrice.set("")
            pic1Uri.set(null)
            pic2Uri.set(null)
            pic3Uri.set(null)
        }

        /**
         * 数据校验方法
         * @return 返回null表示校验通过，否则返回错误提示信息
         */
        fun getValidationError(): String? {
            // 使用一个“规则列表”来定义所有校验
            val validationRules = listOf(
                Pair({ gameName.get().isEmpty() }, "请填写游戏名"),
                Pair({ gameNickName.get().isEmpty() }, "请填写角色名"),
                Pair({ gameServerName.get().isEmpty() }, "请填写区服名"),
                Pair({ getCalculatedPrice(gamePrice.get()) == null }, "请填写实充金额"),
                Pair({ gamePrice.get().isEmpty() }, "请填写正确的实充金额"),
                Pair(
                    { pic1Uri.get() == null && pic2Uri.get() == null && pic3Uri.get() == null },
                    "请上传角色信息截图，至少上传1张截图"
                ),
            )
            // 遍历规则，找到第一个不满足的并返回错误信息
            for ((condition, message) in validationRules) {
                if (condition()) {
                    return message
                }
            }
            // 所有规则都通过
            return null
        }

        /**
         * 计算随机折扣价
         *
         * @param priceString 可能是整数或Double的原始价格字符串
         * @return 计算后的价格 (Double)，如果输入无效则返回 null
         */
        fun getCalculatedPrice(priceString: String?): Double? {
            val priceNumber: Double? = priceString?.toDoubleOrNull()
            if (priceNumber == null) {
                // 字符串是 null 或是无效数字 (如 "abc")
                println("无法解析价格: $priceString")
                return null
            }
            val randomMultiplier = Random.nextDouble(0.1, 0.4)
            val roundedResult = (priceNumber * randomMultiplier * 100.00).roundToInt() / 100.00
            return roundedResult
        }

    }


}


