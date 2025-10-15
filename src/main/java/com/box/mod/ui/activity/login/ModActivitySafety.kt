package com.box.mod.ui.activity.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.sdk.appViewModel
import com.box.common.sdk.eventViewModel
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.mod.R
import com.box.mod.databinding.ModActivityLoginSafetyBinding
import com.box.com.R as RC
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.immersionbar.immersionBar

class ModActivitySafety : BaseVmDbActivity<ModActivitySafetyModel, ModActivityLoginSafetyBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_login_safety

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
            //ModGameBrowserManager.provider.userLogout(this@ModActivitySafety)
            ModActivityLogout.start(this@ModActivitySafety)
        }

        fun uXy() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.marketjson.xieyitanchuang_url_fuwu)
                }
            }
        }

        fun yXy() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.marketjson.xieyitanchuang_url_yinsi)
                }
            }
        }


    }


}