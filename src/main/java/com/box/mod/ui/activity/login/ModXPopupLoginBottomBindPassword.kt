package com.box.mod.ui.activity.login

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.box.base.base.action.ClickAction
import com.box.base.base.action.KeyboardAction
import com.box.mod.R
import com.box.other.xpopup.core.BottomPopupView

@SuppressLint("ViewConstructor")
class ModXPopupLoginBottomBindPassword(context: Context, private var cancel: (() -> Unit)?, private var sure: ((tips: String) -> Unit)?) :
    BottomPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_login_bottom_bindpassword

    private lateinit var password1: EditText
    private lateinit var password2: EditText
    private lateinit var cancelBtn: Button
    private lateinit var confirmBtn: Button


    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        password1 = findViewById(R.id.password_1)!!
        password2 = findViewById(R.id.password_2)!!
        cancelBtn = findViewById(R.id.cancel_button)!!
        confirmBtn = findViewById(R.id.confirm_button)!!


        setOnClickListener(R.id.cancel_button, R.id.confirm_button)

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

        }
    }

    override fun dismiss() {
        super.dismiss()
        hideKeyboard(this)
    }


}