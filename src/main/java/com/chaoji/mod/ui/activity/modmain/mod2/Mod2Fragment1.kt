package com.chaoji.mod.ui.activity.modmain.mod2

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager

import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.base.base.fragment.BaseTitleBarFragment
import com.chaoji.base.ext.parseState
import com.chaoji.base.network.NetState
import com.chaoji.im.GAME_KAIXINYIXIA
import com.chaoji.im.appContext
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.mod.ui.adapter.AppletDescAdapter
import com.chaoji.mod.ui.adapter.AppletsInfoDiffCallback
import com.chaoji.im.ui.xpop.XPopupDrawerAiList
import com.chaoji.mod.R
import com.chaoji.mod.databinding.FragmentNavigation1Binding
import com.chaoji.mod.databinding.Mod2Fragment1Binding
import com.chaoji.mod.databinding.Mod5Fragment1Binding
import com.chaoji.mod.ui.activity.game.ModActivityGameBrowser
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.other.xpopup.XPopup
import com.chaoji.common.R as RC

class Mod2Fragment1 : BaseTitleBarFragment<Mod2Fragment1Model, Mod2Fragment1Binding>() {
    var appDescList: MutableList<AppletsInfo> = mutableListOf()
    var appDescListAdapter = AppletDescAdapter(appDescList)

    override fun layoutId(): Int = R.layout.mod2_fragment_1

    companion object {
        fun newInstance(): Mod2Fragment1 {
            return Mod2Fragment1()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.viewmodel = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }
        appDescListAdapter.setDiffCallback(AppletsInfoDiffCallback())
        mDataBinding.recyclerView.run {
            layoutManager = object : GridLayoutManager(context, 2) {
                override fun isLayoutRTL(): Boolean {
                    return false
                }
            }
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 4).toInt()))
            adapter = appDescListAdapter
        }

        appDescListAdapter.setOnItemClickListener { adapter, view, position ->
            val appletsInfo = adapter.data[position] as AppletsInfo
            CommonActivityBrowser.start(appContext,appletsInfo.redirect)
        }




    }

    override fun initData() {

    }

    override fun createObserver() {
        mViewModel.postDataAppApi85Result.observe(this) { it ->
            parseState(it, {
                it?.let {
                    appDescListAdapter.setDiffNewData(it.marketjson.list_data)
                }

            }, {
                Toaster.show(it.errorMsg)
            })
        }

    }

    inner class ProxyClick {
        fun game1() {
        }

        fun game2() {
        }

        fun game3() {
            ModActivityGameBrowser.start(appContext, GAME_KAIXINYIXIA)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun lazyLoadData() {
        mViewModel.postDataAppApi85()
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

                }, {

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