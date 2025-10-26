package com.box.mod.ui.data

import androidx.databinding.ObservableBoolean
import com.box.mod.R // 确保导入你的R文件


data class ModTabTextBean(
    val name: String,
    var select: ObservableBoolean = ObservableBoolean(false)
) {

    fun bg(): Int {
        return if (select.get()) {
            R.drawable.bg_item_tab_text_select // 对应 item_tab_text.xml 里的 tools:background
        } else {
            R.drawable.bg_item_tab_text_default
        }
    }

    fun txtColor(): Int {
        return if (select.get()) {
            R.color.mod_tab_color_select
        } else {
            R.color.mod_tab_color_default
        }
    }
}