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
import androidx.databinding.ObservableField
import androidx.fragment.app.viewModels
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.network.NetState
import com.box.common.MMKVConfig
import com.box.common.appContext
import com.box.mod.R
import com.box.mod.databinding.ModFragmentGujiaBinding
import com.box.mod.view.xpop.ModXPopupCenterPermissions
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.box.com.R as RC


class ModFragmentGuJia : BaseTitleBarFragment<ModFragmentGuJia.Model, ModFragmentGujiaBinding>() {
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

    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_gujia

    companion object {
        fun newInstance(): ModFragmentGuJia {
            return ModFragmentGuJia()
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
        if(!MMKVConfig.permissionsAlbum){
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
                        MMKVConfig.permissionsAlbum = true
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

    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "游戏账号估值") {
        var pic = IntObservableField(0)
        var gameName  = StringObservableField()
        var gameNickName  = StringObservableField()
        var gameServerName  = StringObservableField()
        var gamePrice  = StringObservableField()
        var pic1Uri  = ObservableField<Uri>()
        var pic2Uri  = ObservableField<Uri>()
        var pic3Uri  = ObservableField<Uri>()

        fun clearData() {
            gameName.set("")
            gameNickName.set("")
            gameServerName.set("")
            gamePrice.set("")
            pic1Uri.set(null)
            pic2Uri.set(null)
            pic3Uri.set(null)
        }

        /**
         * 数据校验方法
         * @return 返回null表示校验通过，否则返回错误提示信息
         */
        fun getValidationError(): String? {
            // 使用一个“规则列表”来定义所有校验
            val validationRules = listOf(
                Pair( { gameName.get().isEmpty() }, "请填写游戏名" ),
                Pair( { gameNickName.get().isEmpty() }, "请填写角色名" ),
                Pair( { gameServerName.get().isEmpty() }, "请填写区服名" ),
                Pair( { gamePrice.get().isEmpty() }, "请填写实充金额" ),
                Pair( { pic1Uri.get() == null && pic2Uri.get() == null && pic3Uri.get() == null}, "请上传角色信息截图，至少上传1张截图" ),
            )
            // 遍历规则，找到第一个不满足的并返回错误信息
            for ((condition, message) in validationRules) {
                if (condition()) {
                    return message
                }
            }
            // 所有规则都通过
            return null
        }

    }


}


