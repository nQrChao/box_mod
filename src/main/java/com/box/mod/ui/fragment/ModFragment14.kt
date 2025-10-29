package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.network.NetState
import com.box.mod.R
import com.box.mod.databinding.ModFragment14Binding
import com.box.other.immersionbar.immersionBar


class ModFragment14 : BaseTitleBarFragment<ModFragment14.Model, ModFragment14Binding>() {
    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_14

    companion object {
        fun newInstance(): ModFragment14 {
            return ModFragment14()
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

    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "") {
        var pic = IntObservableField(0)

    }


}


