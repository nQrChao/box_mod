package com.box.mod.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.common.BR
import com.box.common.data.model.ModGameHallList
import com.box.common.data.model.ModGameLabelBean
import com.box.common.ui.adapter.HorizontalSpaceItemDecoration
import com.box.mod.R
import com.box.mod.databinding.ModItemRankBinding

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