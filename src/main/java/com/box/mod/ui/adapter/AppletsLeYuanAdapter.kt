package com.box.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.common.data.model.AppletsLeYuan
import com.box.mod.R
import com.box.mod.BR
import com.box.mod.databinding.ModItemAppletsLeyuanBinding

class AppletsLeYuanAdapter constructor(list: MutableList<AppletsLeYuan>) : BaseQuickAdapter<AppletsLeYuan, BaseDataBindingHolder<ModItemAppletsLeyuanBinding>>(
    R.layout.mod_item_applets_leyuan, list
) {
    override fun convert(holder: BaseDataBindingHolder<ModItemAppletsLeyuanBinding>, item: AppletsLeYuan) {
        holder.dataBinding?.setVariable(BR.appletLeYuan, item)
    }

    override fun onBindViewHolder(holder: BaseDataBindingHolder<ModItemAppletsLeyuanBinding>, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        val binding = holder.dataBinding
        val item = getItem(position)

        // 遍历所有的 payload
        for (payload in payloads) {
            if (payload == "LIKE_UPDATE") {
                binding?.setVariable(BR.appletLeYuan, item)
            }
        }
    }

}
