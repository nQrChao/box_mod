package com.chaoji.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.im.data.model.LocalGameInfo
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModItemLocalGamesBinding

class LocalGamesAdapter constructor(list: MutableList<LocalGameInfo>) : BaseQuickAdapter<LocalGameInfo, BaseDataBindingHolder<ModItemLocalGamesBinding>>(
    R.layout.mod_item_local_games, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemLocalGamesBinding>, item: LocalGameInfo) {
        holder.dataBinding?.setVariable(BR.localGame, item)
    }

}