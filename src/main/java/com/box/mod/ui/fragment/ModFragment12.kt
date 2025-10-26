package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.network.NetState
import com.box.common.MMKVConfig
import com.box.common.appContext
import com.box.mod.R
import com.box.mod.databinding.ModFragment12Binding
import com.box.mod.view.xpop.ModXPopupCenterPermissions
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.box.com.R as RC


class ModFragment12 : BaseTitleBarFragment<ModFragment12Model, ModFragment12Binding>() {
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                when (mViewModel.pic.get()) {
                    1 -> {mViewModel.pic1Uri.set(uri)}
                    2 -> {mViewModel.pic2Uri.set(uri)}
                    3 -> {mViewModel.pic3Uri.set(uri)}
                }
            } else {
                Toaster.show("未选择任何图片")
            }
        }

    override val mViewModel: ModFragment12Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_12

    companion object {
        fun newInstance(): ModFragment12 {
            return ModFragment12()
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

    override fun isStatusBarEnabled(): Boolean {
        return false
    }

    override fun getTitleBar(): TitleBar? {
       return mDataBinding.titleBar
    }





    override fun createObserver() {

    }

    override fun lazyLoadData() {

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

            val errorMessage = mViewModel.getValidationError()
            if (errorMessage != null) {
                if (mViewModel.gameName.get().isEmpty()) {
                    mDataBinding.gameNameEdit.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                } else if (mViewModel.gameNickName.get().isEmpty()) {
                    mDataBinding.gameNickNameEdit.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                }  else if (mViewModel.gameServerName.get().isEmpty()) {
                    mDataBinding.gameServerEdit.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                } else if (mViewModel.gamePrice.get().isEmpty()) {
                    mDataBinding.gamePriceEdit.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                } else if (mViewModel.pic1Uri.get() == null && mViewModel.pic2Uri.get() == null && mViewModel.pic3Uri.get() == null) {
                    mDataBinding.gameAddPicLayout.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                }

                Toaster.show(errorMessage)
                return
            }
            mViewModel.clearData()



        }

    }

    private fun selectPhoto(pic: Int) {
        if(!MMKVConfig.picPermissions){
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
                        MMKVConfig.picPermissions = true
                        mViewModel.pic.set(pic)
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {

                    })
                .show()
        }else{
            mViewModel.pic.set(pic)
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


    }


}


