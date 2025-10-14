package com.box.mod.ui.activity.modmain.mod5

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.action.HandlerAction
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.ext.parseState
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.data.model.ModLocalAppletsInfo
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.other.hjq.toast.Toaster
import com.box.mod.R
import com.box.mod.databinding.Mod5Fragment3Binding
import com.box.mod.ui.adapter.LocalAppletInfoAdapter
import com.box.other.immersionbar.immersionBar


class Mod5Fragment3 : BaseTitleBarFragment<Mod5Fragment3Model, Mod5Fragment3Binding>(), HandlerAction {
    var localAppLetsList: MutableList<ModLocalAppletsInfo> = mutableListOf()
    var localAppLetsAdapter = LocalAppletInfoAdapter(localAppLetsList)
    override fun layoutId(): Int = R.layout.mod5_fragment_3

    companion object {
        fun newInstance(): Mod5Fragment3 {
            return Mod5Fragment3()
        }
    }


    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()

        immersionBar {
            statusBarDarkFont(true)
            init()
        }

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 2).toInt()))
            adapter = localAppLetsAdapter
        }
        localAppLetsAdapter.addChildClickViewIds(R.id.pic1)
        localAppLetsAdapter.addChildClickViewIds(R.id.pic2)
        localAppLetsAdapter.addChildClickViewIds(R.id.pic3)
        localAppLetsAdapter.addChildClickViewIds(R.id.pic4)
        localAppLetsAdapter.setOnItemChildClickListener { adapter, view, position ->
            val localApplets = adapter.data[position] as ModLocalAppletsInfo
            when (view.id) {
                R.id.pic1 -> {
                    CommonActivityBrowser.start(appContext,localApplets.redirect1)
                }
                R.id.pic2 -> {
                    CommonActivityBrowser.start(appContext,localApplets.redirect2)
                }
                R.id.pic3 -> {
                    CommonActivityBrowser.start(appContext,localApplets.redirect3)
                }
                R.id.pic4 -> {
                    CommonActivityBrowser.start(appContext,localApplets.redirect4)
                }
            }
        }

    }

    override fun initData() {
        mViewModel.postDataAppApi259()
    }

    override fun createObserver() {
        mViewModel.postDataAppApi259Result.observe(this) { it ->
            parseState(it, {
                it?.let {
                    mViewModel.setLocalAppletsList(it.marketjson.list_data)
                }

            }, {
                Toaster.show(it.errorMsg)
            })
        }

        mViewModel.localAppletsList.observe(this){
            localAppLetsAdapter.setList(it)
        }

    }


    override fun lazyLoadData() {
    }

    override fun onNetworkStateChanged(it: NetState) {

    }



    inner class ProxyClick {


    }


}