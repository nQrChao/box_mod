package com.box.mod.ui.xpop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.box.base.base.action.ClickAction
import com.box.base.base.action.KeyboardAction
import com.box.common.data.model.ModGameFuLiInfo
import com.box.mod.R
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.xpopup.core.BottomPopupView

@SuppressLint("ViewConstructor")
class XPopupBottomYouHui(
    context: Context, private var lingQuType: Boolean, private var youhuiBean: ModGameFuLiInfo, private var cancel: (() -> Unit)?, private var sure: ((
        fuLiInfo:
        ModGameFuLiInfo
    )
    -> Unit)?
) :
    BottomPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_youhui_bottom_dialog

    private lateinit var cancelImage: ImageView
    private lateinit var priceText: TextView
    private lateinit var titleText: TextView
    private lateinit var title2Text: TextView
    private lateinit var lingquBtn: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        cancelImage = findViewById(R.id.cancel)!!
        priceText = findViewById(R.id.price)!!
        titleText = findViewById(R.id.title)!!
        title2Text = findViewById(R.id.title2)!!
        lingquBtn = findViewById(R.id.lingqu)!!

        if (lingQuType) {
            lingquBtn.alpha = 0.5f
            lingquBtn.text = "已领取"
            lingquBtn.isClickable = false
        }

        priceText.text = StringUtils.format("￥%s", youhuiBean.coupon_list[0].amount)
        titleText.text = StringUtils.format("单笔满%s元可用", youhuiBean.coupon_list[0].amount)
        title2Text.text = StringUtils.format("适用于：《%s》", youhuiBean.coupon_list[0].displayName)

        setOnClickListener(R.id.cancel, R.id.lingqu)


    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.lingqu -> {
                sure?.invoke(youhuiBean)
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