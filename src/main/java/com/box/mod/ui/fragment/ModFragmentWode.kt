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
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.R
import com.box.mod.databinding.ModFragmentWodeBinding
import com.box.mod.ui.activity.ModActivityLogin
import com.box.mod.ui.activity.ModActivitySafety
import com.box.mod.ui.activity.ModActivityShouCang
import com.box.mod.ui.activity.fankui.ModActivityFankui1
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
            statusBarDarkFont(true)
            init()
        }

    }


    override fun createObserver() {

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
                ModActivityShouCang.start(appContext,1)
            }
        }
        fun jueseShoucang() {
            if (isLogin()) {
                ModActivityShouCang.start(appContext)
            }
        }

        fun user() {
            if (!isLogin()) {
                ModActivityLogin.start(appContext)
            }
        }
        fun xiaoxi() {

        }

        fun fankui() {
            ModActivityFankui1.start(appContext)
        }
        fun fangChenMi() {

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
                        mViewModel.modUserInfo.postValue(null)
                        appViewModel.modUserInfo.postValue(null)
                        eventViewModel.isLogin.value = false
                    }, null, false, RC.layout.xpopup_confirm
                ).show()

        }

    }

    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "个人中心") {
        val tuiSong = MutableLiveData<Boolean>()
        var pic = IntObservableField(0)
        var modUserInfo = MutableLiveData<ModUserInfo>()

    }


}


