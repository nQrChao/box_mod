package com.box.mod.ui.activity.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import androidx.core.text.HtmlCompat
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.sdk.appViewModel
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.mod.R
import com.box.mod.databinding.ModActivityLoginRegisterXdBinding
import com.box.com.R as RC
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar

class ModActivityXDRegister : BaseVmDbActivity<ModActivityXDRegisterModel, ModActivityLoginRegisterXdBinding>() {
    private val contentText = "我已阅读并同意《隐私政策》、《用户服务协议》"
    private val linkTextColor = Color.parseColor("#007BFF")

    private val userAgreementClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            appViewModel.appInfo.value?.marketjson?.xieyitanchuang_url_fuwu?.let {
                CommonActivityBrowser.start(appContext, it)
            }
        }
    }

    private val privacyPolicyClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            appViewModel.appInfo.value?.marketjson?.xieyitanchuang_url_yinsi?.let {
                CommonActivityBrowser.start(appContext, it)
            }
        }
    }

    override fun layoutId(): Int = R.layout.mod_activity_login_register_xd

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityXDRegister::class.java)
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

        // 使用HtmlCompat.fromHtml处理HTML标记，同时为了更好的兼容性
        val spannableString = SpannableString(
            HtmlCompat.fromHtml(contentText.replace("\n", "<br>"), HtmlCompat.FROM_HTML_MODE_LEGACY)
        )
        var startIndex = contentText.indexOf("《用户服务协议》")
        var endIndex = startIndex + "《用户服务协议》".length
        if (startIndex >= 0) {
            spannableString.setSpan(userAgreementClickableSpan, startIndex, endIndex, 0)
            spannableString.setSpan(ForegroundColorSpan(linkTextColor), startIndex, endIndex, 0)
        }

        startIndex = contentText.indexOf("《隐私政策》")
        endIndex = startIndex + "《隐私政策》".length
        if (startIndex >= 0) {
            spannableString.setSpan(privacyPolicyClickableSpan, startIndex, endIndex, 0)
            spannableString.setSpan(ForegroundColorSpan(linkTextColor), startIndex, endIndex, 0)
        }
        mDataBinding.tvAgree.text = spannableString
        mDataBinding.tvAgree.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        mDataBinding.tvAgree.highlightColor = Color.TRANSPARENT
    }

    override fun createObserver() {
        mViewModel.loginBeanResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }
        mViewModel.userInfoBeanResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }
        mViewModel.modUserRealName.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    Toaster.show("账号注册成功")
                    appViewModel.modUserRealName.postValue(data)
                    ActivityUtils.finishActivity(ModActivityXDLogin::class.java)
                    finish()
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
        fun login() {
            if (mDataBinding.loginName.text.isNullOrEmpty()) {
                mDataBinding.loginName.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("请输入用户名或手机号码")
                return
            }

            if (mViewModel.password1.get().length < 6 || mViewModel.password2.get().length < 6) {
                mDataBinding.password1.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("密码长度应不少于6位数")
                return
            }

            if (mViewModel.password1.get() != mViewModel.password2.get()) {
                mDataBinding.password1.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                mDataBinding.password2.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("两次密码输入不一致")
                return
            }

            if (!mDataBinding.agreementButton.isChecked) {
                mDataBinding.llAgree.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("请先阅读同意《用户协议》及《用户隐私协议》")
                return
            }

            mViewModel.postRegisterByUserName()

        }

        fun oneKeyLogin() {


        }

        fun phoneLogin() {


        }

        fun forgetPwd() {


        }

        var agreementButtonChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            mDataBinding.agreementButton.isChecked = isChecked
        }
    }


}