package com.box.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.common.data.model.ModGameLabelBean
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModItemGameLabelBinding

class ModGameLabelAdapter constructor(list: MutableList<ModGameLabelBean>) : BaseQuickAdapter<ModGameLabelBean, BaseDataBindingHolder<ModItemGameLabelBinding>>(
    R.layout.mod_item_game_label, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemGameLabelBinding>, item: ModGameLabelBean) {
        holder.dataBinding?.setVariable(BR.gameLabel, item)

    }

}