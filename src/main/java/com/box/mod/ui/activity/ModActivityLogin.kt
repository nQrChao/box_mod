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
import android.widget.CompoundButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.graphics.toColorInt
import androidx.core.text.HtmlCompat
import com.box.base.base.activity.BaseModVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.appViewModel
import com.box.common.data.model.ModDataBean
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.mod.BR.modData
import com.box.mod.R
import com.box.mod.databinding.ModActivityLoginBinding
import com.box.mod.databinding.ModItemRankShoucangBinding
import com.box.mod.ui.xpop.ModXPopupLoginBottomXieYi
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

@SuppressLint("CustomSplashScreen")
class ModActivityLogin : BaseModVmDbActivity<ModActivityLogin.Model, ModActivityLoginBinding>() {

    private val contentText = "我已阅读并同意《隐私政策》、《用户服务协议》"
    private val linkTextColor = "#007BFF".toColorInt()

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


    override val mViewModel: Model by viewModels()
    override fun layoutId(): Int {
        return R.layout.mod_activity_login
    }

    companion object {
        var resultLauncher: ActivityResultLauncher<Intent>? = null
        fun start(context: Context) {
            val intent = Intent(context, ModActivityLogin::class.java)
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

    }

    override fun onNetworkStateChanged(netState: NetState) {
    }


    inner class ProxyClick {
        fun forgetPwd() {

        }

        fun login() {



            if (mDataBinding.agreementButton.isChecked) {
                //loginAction()
            } else {
                showXieYiTips {
                    //loginAction()
                    mDataBinding.agreementButton.isChecked = true
                }
            }
        }

        fun goRegister() {

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
    class Model : BaseViewModel(title = "") {
        var pName =  StringObservableField(AppUtils.getAppName())
        var uName = StringObservableField("")
        var password = StringObservableField("")
        var hasData = BooleanObservableField(false)
        var isSelect = IntObservableField(0)

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