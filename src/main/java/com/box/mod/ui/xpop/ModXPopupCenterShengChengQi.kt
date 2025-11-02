package com.box.mod.ui.xpop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.box.base.base.action.ClickAction
import com.box.base.base.action.KeyboardAction
import com.box.common.data.model.ModDataBean
import com.box.mod.R
import com.box.other.blankj.utilcode.util.ClipboardUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.xpopup.core.CenterPopupView

@SuppressLint("ViewConstructor")
class ModXPopupCenterShengChengQi(context: Context, var randomNameBean: ModDataBean, private var shoucang: ((ModDataBean) -> Unit)?) :
    CenterPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_shengchengqi

    private var tvContent: TextView? = null
    private var tvCopy: TextView? = null
    private var tvShoucang: TextView? = null
    private var ivCancel: ImageView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        tvCopy = findViewById<TextView>(R.id.tv_copy)
        tvContent = findViewById<TextView>(R.id.tv_content)
        tvShoucang = findViewById<TextView>(R.id.tv_shoucang)
        ivCancel = findViewById<ImageView>(R.id.iv_cancel)

        tvContent?.text = randomNameBean.name

        setOnClickListener(R.id.tv_shoucang,R.id.iv_cancel,R.id.tv_copy)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_shoucang -> {
                shoucang?.invoke(randomNameBean)
                dismiss()
            }

            R.id.iv_cancel -> {
                dismiss()
            }

            R.id.tv_copy -> {
                ClipboardUtils.copyText(randomNameBean.name)
                Toaster.show("角色名已复制")
                dismiss()
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        hideKeyboard(this)
    }


}