package com.chaoji.mod.ui.activity.jiaoyi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.chaoji.base.base.action.ClickAction
import com.chaoji.base.base.action.KeyboardAction
import com.chaoji.im.appContext
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.mod.BuildConfig
import com.chaoji.mod.R
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.xpopup.core.BottomPopupView

@SuppressLint("ViewConstructor")
class ModXPopupJiaoyiBottomGouMai(context: Context, private var cancel: (() -> Unit)?, private var sure: ((tips: String) -> Unit)?) :
    BottomPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_jiaoyi_bottom_goumai

    private lateinit var xuzhi2: TextView
    private lateinit var xuzhi3: TextView
    private lateinit var checkView: CheckBox
    private lateinit var checkLayout: LinearLayout
    private lateinit var cancelBtn: Button
    private lateinit var confirmBtn: Button
    private val contentText2 = "2、时间因素造成的信息变化，不视为信息失实际，具体可能会存在的变化，了解更多交易须知"
    private val contentText3 = "3、部分游戏存在设备限制，购前务必先尝试游戏是否可正常登录，若无法进入不建议购买。交易完成后，不支持退换。如因不可抗力因素存在问题，联系客服核实并协助处理"
    private val linkTextColor2 = Color.parseColor("#007BFF")
    private val linkTextColor3 = Color.parseColor("#FF0000")
    private val userClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            if (BuildConfig.APP_UPDATE_ID == "27"){
                CommonActivityBrowser.start(appContext, "https://mobile.xiaodianyouxi.com/index.php/Index/market_view/?id=566")
            }else{
                CommonActivityBrowser.start(appContext, "https://mobile.xiaodianyouxi.com/index.php/Index/market_view/?id=588")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        xuzhi2 = findViewById(R.id.goumai_xuzhi2)!!
        xuzhi3 = findViewById(R.id.goumai_xuzhi3)!!
        checkView = findViewById(R.id.check_view)!!
        checkLayout = findViewById(R.id.check_layout)!!
        cancelBtn = findViewById(R.id.cancel_button)!!
        confirmBtn = findViewById(R.id.confirm_button)!!

        // 使用HtmlCompat.fromHtml处理HTML标记，同时为了更好的兼容性
        val spannableString2 = SpannableString(
            HtmlCompat.fromHtml(contentText2.replace("\n", "<br>"), HtmlCompat.FROM_HTML_MODE_LEGACY)
        )
        var startIndex = contentText2.indexOf("了解更多交易须知")
        var endIndex = startIndex + "了解更多交易须知".length
        if (startIndex >= 0) {
            spannableString2.setSpan(userClickableSpan, startIndex, endIndex, 0)
            spannableString2.setSpan(ForegroundColorSpan(linkTextColor2), startIndex, endIndex, 0)
        }
        xuzhi2.text = spannableString2
        xuzhi2.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        xuzhi2.highlightColor = Color.TRANSPARENT



        val spannableString3= SpannableString(
            HtmlCompat.fromHtml(contentText3.replace("\n", "<br>"), HtmlCompat.FROM_HTML_MODE_LEGACY)
        )
        startIndex = contentText3.indexOf("交易完成后，不支持退换")
        endIndex = startIndex + "交易完成后，不支持退换".length
        if (startIndex >= 0) {
            spannableString3.setSpan(ForegroundColorSpan(linkTextColor3), startIndex, endIndex, 0)
        }
        xuzhi3.text = spannableString3
        xuzhi3.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        xuzhi3.highlightColor = Color.TRANSPARENT

        val changeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            checkView.isChecked = isChecked
        }
        checkView.setOnCheckedChangeListener(changeListener)

        setOnClickListener(R.id.cancel, R.id.cancel_button, R.id.confirm_button, R.id.check_layout)

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.check_layout -> {
                checkView.isChecked = !checkView.isChecked
            }

            R.id.cancel_button -> {
                cancel?.invoke()
                dismiss()
            }

            R.id.confirm_button -> {
                if (!checkView.isChecked) {
                    checkLayout.startAnimation(AnimationUtils.loadAnimation(appContext, com.chaoji.common.R.anim.shake_anim))
                    Toaster.show("请先阅读买家须知")
                    return
                }
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