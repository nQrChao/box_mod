package com.box.mod.ui.fragment

import android.net.Uri
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.sdk.ImSDK
import com.box.common.sdk.appViewModel
import com.box.common.sdk.eventViewModel
import com.box.common.utils.MMKVUtil
import com.box.mod.R
import com.box.mod.databinding.MainFragment2Binding
import com.box.mod.ui.activity.jiaoyi.ModActivityJiaoYiShiMing
import com.box.mod.ui.activity.login.ModActivityXDLogin
import com.box.mod.ui.xpop.ModXPopupClientType
import com.box.mod.view.xpop.ModXPopupCenterPermissions
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.box.common.R as RC


class MainFragment2 : BaseTitleBarFragment<MainFragment2Model, MainFragment2Binding>() {
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

    override fun layoutId(): Int = R.layout.main_fragment_2

    companion object {
        fun newInstance(): MainFragment2 {
            return MainFragment2()
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


    }

    override fun initData() {

    }

    override fun createObserver() {
        eventViewModel.clientType.observe(this) {
            when (it) {
                "view1" -> {
                    mViewModel.clientType.set("Android")
                }

                "view2" -> {
                    mViewModel.clientType.set("IOS")
                }
            }
        }
        eventViewModel.productType.observe(this) {
            when (it) {
                "view1" -> {
                    mViewModel.productType.set("账号")
                }

                "view2" -> {
                    mViewModel.productType.set("游戏币")
                }

                "view3" -> {
                    mViewModel.productType.set("道具")
                }
            }
        }
    }

    override fun lazyLoadData() {

    }

    override fun onNetworkStateChanged(it: NetState) {
    }

    private fun selectPhoto(pic: Int) {
        if(StringUtils.isEmpty(MMKVUtil.getPicPer())){
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
                        MMKVUtil.savePicPer("PicPer")
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

    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun confirm() {
            if (isLogin()) {
                if(appViewModel.modUserRealName.value?.isRealName() == false){
                    Toaster.show("未实名认证，请先进行实名认证")
                    ActivityUtils.startActivity(ModActivityJiaoYiShiMing::class.java)
                    return
                }
                if(appViewModel.modUserRealName.value?.isRealName18() == false){
                    Toaster.show("未成年用户不允许发布商品")
                    return
                }
                val errorMessage = mViewModel.getValidationError()
                if (errorMessage != null) {
                    if (mViewModel.gameName.get().isNullOrEmpty()) {
                        mDataBinding.fabuGameName.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    } else if (mViewModel.platformName.get().isNullOrEmpty()) {
                        mDataBinding.fabuPlatformName.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    } else if (mViewModel.accountName.get().isNullOrEmpty()) {
                        mDataBinding.fabuAccountName.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    } else if (mViewModel.titleName.get().isNullOrEmpty()) {
                        mDataBinding.fabuTitleName.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    } else if (mViewModel.productIntro.get().isNullOrEmpty()) {
                        mDataBinding.fabuProductIntro.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    } else if (mViewModel.pic1Uri.get() == null) {
                        mDataBinding.pic1.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    } else if (mViewModel.pic2Uri.get() == null) {
                        mDataBinding.pic2.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    } else if (mViewModel.pic3Uri.get() == null) {
                        mDataBinding.pic3.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    } else if (mViewModel.price.get().isNullOrEmpty()) {
                        mDataBinding.fabuPrice.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    }

                    Toaster.show(errorMessage)
                    return
                }
                mViewModel.clear()

                XPopup.Builder(context)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(true)
                    .animationDuration(5)
                    .navigationBarColor(ColorUtils.getColor(com.box.common.R.color.xpop_shadow_color))
                    .isLightStatusBar(true)
                    .hasNavigationBar(true)
                    .asConfirm(
                        "提交成功", "您的商品已成功提交，审核通过后自动上架。",
                        "取消", "我知道了",
                        {
                            //返回首页
                            ImSDK.eventViewModelInstance.setMainCurrentItem.value = 0
                        }, null, true, com.box.common.R.layout.xpopup_confirm
                    ).show()
            } else {
                Toaster.show("您还未登录，请先登录")
                ModActivityXDLogin.start(appContext)
            }

        }

        fun pic1() {
            selectPhoto(1)
        }

        fun pic2() {
            selectPhoto(2)
        }

        fun pic3() {
            selectPhoto(3)
        }

        fun clientType(view: View) {
            XPopup.Builder(context)
                .hasShadowBg(false)
                .hasStatusBar(true)
                .isLightStatusBar(true)
                .hasNavigationBar(true)
                .isDestroyOnDismiss(false)
                .atView(view)
                .asCustom(context?.let { ModXPopupClientType(it) })
                .show()
        }

        fun productType(view: View) {
//            XPopup.Builder(context)
//                .hasShadowBg(false)
//                .hasStatusBar(true)
//                .isLightStatusBar(true)
//                .hasNavigationBar(true)
//                .isDestroyOnDismiss(false)
//                .atView(view)
//                .asCustom(context?.let { ModXPopupProductType(it) })
//                .show()
        }

    }



}


