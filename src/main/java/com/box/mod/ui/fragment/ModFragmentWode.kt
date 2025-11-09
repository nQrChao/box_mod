package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.appViewModel
import com.box.common.data.model.ModUserInfo
import com.box.common.eventViewModel
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.common.utils.CacheManager
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.R
import com.box.mod.databinding.ModFragmentWodeBinding
import com.box.mod.ui.activity.ModActivityChangePassword
import com.box.mod.ui.activity.ModActivityLogin
import com.box.mod.ui.activity.ModActivitySafety
import com.box.mod.ui.activity.ModActivityShouCang
import com.box.mod.ui.activity.fankui.ModActivityFankui1
import com.box.mod.ui.activity.message.ModActivityMessage1
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.box.com.R as RC

class ModFragmentWode : BaseTitleBarFragment<ModFragmentWode.Model, ModFragmentWodeBinding>() {
    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_wode

    companion object {
        fun newInstance(): ModFragmentWode {
            return ModFragmentWode()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }

        appViewModel.modInitBean.value.let {
            if (it != null) {
                mDataBinding.beiAnBar.setRightText(it.number)
            }
        }
        mDataBinding.huancunBar.setRightText(CacheManager.getFormattedTotalCacheSize(appContext))

    }


    override fun createObserver() {
        appViewModel.modUserInfo.observe(this) {
            mViewModel.modUserInfo.value = it
//            if (it.localAvatarResName != null) {
//                val resId = mDataBinding.userIcon.context.resources.getIdentifier(
//                    it.localAvatarResName,
//                    "drawable",
//                    mDataBinding.userIcon.context.packageName
//                )
//                if (resId != 0) {
//                    mDataBinding.userIcon.setImageResource(resId)
//                }
//            }
        }

        MMKVConfig.userInfo?.let { savedUser ->
            eventViewModel.isLogin.value = true
            appViewModel.modUserInfo.value = savedUser
        }
    }

    override fun lazyLoadData() {

    }

    override fun onNetworkStateChanged(it: NetState) {
    }

    override fun onResume() {
        super.onResume()
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun confirm() {

        }

        fun gameShoucang() {
            if (isLogin()) {
                ModActivityShouCang.start(appContext, 1)
            } else {
                Toaster.show("请先登录")
                ModActivityLogin.start(appContext)
            }
        }

        fun jueseShoucang() {
            if (isLogin()) {
                ModActivityShouCang.start(appContext)
            } else {
                Toaster.show("请先登录")
                ModActivityLogin.start(appContext)
            }
        }

        fun user() {
            if (!isLogin()) {
                ModActivityLogin.start(appContext)
            }
        }

        fun xiaoxi() {
            if (isLogin()) {
                ModActivityMessage1.start(appContext)
            } else {
                Toaster.show("请先登录")
                ModActivityLogin.start(appContext)
            }

        }

        fun huancun() {
            if (CacheManager.getTotalCacheSize(appContext) > 0) {
                XPopup.Builder(context)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(true)
                    .animationDuration(5)
                    .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                    .isLightStatusBar(true)
                    .hasNavigationBar(true)
                    .asConfirm(
                        "清理缓存", "是否清理全部缓存",
                        "取消", "确定",
                        {
                            CacheManager.clearAllCache(appContext)
                            mDataBinding.huancunBar.setRightText(
                                CacheManager.getFormattedTotalCacheSize(
                                    appContext
                                )
                            )
                        }, null, false, R.layout.xpopup_confirm_mod
                    ).show()
            } else {
                Toaster.show("")
            }
        }

        fun fankui() {
            ModActivityFankui1.start(appContext)
        }

        fun beian() {
            appViewModel.modInitBean.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.numLink)
                }
            }
        }

        fun fangChenMi() {
            appViewModel.modInitBean.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.antiAddictionUrl)
                }
            }
        }

        fun xiugaimima() {
            ModActivityChangePassword.start(appContext)
        }

        fun userAnQuan() {
            ModActivitySafety.start(appContext)
        }

        fun quit() {
            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .animationDuration(5)
                .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                .isLightStatusBar(true)
                .hasNavigationBar(true)
                .asConfirm(
                    "退出", "是否退出该账号",
                    "取消", "确定",
                    {
                        Toaster.show("账号已退出")
                        MMKVConfig.userInfo = null
                        appViewModel.modUserInfo.postValue(null)
                        eventViewModel.isLogin.value = false
                    }, null, false, RC.layout.xpopup_confirm
                ).show()


        }

    }

    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "") {
        val tuiSong = MutableLiveData<Boolean>()
        var pic = IntObservableField(0)
        val modUserInfo = MutableLiveData<ModUserInfo>()

    }


}


