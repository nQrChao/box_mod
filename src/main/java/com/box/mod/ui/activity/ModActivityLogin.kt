package com.box.mod.ui.activity

import android.annotation.SuppressLint
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.graphics.toColorInt
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.box.base.base.activity.BaseModVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appContext
import com.box.common.appViewModel
import com.box.common.data.RegisterRequest
import com.box.common.data.model.ModDataBean
import com.box.common.data.model.ModUserInfo
import com.box.common.eventViewModel
import com.box.common.network.apiService
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.common.utils.ext.logsE
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.BR.modData
import com.box.mod.R
import com.box.mod.databinding.ModActivityLoginBinding
import com.box.mod.databinding.ModItemRankShoucangBinding
import com.box.mod.ui.xpop.ModXPopupLoginBottomXieYi
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.com.R as RC

@SuppressLint("CustomSplashScreen")
class ModActivityLogin : BaseModVmDbActivity<ModActivityLogin.Model, ModActivityLoginBinding>() {

    private val contentText = "我已阅读并同意《隐私政策》、《用户服务协议》"
    private val linkTextColor = "#007BFF".toColorInt()

    private val userAgreementClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            appViewModel.modInitBean.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.userAgreementLink)
                }
            }
        }
    }

    private val privacyPolicyClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            appViewModel.modInitBean.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.privacyPolicyLink)
                }
            }
        }
    }


    override val mViewModel: Model by viewModels()
    override fun layoutId(): Int {
        return R.layout.mod_activity_login
    }

    companion object {
        var resultLauncher: ActivityResultLauncher<Intent>? = null
        fun start(context: Context) {
            val intent = Intent(context, ModActivityLogin::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
            navigationBarColor(com.box.com.R.color.white_pressed_color)
            statusBarDarkFont(true)
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
        mViewModel.loginResult.observe(this) { resultState ->
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
                    eventViewModel.isLogin.value = true
                    MMKVConfig.userInfo = data
                    appViewModel.modUserInfo.value = data
                    Toaster.show("登录成功")
                    finish()
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        mViewModel.registerResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data != null) {
                        val localAvatars = listOf(
                            "mod_user_icon1",
                            "mod_user_icon2",
                            "mod_user_icon3",
                            "mod_user_icon4",
                            "mod_user_icon5",
                            "mod_user_icon6",
                            "mod_user_icon7",
                            "mod_user_icon8"
                        )
                        data.localAvatarResName = localAvatars.random()
                    }
                    MMKVConfig.userInfo = data
                    eventViewModel.isLogin.value = true
                    appViewModel.modUserInfo.value = data
                    Toaster.show("注册成功")
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

        private val loginRegisterTransition: TransitionSet = TransitionSet().apply {
            // 设置淡入淡出
            addTransition(Fade().apply {
                duration = 150 // 淡入淡出时长
            })
            // 设置布局移动
            addTransition(ChangeBounds().apply {
                duration = 300 // 布局移动时长
            })
            // ordering = TransitionSet.ORDERING_TOGETHER // 默认就是一起播放
        }

        fun toLoginView() {
            TransitionManager.beginDelayedTransition(
                mDataBinding.contentContainer,
                loginRegisterTransition
            )
            mViewModel.isLoginView.set(true)
        }

        fun toRegisterView() {
            TransitionManager.beginDelayedTransition(
                mDataBinding.contentContainer,
                loginRegisterTransition
            )
            mViewModel.isLoginView.set(false)
        }

        fun returnImg() {
            finish()
        }

        fun forgetPwd() {

        }

        fun goRegister() {
            if (mViewModel.uName.get().isEmpty()) {
                showFieldError(mDataBinding.uname, "请输入用户名")
                return
            }
            if (mViewModel.uName.get().length < 6) {
                showFieldError(mDataBinding.uname, "用户名长度至少6位")
                return
            }
            if (mViewModel.password.get().length < 6) {
                showFieldError(mDataBinding.loginPassword, "密码长度应不少于6位")
                return
            }
            if (mViewModel.password.get().length > 16) {
                showFieldError(mDataBinding.loginPassword, "密码长度应不大于16位")
                return
            }
            if (mViewModel.password2.get().length < 6) {
                showFieldError(mDataBinding.loginPassword2, "密码长度应不少于6位")
                return
            }
            if (mViewModel.password2.get() != mViewModel.password.get()) {
                showFieldError(mDataBinding.loginPassword2, "两次密码输入不一致，请确认输入")
                return
            }
            if (mDataBinding.agreementButton.isChecked) {
                mViewModel.postUserRegister(mViewModel.uName.get(), mViewModel.password.get())
            } else {
                showXieYiTips {
                    mViewModel.postUserRegister(mViewModel.uName.get(), mViewModel.password.get())
                    mDataBinding.agreementButton.isChecked = true
                }
            }
        }

        fun login() {
            if (mViewModel.uName.get().isEmpty()) {
                showFieldError(mDataBinding.uname, "请输入用户名")
                return
            }
            if (mViewModel.uName.get().length < 6) {
                showFieldError(mDataBinding.uname, "用户名长度至少6位")
                return
            }
            if (mViewModel.password.get().length < 6) {
                showFieldError(mDataBinding.loginPassword, "密码长度应不少于6位")
                return
            }
            if (mViewModel.password.get().length > 16) {
                showFieldError(mDataBinding.loginPassword, "密码长度应不大于16位")
                return
            }
            if (mDataBinding.agreementButton.isChecked) {
                mViewModel.postUserLogin(mViewModel.uName.get(), mViewModel.password.get())
            } else {
                showXieYiTips {
                    mViewModel.postUserLogin(mViewModel.uName.get(), mViewModel.password.get())
                    mDataBinding.agreementButton.isChecked = true
                }
            }
        }

        fun testClick() {

        }

        var agreementButtonChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            mDataBinding.agreementButton.isChecked = isChecked
        }

    }

    fun showXieYiTips(sure: (() -> Unit)?) {
        XPopup.Builder(this@ModActivityLogin)
            .isDestroyOnDismiss(true)
            .hasStatusBar(true)
            .isLightStatusBar(true)
            .autoFocusEditText(false)
            .autoOpenSoftInput(false)
            .navigationBarColor(ColorUtils.getColor(com.box.com.R.color.white))
            .hasNavigationBar(true)
            .asCustom(
                ModXPopupLoginBottomXieYi(this@ModActivityLogin, {

                }, {
                    sure?.invoke()
                })
            )
            .show()
    }

    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "　　　", titleLine = false) {
        var pName = StringObservableField(AppUtils.getAppName())
        var uName = StringObservableField("")
        var password = StringObservableField("")
        var password2 = StringObservableField("")
        var hasData = BooleanObservableField(false)
        var isSelect = IntObservableField(0)
        var isLoginView = BooleanObservableField(true)

        var loginResult = MutableLiveData<ModResultStateWithMsg<ModUserInfo>>()
        var registerResult = MutableLiveData<ModResultStateWithMsg<ModUserInfo>>()

        fun postUserLogin(userNameInput: String, passwordInput: String) {
            val requestBody = RegisterRequest(
                userName = userNameInput,
                password = passwordInput
            )
            modRequestWithMsg(
                { apiService.postUserLogin(requestBody) },
                loginResult,
                isShowDialog = true,
            )
        }

        fun postUserRegister(userNameInput: String, passwordInput: String) {
            val requestBody = RegisterRequest(
                userName = userNameInput,
                password = passwordInput
            )
            modRequestWithMsg(
                { apiService.postUserRegister(requestBody) },
                registerResult,
                isShowDialog = true,
            )
        }


    }


    class ModGameRankShoucangAdapter :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemRankShoucangBinding>>(
            R.layout.mod_item_rank_shoucang
        ) {

        override fun convert(
            holder: BaseDataBindingHolder<ModItemRankShoucangBinding>,
            item: ModDataBean
        ) {
            // 绑定逻辑保持不变
            holder.dataBinding?.let {
                it.setVariable(modData, item)
                it.executePendingBindings()
            }

        }

    }


}