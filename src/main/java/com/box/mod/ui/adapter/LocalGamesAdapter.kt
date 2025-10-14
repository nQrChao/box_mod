package com.box.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.common.data.model.LocalGameInfo
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModItemLocalGamesBinding

class LocalGamesAdapter constructor(list: MutableList<LocalGameInfo>) : BaseQuickAdapter<LocalGameInfo, BaseDataBindingHolder<ModItemLocalGamesBinding>>(
    R.layout.mod_item_local_games, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemLocalGamesBinding>, item: LocalGameInfo) {
        holder.dataBinding?.setVariable(BR.localGame, item)
    }

}