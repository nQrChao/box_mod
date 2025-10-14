package com.chaoji.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.im.data.model.ModGameLabelBean
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModItemGameLabelBinding

class ModGameLabelAdapter constructor(list: MutableList<ModGameLabelBean>) : BaseQuickAdapter<ModGameLabelBean, BaseDataBindingHolder<ModItemGameLabelBinding>>(
    R.layout.mod_item_game_label, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemGameLabelBinding>, item: ModGameLabelBean) {
        holder.dataBinding?.setVariable(BR.gameLabel, item)

    }

}