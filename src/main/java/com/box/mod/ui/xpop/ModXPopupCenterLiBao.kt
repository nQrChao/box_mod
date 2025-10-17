package com.box.mod.ui.xpop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import com.box.base.base.action.ClickAction
import com.box.base.base.action.KeyboardAction
import com.box.common.countClick
import com.box.common.eventViewModel
import com.box.mod.R
import com.box.other.xpopup.core.CenterPopupView

@SuppressLint("ViewConstructor")
class ModXPopupCenterLiBao(context: Context, var codeText: String, private var cancel: (() -> Unit)?, private var sure: (() -> Unit)?) :
    CenterPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_libao

    private var titleView: TextView? = null
    private var cancelView: TextView? = null
    private var gameCodeView: TextView? = null
    private var confirmView: TextView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        titleView = findViewById<TextView>(R.id.tv_title)
        gameCodeView = findViewById<TextView>(R.id.gameCode)
        cancelView = findViewById<TextView>(R.id.tv_cancel)
        confirmView = findViewById<TextView>(R.id.tv_confirm)

        gameCodeView?.text = codeText

        setOnClickListener(R.id.tv_cancel, R.id.tv_confirm,R.id.tv_content,R.id.tv_title)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_confirm -> {
                sure?.invoke()
                dismiss()
            }

            R.id.tv_cancel -> {
                cancel?.invoke()
                dismiss()
            }

            R.id.gameCode -> {

            }
            R.id.tv_title -> {
                countClick {
                    eventViewModel.startMJ.postValue(false)
                }
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        hideKeyboard(this)
    }


}