package com.box.mod.ui.xpop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.box.other.blankj.utilcode.util.IntentUtils
import com.box.base.base.action.ClickAction
import com.box.base.base.action.KeyboardAction
import com.box.common.appContext
import com.box.common.countClick
import com.box.common.sdk.appViewModel
import com.box.common.data.model.ProtocolInit
import com.box.common.sdk.ImSDK
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.mod.R
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.hjq.toast.Toaster
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.box.other.xpopup.core.CenterPopupView
import java.io.File

@SuppressLint("ViewConstructor")
class ModXPopupCenterTip(
    context: Context,
    var xyInit: ProtocolInit,
    private var liulan: (() -> Unit)?,
    private var chakan: (() -> Unit)?
) :
    CenterPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_protocol_tip

    private var titleView: TextView? = null
    private var cancelView: TextView? = null
    private var contentTextView: TextView? = null
    private var confirmView: TextView? = null
    private var contentText =
        "如果您不同意《隐私政策》协议，将进入浏览模式，此模式下我们不会收集您的信息，只提供部分基础内容的浏览功能。"
    private val linkTextColor = Color.parseColor("#007BFF") // 设置链接颜色，这里使用蓝色，可以替换为其他颜色

    private val privacyPolicyClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.marketjson.xieyitanchuang_url_yinsi)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        titleView = findViewById<TextView>(R.id.tv_title)
        contentTextView = findViewById<TextView>(R.id.tv_content)
        cancelView = findViewById<TextView>(R.id.tv_cancel)
        confirmView = findViewById<TextView>(R.id.tv_confirm)
        titleView?.text = xyInit.marketjson.xieyitanchuang_biaoti


        // 使用HtmlCompat.fromHtml处理HTML标记，同时为了更好的兼容性
        val spannableString = SpannableString(
            HtmlCompat.fromHtml(contentText.replace("\n", "<br>"), HtmlCompat.FROM_HTML_MODE_LEGACY)
        )

        val startIndex = contentText.indexOf("《隐私政策》")
        val endIndex = startIndex + "《隐私政策》".length
        if (startIndex >= 0) {
            spannableString.setSpan(privacyPolicyClickableSpan, startIndex, endIndex, 0)
            spannableString.setSpan(ForegroundColorSpan(linkTextColor), startIndex, endIndex, 0)
        }
        contentTextView?.text = spannableString
        contentTextView?.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        contentTextView?.highlightColor = Color.TRANSPARENT
        setOnClickListener(R.id.tv_chakan, R.id.tv_liulan, R.id.tv_close, R.id.tv_content,R.id.tv_title)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_chakan -> {
                chakan?.invoke()
                dismiss()
            }

            R.id.tv_liulan -> {
                liulan?.invoke()
                dismiss()
            }

            R.id.tv_close -> {
                AppUtils.exitApp()
            }

            R.id.tv_content -> {
                countClick {
                    ImSDK.eventViewModelInstance.startMJ.postValue(true)
                }
            }
            R.id.tv_title -> {
                countClick {
                    ImSDK.eventViewModelInstance.startMJ.postValue(false)
                }
            }
        }
    }


    /**
     * 安装 Apk
     */
    private fun installApk(file: File) {
        XXPermissions.with(context).permission(Permission.REQUEST_INSTALL_PACKAGES)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    context.startActivity(IntentUtils.getInstallAppIntent(file))
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    if (never) {
                        Toaster.show("被永久拒绝授权，请手动授予权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(context, permissions)
                    } else {
                        Toaster.show("获取权限失败，请手动授予权限")
                    }
                }
            })
    }

    override fun dismiss() {
        super.dismiss()
        hideKeyboard(this)
    }


}