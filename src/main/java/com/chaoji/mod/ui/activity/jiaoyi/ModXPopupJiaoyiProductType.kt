package com.chaoji.mod.ui.activity.jiaoyi

import android.content.Context
import android.view.View
import android.widget.TextView
import com.chaoji.base.base.action.ClickAction
import com.chaoji.im.sdk.ImSDK.Companion.eventViewModelInstance
import com.chaoji.mod.R
import com.chaoji.other.xpopup.core.AttachPopupView

class ModXPopupJiaoyiProductType(context: Context) :
    AttachPopupView(context), ClickAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_jiaoyi_product_type

    private lateinit var view1: TextView
    private lateinit var view2: TextView
    private lateinit var view3: TextView

    override fun onCreate() {
        super.onCreate()
        view1 = findViewById(R.id.view1)!!
        view2 = findViewById(R.id.view2)!!
        view3 = findViewById(R.id.view3)!!
        setOnClickListener(view1, view2, view3)
    }

    override fun onClick(view: View) {
        when (view) {
            view1 -> {
                eventViewModelInstance.productType.postValue("view1")
            }

            view2 -> {
                eventViewModelInstance.productType.postValue("view2")
            }

            view3 -> {
                eventViewModelInstance.productType.postValue("view3")
            }


        }
        dismiss()

    }
}