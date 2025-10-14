package com.chaoji.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.network.NetState
import com.chaoji.im.glide.GlideApp
import com.chaoji.im.sdk.appViewModel
import com.chaoji.mod.R
import com.chaoji.common.R as RC
import com.chaoji.mod.databinding.ModActivityPreviewBinding
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.immersionbar.immersionBar

class ModActivityPreview : BaseVmDbActivity<ModActivityPreviewModel, ModActivityPreviewBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_preview

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityPreview::class.java)
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
        appViewModel.appInfo.value.let {
            if (it != null) {
                GlideApp.with(this@ModActivityPreview)
                    .load(it.pic)
                    .transform(RoundedCorners(15))
                    .error(RC.drawable.status_error_ic)
                    .into(mDataBinding.modPreviewPic)
            }
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