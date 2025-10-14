package com.chaoji.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.im.data.model.ModYouHuiQuanBean
import com.chaoji.mod.R
import com.chaoji.mod.BR
import com.chaoji.mod.databinding.ModItemYouhuiquan2Binding

class ModYouHuiQuanItemAdapter constructor(list: MutableList<ModYouHuiQuanBean>) : BaseQuickAdapter<ModYouHuiQuanBean, BaseDataBindingHolder<ModItemYouhuiquan2Binding>>(
    R.layout.mod_item_youhuiquan_2, list
) {
    override fun convert(holder: BaseDataBindingHolder<ModItemYouhuiquan2Binding>, item: ModYouHuiQuanBean) {
        holder.dataBinding?.setVariable(BR.youhuiquanBean, item)
    }

    override fun onBindViewHolder(holder: BaseDataBindingHolder<ModItemYouhuiquan2Binding>, position: Int, payloads: MutableList<Any>) {
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
                binding?.setVariable(BR.youhuiquanBean, item)
            }
        }
    }

}
