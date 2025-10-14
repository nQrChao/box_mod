package com.chaoji.mod.ui.activity.login

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.chaoji.base.base.action.ClickAction
import com.chaoji.base.base.action.KeyboardAction
import com.chaoji.im.appContext
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.mod.R
import com.chaoji.other.xpopup.core.BottomPopupView

@SuppressLint("ViewConstructor")
class ModXPopupLoginBottomXieYi(context: Context, private var cancel: (() -> Unit)?, private var sure: ((tips: String) -> Unit)?) :
    BottomPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_jiaoyi_bottom_xieyi

    private lateinit var content: TextView
    private lateinit var cancelBtn: Button
    private lateinit var confirmBtn: Button
    private val contentText = "进入下一步前，请先阅读并同意《用户服务协议》、《隐私政策》"
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
    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        content = findViewById(R.id.content)!!
        cancelBtn = findViewById(R.id.cancel_button)!!
        confirmBtn = findViewById(R.id.confirm_button)!!

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
        content.text = spannableString
        content.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        content.highlightColor = Color.TRANSPARENT

        setOnClickListener(R.id.cancel, R.id.cancel_button, R.id.confirm_button)

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cancel_button -> {
                cancel?.invoke()
                dismiss()
            }

            R.id.confirm_button -> {
                sure?.invoke("")
                dismiss()
            }

            R.id.cancel -> {
                cancel?.invoke()
                dismiss()
            }

        }
    }

    override fun dismiss() {
        super.dismiss()
        hideKeyboard(this)
    }


}