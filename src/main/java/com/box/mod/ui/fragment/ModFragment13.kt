package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.network.NetState
import com.box.mod.BR.createType
import com.box.mod.BR.position
import com.box.mod.R
import com.box.mod.databinding.ModFragment13Binding
import com.box.mod.databinding.ModItemCreaterLabelBinding
import com.box.mod.ui.data.ModCreateTypeBean
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder


class ModFragment13 : BaseTitleBarFragment<ModFragment13Model, ModFragment13Binding>() {
    private var createTypeAdapter = ItemCreateTypeAdapter(mutableListOf())

    override val mViewModel: ModFragment13Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_13

    companion object {
        fun newInstance(): ModFragment13 {
            return ModFragment13()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            statusBarDarkFont(true)
            init()
        }

    }


    override fun createObserver() {

    }

    override fun lazyLoadData() {

    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun confirm() {

        }

    }

    /**********************************************Adapter**************************************************/
    class ItemCreateTypeAdapter constructor(list: MutableList<ModCreateTypeBean>) :
        BaseQuickAdapter<ModCreateTypeBean, BaseDataBindingHolder<ModItemCreaterLabelBinding>>(
            R.layout.mod_item_creater_label, list
        ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemCreaterLabelBinding>, item: ModCreateTypeBean) {
            holder.dataBinding?.setVariable(createType, item)
            holder.dataBinding?.setVariable(position, holder.bindingAdapterPosition)
        }
    }


}


