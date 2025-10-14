package com.chaoji.mod.ui.activity.jiaoyi

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.chaoji.base.base.action.ClickAction
import com.chaoji.base.base.action.KeyboardAction
import com.chaoji.mod.R
import com.chaoji.other.xpopup.core.BottomPopupView

@SuppressLint("ViewConstructor")
class ModXPopupJiaoyiBottomHuanJia(context: Context, private var cancel: (() -> Unit)?, private var sure: ((tips: String) -> Unit)?) :
    BottomPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_jiaoyi_bottom_huanjia

    private lateinit var price: TextView
    private lateinit var liuyan: EditText
    private lateinit var cancelBtn: Button
    private lateinit var confirmBtn: Button


    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        price = findViewById(R.id.price)!!
        liuyan = findViewById(R.id.edit_liuyan)!!
        cancelBtn = findViewById(R.id.cancel_button)!!
        confirmBtn = findViewById(R.id.confirm_button)!!


        setOnClickListener(R.id.cancel, R.id.cancel_button, R.id.confirm_button)

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cancel_button -> {
                cancel?.invoke()
                dismiss()
            }

            R.id.confirm_button -> {
                val showText = if (liuyan.text.isEmpty()) {
                    "已发起还价申请，请等待卖家改价。\n卖家改价后可在消息中心查看。"
                } else {
                    "已向卖家发送留言，请等待卖家改价。\n卖家改价后可在消息中心查看。"
                }
                sure?.invoke(showText)
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