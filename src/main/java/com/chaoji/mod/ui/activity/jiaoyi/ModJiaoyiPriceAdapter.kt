package com.chaoji.mod.ui.activity.jiaoyi

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModItemJiaoyiPriceBinding

class ModJiaoyiPriceAdapter constructor(list: MutableList<AppletsInfo>) : BaseQuickAdapter<AppletsInfo, BaseDataBindingHolder<ModItemJiaoyiPriceBinding>>(
    R.layout.mod_item_jiaoyi_price, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemJiaoyiPriceBinding>, item: AppletsInfo) {
        holder.dataBinding?.setVariable(BR.applet, item)
    }

}