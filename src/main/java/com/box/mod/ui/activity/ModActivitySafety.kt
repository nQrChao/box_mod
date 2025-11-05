package com.box.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.appViewModel
import com.box.common.eventViewModel
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.mod.R
import com.box.mod.databinding.ModActivitySafetyBinding
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.immersionbar.immersionBar
import com.box.com.R as RC

class ModActivitySafety : BaseVmDbActivity<ModActivitySafety.Model, ModActivitySafetyBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_safety

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivitySafety::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            navigationBarColor(RC.color.white)
            init()
        }

        mViewModel.isLogin.set(eventViewModel.isLogin.value ?: false)

    }

    override fun createObserver() {


    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun uZx() {
            ModActivityLogout.start(this@ModActivitySafety)
        }

        fun uXy() {
            appViewModel.modInfoBean.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.userAgreementLink)
                }
            }
        }

        fun yXy() {
            appViewModel.modInfoBean.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.privacyPolicyLink)
                }
            }
        }


    }

    class Model : BaseViewModel(title = "隐私权限安全") {
        var isLogin = BooleanObservableField(false)


    }


}