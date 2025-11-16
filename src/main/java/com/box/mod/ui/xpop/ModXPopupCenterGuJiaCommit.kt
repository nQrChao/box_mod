package com.box.mod.ui.xpop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.box.base.base.action.ClickAction
import com.box.base.base.action.KeyboardAction
import com.box.mod.R
import com.box.other.xpopup.core.CenterPopupView

@SuppressLint("ViewConstructor")
class ModXPopupCenterGuJiaCommit(context: Context, var contentText: String, private var sure: (() -> Unit)?) :
    CenterPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_gujia_commit

    private var btnCommit: Button? = null
    private var tvContent: TextView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        btnCommit = findViewById<Button>(R.id.btn_commit)
        tvContent = findViewById<TextView>(R.id.tv_content)

        //tvContent?.text = contentText

        setOnClickListener(R.id.tv_content,R.id.btn_commit)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_commit -> {
                sure?.invoke()
                dismiss()
            }

            R.id.tv_content -> {

            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        hideKeyboard(this)
    }


}