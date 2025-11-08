package com.box.mod.ui.activity.fankui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.ObservableField
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.R
import com.box.mod.databinding.ModActivityFankui1Binding
import com.box.mod.view.xpop.ModXPopupCenterPermissions
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.box.com.R as RC

class ModActivityFankui1 : BaseVmDbActivity<ModActivityFankui1.Model, ModActivityFankui1Binding>() {
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                when (mViewModel.pic.get()) {
                    1 -> {mViewModel.picUri.set(uri)}
                }
            } else {
                Toaster.show("未选择任何图片")
            }
        }

    override fun layoutId(): Int = R.layout.mod_activity_fankui_1

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityFankui1::class.java)
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
            titleBar(mDataBinding.titleBar)
            navigationBarColor(R.color.mod_fankui1_nav_color)
            init()
        }
        mDataBinding.titleBar.leftView.setOnClickListener {
            finish()
        }

    }

    override fun createObserver() {


    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    override fun onLeftClick(view: TitleBar) {
        super.onLeftClick(view)
        finish()
    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun addPic() {
            selectPhoto(1)
        }

        fun confirm() {
            if (mViewModel.questionText.get().length < 15) {
                mDataBinding.modQuestionEdit.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("亲，能否说的再详细一点呢(大于15个字)")
            } else {
                if (StringUtils.isEmpty(mViewModel.qqText.get())) {
                    mDataBinding.modQQEdit.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    Toaster.show("请输入您的QQ号码")
                } else {
                    mViewModel.qqText.set("")
                    mViewModel.questionText.set("")
                    Toaster.show("提交成功，感谢您的建议")
                    finish()
                }
            }
        }


    }


    private fun selectPhoto(picIndex: Int) {
        if(!MMKVConfig.permissionsAlbum){
            XPopup.Builder(this@ModActivityFankui1)
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .isLightStatusBar(true)
                .animationDuration(5)
                .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                .hasNavigationBar(true)
                .asCustom(
                    ModXPopupCenterPermissions(this@ModActivityFankui1, "相册", "用于实现图片选择功能", {
                        MMKVConfig.permissionsAlbum = true
                        mViewModel.pic.set(picIndex)
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {

                    })
                .show()
        }else{
            mViewModel.pic.set(picIndex)
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

    }

    class Model : BaseViewModel(title = "") {
        var pic = IntObservableField(0)
        var isLogin = BooleanObservableField(false)
        var picUri  = ObservableField<Uri>()
        var version = "VER:" + AppUtils.getAppVersionName()
        var questionText = StringObservableField("")
        var qqText = StringObservableField("")


    }


}