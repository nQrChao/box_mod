package com.chaoji.mod.ui.activity.modmain.mod5

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaoji.base.base.action.HandlerAction
import com.chaoji.base.base.fragment.BaseTitleBarFragment
import com.chaoji.base.ext.parseState
import com.chaoji.base.network.NetState
import com.chaoji.im.GAME_CAICAIDIANYINGMING
import com.chaoji.im.GAME_CAIMIYU
import com.chaoji.im.GAME_JIANFENGCHAZHEN
import com.chaoji.im.GAME_KAIXINYIXIA
import com.chaoji.im.appContext
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.im.data.model.LocalGameInfo
import com.chaoji.mod.R
import com.chaoji.mod.databinding.Mod5Fragment4Binding
import com.chaoji.mod.ui.activity.ModActivityPicDown
import com.chaoji.mod.ui.activity.game.ModActivityGameBrowser
import com.chaoji.mod.ui.adapter.AppletPicSmallAdapter
import com.chaoji.mod.ui.adapter.LocalGamesAdapter
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.blankj.utilcode.util.ResourceUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar

class Mod5Fragment4 : BaseTitleBarFragment<Mod5Fragment4Model, Mod5Fragment4Binding>(), HandlerAction {
    var appPicList: MutableList<AppletsInfo> = mutableListOf()
    var localGameList: MutableList<LocalGameInfo> = mutableListOf()
    var appPicAdapter = AppletPicSmallAdapter(appPicList)
    var localGameAdapter = LocalGamesAdapter(localGameList)
    override fun layoutId(): Int = R.layout.mod5_fragment_4

    companion object {
        fun newInstance(): Mod5Fragment4 {
            return Mod5Fragment4()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            statusBarDarkFont(true)
            init()
        }
        getLocalGames()

        mDataBinding.recyclerView.run {
            layoutManager = LinearLayoutManager(appContext, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 2).toInt()))
            adapter = appPicAdapter
        }

        mDataBinding.recyclerView2.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 1).toInt()))
            adapter = localGameAdapter
        }


        appPicAdapter.setOnItemClickListener { adapter, view, position ->
            val appletsInfo = adapter.data[position] as AppletsInfo
            ModActivityPicDown.start(appContext, appletsInfo.pic2)
        }

        localGameAdapter.setOnItemClickListener { adapter, view, position ->
            val localGameInfo = adapter.data[position] as LocalGameInfo
            ModActivityGameBrowser.start(appContext, localGameInfo.url)
        }

    }
    override fun initData() {
        mViewModel.postDataAppApi268()
    }

    override fun createObserver() {
        mViewModel.postDataAppApi268Result.observe(this) { it ->
            parseState(it, {
                it?.let {
                    Logs.e("it.marketjson.list_data:",GsonUtils.toJson(it.marketjson.list_data))
                    appPicAdapter.setList(it.marketjson.list_data)
                }
            }, {
                Toaster.show(it.errorMsg)
            })
        }
    }

    override fun onRightClick(view: TitleBar) {
    }

    override fun lazyLoadData() {

    }

    override fun onNetworkStateChanged(it: NetState) {
    }

    private fun getLocalGames(){
        for (i in 0 until 3) {
            val localGameInfo = LocalGameInfo()
            when (i) {
                0 -> {
                    localGameInfo.pic = ResourceUtils.getDrawable(R.mipmap.mod_moyu_game1)
                    localGameInfo.url = GAME_CAICAIDIANYINGMING
                }
                1 -> {
                    localGameInfo.pic = ResourceUtils.getDrawable(R.mipmap.mod_moyu_game2)
                    localGameInfo.url = GAME_KAIXINYIXIA
                }
//                2 -> {
//                    localGameInfo.pic = ResourceUtils.getDrawable(R.mipmap.mod_moyu_game3)
//                    localGameInfo.url = GAME_JIANFENGCHAZHEN
//                }
                2 -> {
                    localGameInfo.pic = ResourceUtils.getDrawable(R.mipmap.mod_moyu_game4)
                    localGameInfo.url = GAME_CAIMIYU
                }
            }
            localGameList.add(localGameInfo)
        }
    }

    /**********************************************Click**************************************************/
    inner class ProxyClick {

        fun game1(){
            ModActivityGameBrowser.start(appContext,GAME_CAICAIDIANYINGMING)
        }

        fun game2(){
            ModActivityGameBrowser.start(appContext, GAME_KAIXINYIXIA)
        }

        fun game3(){
            ModActivityGameBrowser.start(appContext, GAME_JIANFENGCHAZHEN)
        }

        fun game4(){
            ModActivityGameBrowser.start(appContext, GAME_CAIMIYU)
        }


    }

}