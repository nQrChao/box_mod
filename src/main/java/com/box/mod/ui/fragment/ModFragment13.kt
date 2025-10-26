package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.network.NetState
import com.box.mod.R
import com.box.mod.databinding.ModFragment13Binding
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder


class ModFragment13 : BaseTitleBarFragment<ModFragment13Model, ModFragment13Binding>() {
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
    class ItemTypeAdapter constructor(list: MutableList<AppletsXiaoGameFuLiInfo.DemoInfo>) :
        BaseQuickAdapter<AppletsXiaoGameFuLiInfo.DemoInfo, BaseDataBindingHolder<ItemGameShiwanBinding>>(
            R.layout.item_game_shiwan, list
        ) {
        override fun convert(holder: BaseDataBindingHolder<ItemGameShiwanBinding>, item: AppletsXiaoGameFuLiInfo.DemoInfo) {
            holder.dataBinding?.setVariable(com.chaoji.mod.BR.demoInfo, item)
            holder.dataBinding?.setVariable(com.chaoji.mod.BR.position, holder.bindingAdapterPosition)
        }
    }


}


