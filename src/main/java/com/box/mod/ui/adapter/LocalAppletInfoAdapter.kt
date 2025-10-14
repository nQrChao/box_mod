package com.box.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModItemAppletsDescProBinding
import com.box.common.data.model.ModLocalAppletsInfo

class LocalAppletInfoAdapter constructor(list: MutableList<ModLocalAppletsInfo>) : BaseQuickAdapter<ModLocalAppletsInfo, BaseDataBindingHolder<ModItemAppletsDescProBinding>>(
    R.layout.mod_item_applets_desc_pro, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemAppletsDescProBinding>, item: ModLocalAppletsInfo) {
        holder.dataBinding?.setVariable(BR.localAppletInfo, item)
    }

}