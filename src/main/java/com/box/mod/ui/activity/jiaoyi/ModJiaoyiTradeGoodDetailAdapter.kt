package com.box.mod.ui.activity.jiaoyi

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModItemJiaoyiPriceProBinding
import com.box.common.data.model.ModTradeGoodDetailBean

class ModJiaoyiTradeGoodDetailAdapter : BaseQuickAdapter<ModTradeGoodDetailBean, BaseDataBindingHolder<ModItemJiaoyiPriceProBinding>>(
    R.layout.mod_item_jiaoyi_price_pro) {

    override fun convert(holder: BaseDataBindingHolder<ModItemJiaoyiPriceProBinding>, item: ModTradeGoodDetailBean) {
        // 绑定逻辑保持不变
        holder.dataBinding?.setVariable(BR.goodDetailBean, item)
        holder.dataBinding?.executePendingBindings()
    }
}