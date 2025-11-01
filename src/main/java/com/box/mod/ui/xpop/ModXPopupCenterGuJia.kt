package com.box.mod.ui.xpop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import com.box.base.base.action.ClickAction
import com.box.base.base.action.KeyboardAction
import com.box.mod.R
import com.box.other.xpopup.core.CenterPopupView

@SuppressLint("ViewConstructor")
class ModXPopupCenterGuJia(context: Context, var contentText: String,var priceText:String, private var cancel: (() -> Unit)?, private var sure: (() -> Unit)?) :
    CenterPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_gujia

    private var tvPrice: TextView? = null
    private var cancelView: TextView? = null
    private var tvContent: TextView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        tvPrice = findViewById<TextView>(R.id.tv_price)
        tvContent = findViewById<TextView>(R.id.tv_content)
        cancelView = findViewById<TextView>(R.id.tv_cancel)

        tvPrice?.text = priceText
        tvContent?.text = contentText

        setOnClickListener(R.id.tv_cancel,R.id.tv_content,R.id.tv_price)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_content -> {
                sure?.invoke()
                dismiss()
            }

            R.id.tv_cancel -> {
                cancel?.invoke()
                dismiss()
            }

            R.id.tv_price -> {

            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        hideKeyboard(this)
    }


}