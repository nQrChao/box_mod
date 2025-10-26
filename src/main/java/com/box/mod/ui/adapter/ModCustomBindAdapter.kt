package com.box.mod.ui.adapter

import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.box.mod.databinding.ItemTabTextBinding
import com.box.mod.ui.data.ModTabTextBean
import com.box.other.tablayout.DslTabLayout

object ModCustomBindAdapter {
    @BindingAdapter(value = ["dynamicTabs"])
    @JvmStatic
    fun setDynamicTabs(tabLayout: DslTabLayout, titles: List<String>?) {
        tabLayout.removeAllViews()
        if (titles.isNullOrEmpty()) {
            return
        }
        val inflater = LayoutInflater.from(tabLayout.context)
        titles.forEachIndexed { index, title ->
            val itemBinding = ItemTabTextBinding.inflate(
                inflater,
                tabLayout,
                false
            )
            itemBinding.title = title
            itemBinding.root.tag = index
            tabLayout.addView(itemBinding.root)
        }
    }

    @BindingAdapter(value = ["dynamicTabsBean"])
    @JvmStatic
    fun dynamicTabsBean(tabLayout: DslTabLayout, titles: List<ModTabTextBean>?) {
        tabLayout.removeAllViews()
        if (titles.isNullOrEmpty()) {
            return
        }
        val inflater = LayoutInflater.from(tabLayout.context)
        titles.forEachIndexed { index, modTab ->
            val itemBinding = ItemTabTextBinding.inflate(
                inflater,
                tabLayout,
                false
            )
            itemBinding.modTab = modTab
            itemBinding.root.tag = index
            tabLayout.addView(itemBinding.root)
        }
    }

    @BindingAdapter(value = ["dynamicBackground"])
    @JvmStatic
    fun setDynamicBackground(view: TextView, resId: Int) {
        if (resId != 0) { // 避免传入无效ID
            view.setBackgroundResource(resId)
        }
    }

    @BindingAdapter(value = ["dynamicTextColor"])
    @JvmStatic
    fun setDynamicTextColor(view: TextView, resId: Int) {
        if (resId != 0) { // 避免传入无效ID
            view.setTextColor(ContextCompat.getColor(view.context, resId))
        }
    }



}