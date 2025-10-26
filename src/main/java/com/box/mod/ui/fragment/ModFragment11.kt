package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.network.NetState
import com.box.mod.R
import com.box.mod.databinding.ModFragment11Binding
import com.box.other.immersionbar.immersionBar


class ModFragment11 : BaseTitleBarFragment<ModFragment11Model, ModFragment11Binding>() {
    override val mViewModel: ModFragment11Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_11

    companion object {
        fun newInstance(): ModFragment11 {
            return ModFragment11()
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


}


