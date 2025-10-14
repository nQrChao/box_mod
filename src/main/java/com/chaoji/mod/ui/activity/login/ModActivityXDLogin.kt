package com.chaoji.mod.ui.activity.login

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
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModStateWithMsg
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.sdk.eventViewModel
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityLoginXdBinding
import com.chaoji.mod.game.ModManager
import com.chaoji.common.R as RC
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.blankj.utilcode.util.ResourceUtils
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.other.xpopup.XPopup

class ModActivityXDLogin : BaseVmDbActivity<ModActivityXDLoginModel, ModActivityLoginXdBinding>() {
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

    override fun layoutId(): Int = R.layout.mod_activity_login_xd

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityXDLogin::class.java)
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
        getTitleBar()?.setRightTitleColor(ResourceUtils.getColorIdByName("common_header_bg"))
        mViewModel.hasOneKeyLogin.set(ModManager.provider.hasOneKeyLogin())
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
        mViewModel.postGetVerificationCodeResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    Toaster.show("验证码发送成功")
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

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
                    Logs.e("modUserRealName:${GsonUtils.toJson(data)}")
                    appViewModel.modUserRealName.postValue(data)
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
        ModActivityXDRegister.start(appContext)
    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    fun showXieYiTips(sure: (() -> Unit)?) {
        XPopup.Builder(this@ModActivityXDLogin)
            .isDestroyOnDismiss(true)
            .hasStatusBar(true)
            .isLightStatusBar(true)
            .autoFocusEditText(false)
            .autoOpenSoftInput(false)
            .navigationBarColor(ColorUtils.getColor(com.chaoji.common.R.color.white))
            .hasNavigationBar(true)
            .asCustom(
                ModXPopupLoginBottomXieYi(this@ModActivityXDLogin, {

                }, {
                    sure?.invoke()
                })
            )
            .show()
    }

    private fun showFieldError(view: View, message: String) {
        view.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
        Toaster.show(message)
    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun login() {
            val loginAction: () -> Unit
            when (mViewModel.loginType.get()) {
                1 -> {
                    val username = mDataBinding.loginName.text
                    val password = mViewModel.password.get()
                    if (username.isNullOrEmpty()) {
                        showFieldError(mDataBinding.loginName, "请输入用户名或手机号码")
                        return
                    }
                    if (password.length < 6) {
                        showFieldError(mDataBinding.loginPassword, "密码长度应不少于6位数")
                        return
                    }
                    if (password.length > 18) {
                        showFieldError(mDataBinding.loginPassword, "密码长度应不大于18位数")
                        return
                    }
                    loginAction = { mViewModel.postNameLoginAndGetUserInfo() }
                }

                else -> {
                    val phone = mDataBinding.loginName.text
                    val code = mViewModel.verificationCode.get()
                    if (phone.isNullOrEmpty()) {
                        showFieldError(mDataBinding.loginName, "请输入手机号码")
                        return
                    }
                    if (code.isEmpty()) {
                        showFieldError(mDataBinding.loginVerification, "请输入验证码")
                        return
                    }
                    loginAction = { mViewModel.postPhoneLoginAndGetUserInfo() }
                }
            }

            if (mDataBinding.agreementButton.isChecked) {
                loginAction()
            } else {
                showXieYiTips {
                    loginAction()
                    mDataBinding.agreementButton.isChecked = true
                }
            }


        }


        fun oneKeyLogin() {


        }

        fun phoneLogin() {
            mViewModel.loginType.set(2)

        }

        fun nameLogin() {
            mViewModel.loginType.set(1)

        }

        fun forgetPwd() {
            ModActivityXDResetPassword.start(appContext)
        }

        fun getVerificationCode() {
            if (mViewModel.mobileNum.get().length == 11) {
                mViewModel.postGetVerificationCode()
                mDataBinding.cvVerificationCountdown.start()
            } else {
                mDataBinding.loginName.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("请输入正确的手机号码")
            }
        }

        fun bindPassword() {
            popBindPassword()
        }

        var agreementButtonChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            mDataBinding.agreementButton.isChecked = isChecked
        }


    }


    fun popBindPassword() {
        XPopup.Builder(this@ModActivityXDLogin)
            .isDestroyOnDismiss(true)
            .hasStatusBar(true)
            .isLightStatusBar(true)
            .autoFocusEditText(false)
            .autoOpenSoftInput(false)
            .navigationBarColor(ColorUtils.getColor(com.chaoji.common.R.color.white))
            .hasNavigationBar(true)
            .asCustom(
                ModXPopupLoginBottomBindPassword(this@ModActivityXDLogin, {
                    //取消
                }, {
                    //确定
                })
            )
            .show()
    }


}