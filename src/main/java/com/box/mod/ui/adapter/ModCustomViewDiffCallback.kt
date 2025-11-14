package com.box.mod.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.box.common.data.model.ModValuationCommitBean

class ModCustomViewDiffCallback(
    private val oldList: List<ModValuationCommitBean>,
    private val newList: List<ModValuationCommitBean>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}



