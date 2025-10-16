package com.box.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.common.data.model.ModInfo
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModItemAppletsBinding

class AppletsAdapter constructor(list: MutableList<ModInfo>) : BaseQuickAdapter<ModInfo, BaseDataBindingHolder<ModItemAppletsBinding>>(
    R.layout.mod_item_applets, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemAppletsBinding>, item: ModInfo) {
        holder.dataBinding?.setVariable(BR.applet, item)
    }

}