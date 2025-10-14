package com.chaoji.mod.ui.activity.modmain.mod5

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager

import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.base.base.fragment.BaseTitleBarFragment
import com.chaoji.base.ext.parseState
import com.chaoji.base.network.NetState
import com.chaoji.im.GAME_CAICAIDIANYINGMING
import com.chaoji.im.GAME_CAIMIYU
import com.chaoji.im.GAME_KAIXINYIXIA
import com.chaoji.im.appContext
import com.chaoji.im.sdk.ImSDK
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.sdk.eventViewModel
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.im.ui.xpop.XPopupDrawerAiList
import com.chaoji.mod.ui.adapter.AppletDescAdapter
import com.chaoji.mod.ui.adapter.AppletsAdapter
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.other.xpopup.XPopup
import com.chaoji.mod.ui.activity.game.ModActivityGameBrowser
import com.chaoji.mod.R
import com.chaoji.mod.databinding.Mod5Fragment1Binding
import com.chaoji.common.R as RC


class Mod5Fragment1 : BaseTitleBarFragment<Mod5Fragment1Model, Mod5Fragment1Binding>() {
    var appList: MutableList<AppletsInfo> = mutableListOf()
    var appDescList: MutableList<AppletsInfo> = mutableListOf()
    var appListAdapter = AppletsAdapter(appList)
    var appDescListAdapter = AppletDescAdapter(appDescList)

    override fun layoutId(): Int = R.layout.mod5_fragment_1

    companion object {
        fun newInstance(): Mod5Fragment1 {
            return Mod5Fragment1()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView(savedInstanceState: Bundle?) {
        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }
        mDataBinding.viewmodel = mViewModel
        mDataBinding.click = ProxyClick()

        mDataBinding.recyclerView.run {
            layoutManager = object : GridLayoutManager(context, 4) {
                override fun isLayoutRTL(): Boolean {
                    return false
                }
            }.apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position < 2 * 4) 1 else spanCount
                    }
                }
            }
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 4).toInt()))
            adapter = appListAdapter
        }

        mDataBinding.recyclerView2.run {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 2).toInt()))
            adapter = appDescListAdapter
        }


        appListAdapter.setOnItemClickListener { adapter, view, position ->
            val appletsInfo = adapter.data[position] as AppletsInfo
            ImSDK.eventViewModelInstance.setMainCurrentItem.value = 1
            ImSDK.eventViewModelInstance.setNavigation2Info.value = appletsInfo
            ImSDK.eventViewModelInstance.setNavigation2InfoIndex.value = position
        }

        appDescListAdapter.setOnItemClickListener { adapter, view, position ->
            val appletsInfo = adapter.data[position] as AppletsInfo
            CommonActivityBrowser.start(appContext,appletsInfo.redirect)
        }


    }

    override fun initData() {
        mViewModel.postDataAppApi248()
        mViewModel.postDataAppApi249()
    }

    override fun createObserver() {
        mViewModel.postDataAppApi248Result.observe(this) { it ->
            parseState(it, {
                it?.let {
                    if(it.marketjson.list_data.isEmpty()){
                        Toaster.show("数据为空")
                    }else{
                        appListAdapter.setList(it.marketjson.list_data)
                        appViewModel.appLetsList.value = it.marketjson.list_data
                        eventViewModel.setDefaultGameId.postValue(it.marketjson.list_data[0].id.toInt())
                    }

                }

            }, {
                Toaster.show(it.errorMsg)
            })
        }
        mViewModel.postDataAppApi249Result.observe(this) { it ->
            parseState(it, {
                it?.let {
                    appDescListAdapter.setList(it.marketjson.list_data)
                }

            }, {
                Toaster.show(it.errorMsg)
            })
        }
    }

    inner class ProxyClick {
        fun game1() {
            ModActivityGameBrowser.start(appContext,GAME_CAICAIDIANYINGMING)
        }

        fun game2() {
            ModActivityGameBrowser.start(appContext, GAME_CAIMIYU)
        }

        fun game3() {
            ModActivityGameBrowser.start(appContext, GAME_KAIXINYIXIA)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun lazyLoadData() {
    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    override fun onLeftClick(view: TitleBar) {
        XPopup.Builder(context)
            .isViewMode(true)
            .isDestroyOnDismiss(true)
            .hasStatusBar(false)
            .animationDuration(5)
            .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
            .hasNavigationBar(false)
            .asCustom(activity?.let {
                XPopupDrawerAiList(it, {
                    immersionBar {
                        titleBar(mDataBinding.titleBar)
                        statusBarDarkFont(false)
                        init()
                    }
                }, {
                    immersionBar {
                        titleBar(mDataBinding.titleBar)
                        statusBarDarkFont(true)
                        init()
                    }
                })
            })
            .show()

    }

    override fun onRightClick(view: TitleBar) {
        XPopup.Builder(context)
            .isDestroyOnDismiss(true)
            .hasStatusBar(true)
            .animationDuration(5)
            .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
            .isLightStatusBar(true)
            .hasNavigationBar(true)
            .asConfirm(
                "提示", "确定要删除聊天记录？删除后不可恢复",
                "取消", "确定",
                {
                    mViewModel.curChat.value?.let { it1 -> mViewModel.delChatMessage(it1.id) }
                }, null, false, RC.layout.xpopup_confirm
            ).show()
    }


}