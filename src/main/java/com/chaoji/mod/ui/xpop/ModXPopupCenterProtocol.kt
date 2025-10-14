package com.chaoji.mod.ui.xpop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewModelScope
import com.chaoji.other.blankj.utilcode.util.IntentUtils
import com.chaoji.base.base.action.ClickAction
import com.chaoji.base.base.action.KeyboardAction
import com.chaoji.im.appContext
import com.chaoji.im.countClick
import com.chaoji.im.data.model.AppletsData
import com.chaoji.im.getDetailedInformation
import com.chaoji.im.sdk.ImSDK
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.mod.R
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.hjq.toast.Toaster
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.chaoji.other.xpopup.core.CenterPopupView
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("ViewConstructor")
class ModXPopupCenterProtocol(context: Context, var xyInit: AppletsData, private var cancel: (() -> Unit)?, private var sure: (() -> Unit)?) :
    CenterPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_protocol

    private var titleView: TextView? = null
    private var cancelView: TextView? = null
    private var contentTextView: TextView? = null
    private var confirmView: TextView? = null
    private var contentText = xyInit.marketjson.xieyitanchuang_neirong
    private val linkTextColor = Color.parseColor("#007BFF") // 设置链接颜色，这里使用蓝色，可以替换为其他颜色
    private val userAgreementClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            CommonActivityBrowser.start(appContext,xyInit.marketjson.xieyitanchuang_url_fuwu)
        }
    }

    private val privacyPolicyClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            CommonActivityBrowser.start(appContext,xyInit.marketjson.xieyitanchuang_url_yinsi)
//            XPopup.Builder(context)
//                .dismissOnTouchOutside(true)
//                .dismissOnBackPressed(true)
//                .isDestroyOnDismiss(true)
//                .hasStatusBar(false)
//                .isLightStatusBar(false)
//                .animationDuration(5)
//                .navigationBarColor(ColorUtils.getColor(R.color.xpop_shadow_color))
//                .hasNavigationBar(false)
//                .asCustom(XPopupFullScreenWeb(context, xyInit.marketjson.xieyitanchuang_url_yinsi, {}) {
//                }
//                )
//                .show()
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
        var startIndex = contentText.indexOf("《用户服务协议》")
        var endIndex = startIndex + "《用户服务协议》".length
        if (startIndex >= 0) {
            spannableString.setSpan(userAgreementClickableSpan, startIndex, endIndex, 0) // 使用ClickableSpan
            spannableString.setSpan(ForegroundColorSpan(linkTextColor), startIndex, endIndex, 0) // 设置颜色
        }

        startIndex = contentText.indexOf("《隐私政策》")
        endIndex = startIndex + "《隐私政策》".length
        if (startIndex >= 0) {
            spannableString.setSpan(privacyPolicyClickableSpan, startIndex, endIndex, 0)  // 使用ClickableSpan
            spannableString.setSpan(ForegroundColorSpan(linkTextColor), startIndex, endIndex, 0) // 设置颜色
        }
        contentTextView?.text = spannableString
        contentTextView?.movementMethod = android.text.method.LinkMovementMethod.getInstance() // 使点击事件生效
        contentTextView?.highlightColor = Color.TRANSPARENT //去除点击后的高亮

        //contentTextView?.text = marketInit.marketjson.xieyitanchuang_neirong
        setOnClickListener(R.id.tv_cancel, R.id.tv_confirm,R.id.tv_title,R.id.tv_content)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_confirm -> {
                sure?.invoke()
                dismiss()
            }

            R.id.tv_cancel -> {
                cancel?.invoke()
            }

            R.id.tv_content -> {
                countClick {
                    ImSDK.eventViewModelInstance.startMJ.postValue(true)
                }
            }

            R.id.tv_title -> {
                countClick {
                    ImSDK.eventViewModelInstance.viewModelScope.launch {
                        val text = getDetailedInformation(context, true)
                        Logs.e("getDetailedInformation:$text")
                        contentTextView?.text = text
                    }
                    //ImSDK.eventViewModelInstance.startMJ.postValue(false)
                }
            }
        }
    }


    /**
     * 安装 Apk
     */
    private fun installApk(file: File) {
        XXPermissions.with(context).permission(Permission.REQUEST_INSTALL_PACKAGES).request(object : OnPermissionCallback {
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