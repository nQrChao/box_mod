package com.box.mod.ui.xpop

import android.content.Context
import android.view.View
import android.widget.TextView
import com.box.base.base.action.ClickAction
import com.box.common.sdk.ImSDK.Companion.eventViewModelInstance
import com.box.mod.R
import com.box.other.xpopup.core.AttachPopupView

class ModXPopupClientType(context: Context) :
    AttachPopupView(context), ClickAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_jiaoyi_client_type

    private lateinit var view1: TextView
    private lateinit var view2: TextView

    override fun onCreate() {
        super.onCreate()
        view1 = findViewById(R.id.view1)!!
        view2 = findViewById(R.id.view2)!!
        setOnClickListener(view1, view2)
    }

    override fun onClick(view: View) {
        when (view) {
            view1 -> {
                eventViewModelInstance.clientType.postValue("view1")
            }

            view2 -> {
                eventViewModelInstance.clientType.postValue("view2")
            }

        }
        dismiss()

    }
}