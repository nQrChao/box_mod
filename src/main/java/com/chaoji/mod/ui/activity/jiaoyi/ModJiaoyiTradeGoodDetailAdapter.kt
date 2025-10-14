package com.chaoji.mod.ui.activity.jiaoyi

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModItemJiaoyiPriceProBinding
import com.chaoji.im.data.model.ModTradeGoodDetailBean

class ModJiaoyiTradeGoodDetailAdapter : BaseQuickAdapter<ModTradeGoodDetailBean, BaseDataBindingHolder<ModItemJiaoyiPriceProBinding>>(
    R.layout.mod_item_jiaoyi_price_pro) {

    override fun convert(holder: BaseDataBindingHolder<ModItemJiaoyiPriceProBinding>, item: ModTradeGoodDetailBean) {
        // 绑定逻辑保持不变
        holder.dataBinding?.setVariable(BR.goodDetailBean, item)
        holder.dataBinding?.executePendingBindings()
    }
}