package com.chaoji.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModItemAppletsPicBinding

class AppletPicAdapter constructor(list: MutableList<AppletsInfo>) : BaseQuickAdapter<AppletsInfo, BaseDataBindingHolder<ModItemAppletsPicBinding>>(
    R.layout.mod_item_applets_pic, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemAppletsPicBinding>, item: AppletsInfo) {
        holder.dataBinding?.setVariable(BR.applet, item)
    }

}