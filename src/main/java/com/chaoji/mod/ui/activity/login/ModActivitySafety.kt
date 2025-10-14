package com.chaoji.mod.ui.activity.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.sdk.eventViewModel
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityLoginSafetyBinding
import com.chaoji.common.R as RC
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.immersionbar.immersionBar

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