package com.box.mod.ui.adapter

import android.view.View
import android.view.ViewGroup
import com.zhpan.bannerview.BaseBannerAdapter
import androidx.databinding.DataBindingUtil
import com.box.common.data.model.ModDataBean
import com.box.mod.R
import com.box.mod.databinding.ModItemGameBannerBinding
import com.zhpan.bannerview.BaseViewHolder
import java.lang.NullPointerException

class MainBannerAdapter : BaseBannerAdapter<ModDataBean>() {

    override fun createViewHolder(parent: ViewGroup, itemView: View, viewType: Int): BaseViewHolder<ModDataBean?> {
        val binding = DataBindingUtil.bind<ModItemGameBannerBinding>(itemView)
            ?: throw NullPointerException("binding is Null")
        return DataBindingViewHolder(binding)
    }

    override fun bindData(holder: BaseViewHolder<ModDataBean?>?, data: ModDataBean?, position: Int, pageSize: Int) {
        if (holder is DataBindingViewHolder) {
            holder.binding.modData = data
        }
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.mod_item_game_banner
    }
}

internal class DataBindingViewHolder(var binding: ModItemGameBannerBinding) : BaseViewHolder<ModDataBean?>(binding.root)