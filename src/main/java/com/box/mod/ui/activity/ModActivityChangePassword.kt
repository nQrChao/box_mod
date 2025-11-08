package com.box.mod.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import com.box.base.base.activity.BaseModVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appContext
import com.box.common.data.ChangePasswordRequest
import com.box.common.data.model.ModUserInfo
import com.box.common.network.apiService
import com.box.common.utils.ext.logsE
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.R
import com.box.mod.databinding.ModActivityChangePasswordBinding
import com.box.mod.ui.xpop.ModXPopupLoginBottomXieYi
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.box.com.R as RC

@SuppressLint("CustomSplashScreen")
class ModActivityChangePassword : BaseModVmDbActivity<ModActivityChangePassword.Model, ModActivityChangePasswordBinding>() {


    override val mViewModel: Model by viewModels()
    override fun layoutId(): Int {
        return R.layout.mod_activity_change_password
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityChangePassword::class.java)
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
            navigationBarColor(RC.color.white_pressed_color)
            statusBarDarkFont(true)
            init()
        }


        MMKVConfig.userInfo?.let { it ->
            mViewModel.modUserInfo.value = it
        }

    }

    override fun createObserver() {
        mViewModel.changePasswordResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data != null) {
                        val oldUserInfo: ModUserInfo? = MMKVConfig.userInfo
                        val hasRemoteAvatar = data.avatar.isNotEmpty()
                        if (!hasRemoteAvatar && oldUserInfo?.localAvatarResName != null) {
                            data.localAvatarResName = oldUserInfo.localAvatarResName
                        }
                    }
                    Toaster.show("修改成功")
                    finish()
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }


    }

    override fun onNetworkStateChanged(netState: NetState) {
    }


    private fun showFieldError(view: View, message: String) {
        view.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
        Toaster.show(message)
    }

    inner class ProxyClick {

        fun changePassword() {
            if (mViewModel.oldPassword.get().isEmpty()) {
                showFieldError(mDataBinding.oldPassword, "请输入旧密码")
                return
            }
            if (mViewModel.newPassword.get().isEmpty()) {
                showFieldError(mDataBinding.newPassword, "请输入新密码")
                return
            }
            if (mViewModel.newPassword2.get().isEmpty()) {
                showFieldError(mDataBinding.newPassword2, "请再次输入新密码")
                return
            }

            if (mViewModel.newPassword2.get() != mViewModel.newPassword.get()) {
                showFieldError(mDataBinding.newPassword2, "两次密码输入不一致，请确认输入")
                return
            }

            mViewModel.postChangePassword(mViewModel.oldPassword.get(),mViewModel.newPassword.get())

        }



    }

    fun showXieYiTips(sure: (() -> Unit)?) {
        XPopup.Builder(this@ModActivityChangePassword)
            .isDestroyOnDismiss(true)
            .hasStatusBar(true)
            .isLightStatusBar(true)
            .autoFocusEditText(false)
            .autoOpenSoftInput(false)
            .navigationBarColor(ColorUtils.getColor(RC.color.white))
            .hasNavigationBar(true)
            .asCustom(
                ModXPopupLoginBottomXieYi(this@ModActivityChangePassword, {

                }, {
                    sure?.invoke()
                })
            )
            .show()
    }

    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "修改密码", titleLine = false) {
        val modUserInfo = MutableLiveData<ModUserInfo>()
        var oldPassword = StringObservableField("")
        var newPassword = StringObservableField("")
        var newPassword2 = StringObservableField("")
        var changePasswordResult = MutableLiveData<ModResultStateWithMsg<ModUserInfo>>()

        fun postChangePassword(oldPwd: String, newPwd: String) {
            val requestBody = ChangePasswordRequest(
                newPwd = newPwd,
                oldPwd = oldPwd
            )
            modRequestWithMsg(
                { apiService.changePassword(requestBody) },
                changePasswordResult,
                isShowDialog = true,
            )
        }


    }

}