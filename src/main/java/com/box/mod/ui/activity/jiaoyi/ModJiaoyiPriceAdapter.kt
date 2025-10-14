package com.box.mod.ui.activity.jiaoyi

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.common.data.model.AppletsInfo
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModItemJiaoyiPriceBinding

class ModJiaoyiPriceAdapter constructor(list: MutableList<AppletsInfo>) : BaseQuickAdapter<AppletsInfo, BaseDataBindingHolder<ModItemJiaoyiPriceBinding>>(
    R.layout.mod_item_jiaoyi_price, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemJiaoyiPriceBinding>, item: AppletsInfo) {
        holder.dataBinding?.setVariable(BR.applet, item)
    }

}