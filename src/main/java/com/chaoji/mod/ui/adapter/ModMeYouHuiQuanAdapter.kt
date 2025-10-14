package com.chaoji.mod.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.im.appContext
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.im.data.model.ModGameLabelBean
import com.chaoji.im.data.model.ModMeYouHuiQuanBean
import com.chaoji.im.data.model.ModYouHuiQuanBean
import com.chaoji.im.ui.adapter.HorizontalSpaceItemDecoration
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModItemAppletsPicBinding
import com.chaoji.mod.databinding.ModItemMeYouhuiquanBinding

class ModMeYouHuiQuanAdapter constructor(list: MutableList<ModMeYouHuiQuanBean>) : BaseQuickAdapter<ModMeYouHuiQuanBean, BaseDataBindingHolder<ModItemMeYouhuiquanBinding>>(
    R.layout.mod_item_me_youhuiquan, list) {
    private var appList: MutableList<ModGameLabelBean> = mutableListOf()
    private var appListAdapter = ModGameLabelAdapter(appList)
    override fun convert(holder: BaseDataBindingHolder<ModItemMeYouhuiquanBinding>, item: ModMeYouHuiQuanBean) {
        holder.dataBinding?.setVariable(BR.meYouHuiQuan, item)
        holder.dataBinding?.recyclerView?.run {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(HorizontalSpaceItemDecoration(5))
            adapter = appListAdapter
        }

        appListAdapter.setList(item.game_labels)


    }

}