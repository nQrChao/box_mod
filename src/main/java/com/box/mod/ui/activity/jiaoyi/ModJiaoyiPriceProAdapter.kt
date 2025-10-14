package com.box.mod.ui.activity.jiaoyi

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.common.data.model.AppletsInfo
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModItemJiaoyiPriceProBinding

class ModJiaoyiPriceProAdapter constructor(list: MutableList<AppletsInfo>) : BaseQuickAdapter<AppletsInfo, BaseDataBindingHolder<ModItemJiaoyiPriceProBinding>>(
    R.layout.mod_item_jiaoyi_price_pro, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemJiaoyiPriceProBinding>, item: AppletsInfo) {
        holder.dataBinding?.setVariable(BR.applet, item)
    }

}