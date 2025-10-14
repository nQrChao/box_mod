package com.chaoji.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.chaoji.base.base.fragment.BaseTitleBarFragment
import com.chaoji.base.network.NetState
import com.chaoji.mod.R
import com.chaoji.mod.databinding.MainFragment1Binding
import com.chaoji.other.immersionbar.immersionBar


class MainFragment1 : BaseTitleBarFragment<MainFragment1Model, MainFragment1Binding>() {

    override fun layoutId(): Int = R.layout.main_fragment_1

    companion object {
        fun newInstance(): MainFragment1 {
            return MainFragment1()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }

    }

    override fun initData() {

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


