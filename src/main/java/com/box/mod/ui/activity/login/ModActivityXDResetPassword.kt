package com.box.mod.ui.activity.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.mod.R
import com.box.mod.databinding.ModActivityLoginResetPasswordXdBinding
import com.box.com.R as RC
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar

class ModActivityXDResetPassword : BaseVmDbActivity<ModActivityXDResetPasswordModel, ModActivityLoginResetPasswordXdBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_login_reset_password_xd

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityXDResetPassword::class.java)
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
        mViewModel.postResetPasswordResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    Toaster.show("密码修改成功")
                    finish()
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }
        mViewModel.postGetResetPasswordVerificationCodeResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    Toaster.show("验证码发送成功")
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }
    }

    override fun onRightClick(view: TitleBar) {
        super.onRightClick(view)
    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun confirm() {
            if (mDataBinding.loginName.text.isNullOrEmpty()) {
                mDataBinding.loginName.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("请输入手机号码")
                return
            }
            if (mViewModel.verificationCode.get().isEmpty()) {
                mDataBinding.loginVerification.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("请输入验证码")
                return
            }
            if (mViewModel.password.get().length < 6) {
                mDataBinding.loginPassword.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("密码长度应不少于6位数")
                return
            }
            if (mViewModel.password.get().length > 18) {
                mDataBinding.loginPassword.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("密码长度应不大于18位数")
                return
            }

            mViewModel.postResetPassword()

        }


        fun customerService() {
            Toaster.show("跳转人工界面")
        }


        fun getVerificationCode() {
            if (mViewModel.mobileNum.get().length == 11) {
                mViewModel.postGetResetPasswordVerificationCode()
                mDataBinding.cvVerificationCountdown.start()
            } else {
                Toaster.show("请输入正确的手机号码")
            }
        }
    }




}