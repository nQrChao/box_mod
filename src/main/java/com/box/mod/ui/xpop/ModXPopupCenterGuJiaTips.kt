package com.box.mod.ui.xpop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.box.base.base.action.ClickAction
import com.box.base.base.action.KeyboardAction
import com.box.mod.R
import com.box.other.xpopup.core.CenterPopupView

@SuppressLint("ViewConstructor")
class ModXPopupCenterGuJiaTips(context: Context,private var cancel: ((Boolean) -> Unit)?) :
    CenterPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_gujia_tips

    private var agreement: CheckBox? = null
    private var cancelView: TextView? = null
    private var tvContent: TextView? = null

    override fun onCreate() {
        super.onCreate()
        agreement = findViewById<CheckBox>(R.id.agreementButton)
        cancelView = findViewById<TextView>(R.id.tv_cancel)

        setOnClickListener(R.id.tv_cancel)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_cancel -> {
                cancel?.invoke(agreement!!.isChecked)
                dismiss()
            }

        }
    }

    override fun dismiss() {
        super.dismiss()
        hideKeyboard(this)
    }


}