package com.chaoji.mod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModItemAppletsPicSmallBinding

class AppletPicSmallAdapter constructor(list: MutableList<AppletsInfo>) : BaseQuickAdapter<AppletsInfo, BaseDataBindingHolder<ModItemAppletsPicSmallBinding>>(
    R.layout.mod_item_applets_pic_small, list) {
    override fun convert(holder: BaseDataBindingHolder<ModItemAppletsPicSmallBinding>, item: AppletsInfo) {
        holder.dataBinding?.setVariable(BR.applet, item)
    }

}