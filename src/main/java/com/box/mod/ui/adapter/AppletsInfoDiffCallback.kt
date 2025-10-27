package com.box.mod.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.box.common.data.model.ModInitBean

class AppletsInfoDiffCallback : DiffUtil.ItemCallback<ModInitBean>() {

    /**
     * 判断是否是同一个 item
     * @return true 如果是同一个 item, false 则不是
     */
    override fun areItemsTheSame(oldItem: ModInitBean, newItem: ModInitBean): Boolean {
        // 这里需要用一个在数据中唯一的标识符来判断，比如 id
        // 假设 AppletsInfo 有一个叫 'id' 的唯一字段
        return oldItem.id == newItem.id
    }

    /**
     * 判断 item 的内容是否完全一致
     * 只有在 areItemsTheSame() 返回 true 时，此方法才会被调用
     * @return true 内容相同，不需要更新 UI, false 内容不同，需要更新 UI
     */
    override fun areContentsTheSame(oldItem: ModInitBean, newItem: ModInitBean): Boolean {
        // 如果你的 AppletsInfo 是一个 data class, 可以直接用 == 来比较所有字段
        // 如果不是，你需要手动比较所有会影响 UI 显示的字段
        return oldItem == newItem
    }
}