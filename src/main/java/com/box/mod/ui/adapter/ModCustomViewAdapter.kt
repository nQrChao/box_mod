package com.box.mod.ui.adapter

import android.view.animation.AnimationUtils
import com.box.common.data.model.ModValuationCommitBean
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModItemCustomFormBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

class ModCustomViewAdapter : BaseQuickAdapter<ModValuationCommitBean, BaseDataBindingHolder<ModItemCustomFormBinding>>(
    R.layout.mod_item_custom_form) {
    private var lastPosition = -1
    override fun convert(holder: BaseDataBindingHolder<ModItemCustomFormBinding>, item: ModValuationCommitBean) {
        holder.dataBinding?.setVariable(BR.valuationCommitBean, item)
        holder.dataBinding?.executePendingBindings()

        if (holder.layoutPosition > lastPosition) {
            val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_slide_up_fade_in)
            animation.startOffset = 50L * holder.layoutPosition.toLong()
            holder.itemView.startAnimation(animation)
            lastPosition = holder.layoutPosition
        }
    }

    override fun onViewRecycled(holder: BaseDataBindingHolder<ModItemCustomFormBinding>) {
        holder.itemView.clearAnimation()
        super.onViewRecycled(holder)
    }

    fun resetAnimationState() {
        lastPosition = -1
    }

    fun updateList(newList: List<ModValuationCommitBean>) {
        setList(newList)
//        val diffResult = DiffUtil.calculateDiff(ModCustomViewDiffCallback(data, newList))
//        data.clear()
//        data.addAll(newList)
//        diffResult.dispatchUpdatesTo(this)
    }

}

