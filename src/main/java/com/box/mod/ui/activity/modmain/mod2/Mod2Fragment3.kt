package com.box.mod.ui.activity.modmain.mod2

import android.os.Bundle
import com.box.base.base.action.HandlerAction
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.network.NetState
import com.box.common.GAME_CHENGYUCAICAICAI
import com.box.common.GAME_JIANFENGCHAZHEN
import com.box.common.GAME_KAIXINYIXIA
import com.box.common.appContext
import com.box.mod.R
import com.box.mod.databinding.Mod2Fragment3Binding
import com.box.mod.ui.activity.game.ModActivityGameBrowser
import com.box.other.immersionbar.immersionBar

class Mod2Fragment3 : BaseTitleBarFragment<Mod2Fragment3Model, Mod2Fragment3Binding>(), HandlerAction {
    override fun layoutId(): Int = R.layout.mod2_fragment_3

    companion object {
        fun newInstance(): Mod2Fragment3 {
            return Mod2Fragment3()
        }
    }


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



    inner class ProxyClick {
        fun game1(){
            ModActivityGameBrowser.start(appContext, GAME_JIANFENGCHAZHEN)
        }
        fun game2(){
            ModActivityGameBrowser.start(appContext, GAME_KAIXINYIXIA)
        }
        fun game3(){
            ModActivityGameBrowser.start(appContext, GAME_CHENGYUCAICAICAI)
        }
        fun game4(){

        }

    }


}