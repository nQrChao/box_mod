package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.ImageView
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.angcyo.dsladapter.inflate
import com.box.base.base.AppScope
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appViewModel
import com.box.common.data.model.ModDataBean
import com.box.common.data.model.ModValuationCommitBean
import com.box.common.network.apiService
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.utils.ext.logsE
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.BR.modData
import com.box.mod.R
import com.box.mod.databinding.ModFragmentGujiaRealBinding
import com.box.mod.databinding.ModItemCustomFormBinding
import com.box.mod.databinding.ModItemGujiaGameBinding
import com.box.mod.ui.adapter.ModCustomViewAdapter
import com.box.mod.view.xpop.ModXPopupCenterPermissions
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random
import com.box.com.R as RC


class ModFragmentGuJiaReal : BaseTitleBarFragment<ModFragmentGuJiaReal.Model, ModFragmentGujiaRealBinding>() {
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                when (mViewModel.pic.get()) {
                    1 -> {
                        mViewModel.pic1Uri.set(uri)
                    }

                    2 -> {
                        mViewModel.pic2Uri.set(uri)
                    }

                    3 -> {
                        mViewModel.pic3Uri.set(uri)
                    }
                }
            } else {
                Toaster.show("未选择任何图片")
            }
        }

    private val gameListAdapter = ModGameListAdapter()
    private val commitViewAdapter = ModCustomViewAdapter()

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
        /**********************************************************************************/
        mDataBinding.tabLayout.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            mViewModel.clickGameItem.value = mViewModel.gameList.value?.get(toIndex) ?: ModDataBean()
            mViewModel.getCustomFromData(mViewModel.clickGameItem.value?.id.toString())
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
                    fromView?.findViewById<CardView>(R.id.card_view_root)?.setCardBackgroundColor("#FFFFFF".toColorInt())
                    toView.findViewById<CardView>(R.id.card_view_root)?.setCardBackgroundColor("#66C5FF".toColorInt())
                    fromView?.findViewById<LinearLayout>(R.id.textRootView)?.background = (resources.getDrawable(R.drawable.gujia_item_bg, null))
                    toView.findViewById<LinearLayout>(R.id.textRootView)?.background = (resources.getDrawable(R.drawable.mod_gujia_item_selector, null))
                    fromView?.findViewById<TextView>(R.id.name)?.setTextColor("#253E75".toColorInt())
                    toView.findViewById<TextView>(R.id.name)?.setTextColor("#FFFFFF".toColorInt())
                }
            }
        }

        gameListAdapter.setOnItemClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            mViewModel.getCustomFromData(clickedItem.id.toString())
        }
        /**********************************************************************************/


        mDataBinding.recyclerView2.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = commitViewAdapter
        }

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
                    mHandler.sendEmptyMessage(0)
                    //gameListAdapter.setList(data)
                    //gameListAdapter.addData(data!!)
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

            mDataBinding.tabLayout.setCurrentItem(0,true)
            mViewModel.getCustomFromData(it[0].id.toString())
        }


        appViewModel.modUserInfo.observe(this) {
            mViewModel.getGameListData()
        }
    }

    override fun lazyLoadData() {
        mViewModel.getGameListData()
    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun addPic1() {
            selectPhoto(1)
        }

        fun addPic2() {
            selectPhoto(2)
        }

        fun addPic3() {
            selectPhoto(3)
        }

        fun confirm() {
            val formItemsList = commitViewAdapter.data
            // 遍历列表，收集或校验数据
            val commitDataMap = mutableMapOf<String, String>()
            var validationError: String? = null
            for (item in formItemsList) {
                // item.content包含用户输入的最新值
                val value = item.content
                // 非空校验
                if (value.isBlank()) {
                    validationError = "${item.title} 不能为空" // 使用 item.title 作为提示
                    break // 发现第一个错误即停止
                }
                // 将字段标题和输入值存入 Map，用于后续提交
                commitDataMap[item.title] = value
            }
            if (validationError != null) {
                // 如果有错误，显示提示信息
                Toaster.show(validationError)
                return
            }
            Toaster.show("数据收集成功，准备提交：$commitDataMap")
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
    class ModGameListAdapter : BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemGujiaGameBinding>>(
        R.layout.mod_item_gujia_game
    ) {

        override fun convert(holder: BaseDataBindingHolder<ModItemGujiaGameBinding>, item: ModDataBean) {
            holder.dataBinding?.let {
                it.setVariable(modData, item)
                it.executePendingBindings()
            }

        }

    }

    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "游戏账号估值") {
        var clickGameItem = MutableLiveData<ModDataBean>()
        var gameList = MutableLiveData<MutableList<ModDataBean>>()
        var gameListResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        var customFromResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModValuationCommitBean>>>()
        fun getGameListData() {
            modRequestWithMsg({
                apiService.getValuationCommitGameList()
            }, gameListResult)
        }

        fun getCustomFromData(id: String) {
            modRequestWithMsg({
                apiService.getValuationCommitGameFrom(id)
            }, customFromResult)
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


