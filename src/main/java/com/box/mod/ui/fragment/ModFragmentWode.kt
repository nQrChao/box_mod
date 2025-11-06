package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.mod.R
import com.box.mod.databinding.ModFragmentWodeBinding
import com.box.mod.ui.activity.ModActivitySafety
import com.box.other.immersionbar.immersionBar


class ModFragmentWode : BaseTitleBarFragment<ModFragmentWode.Model, ModFragmentWodeBinding>() {
    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_wode

    companion object {
        fun newInstance(): ModFragmentWode {
            return ModFragmentWode()
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

    override fun onResume() {
        super.onResume()

    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun confirm() {

        }

        fun fanKui() {

        }

        fun keFu() {

        }

        fun beiAn() {

        }

        fun uXy() {

        }

        fun yXy() {

        }

        fun fangChenMi() {

        }

        fun userAnQuan() {
            ModActivitySafety.start(appContext)
        }


    }

    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "个人中心") {
        val tuiSong = MutableLiveData<Boolean>()
        var pic = IntObservableField(0)

    }


}


