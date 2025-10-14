package com.chaoji.mod.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.common.BR
import com.chaoji.im.data.model.ModGameHallList
import com.chaoji.im.data.model.ModGameLabelBean
import com.chaoji.im.ui.adapter.HorizontalSpaceItemDecoration
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModItemRankBinding

class ModGameHallListItemAdapter constructor(list: MutableList<ModGameHallList>) : BaseQuickAdapter<ModGameHallList, BaseDataBindingHolder<ModItemRankBinding>>(
    R.layout.mod_item_rank, list
) {
    var appList: MutableList<ModGameLabelBean> = mutableListOf()
    var appListAdapter = ModGameLabelAdapter(appList)
    override fun convert(holder: BaseDataBindingHolder<ModItemRankBinding>, item: ModGameHallList) {
        holder.dataBinding?.setVariable(BR.gameHallListBean, item)
        holder.dataBinding?.topText?.text = holder.bindingAdapterPosition.plus(1).toString()
        holder.dataBinding?.recyclerView?.run {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(HorizontalSpaceItemDecoration(2))
            adapter = appListAdapter
        }

        appListAdapter.setList(item.game_labels)
    }

}