package com.box.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.network.NetState
import com.box.mod.R
import com.box.mod.databinding.ModActivityJiaoyiMessageBinding
import com.box.com.R as RC
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.immersionbar.immersionBar

class ModActivityMessage : BaseVmDbActivity<ModActivityMessageModel, ModActivityJiaoyiMessageBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_message

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityMessage::class.java)
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

    }

    override fun createObserver() {

    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun exitPreview() {
            //退出预览
            finish()
        }
    }


}