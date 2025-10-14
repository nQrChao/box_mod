package com.chaoji.mod.ui.fragment.fragment1

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.chaoji.base.base.action.StatusAction

import com.chaoji.base.base.fragment.BaseTitleBarFragment
import com.chaoji.base.ext.parseModState
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.data.model.AppletsLeYuan
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.im.ui.layout.StatusLayout
import com.chaoji.im.utils.ScrollableContainer
import com.chaoji.mod.R
import com.chaoji.mod.databinding.FragmentNavigation13Binding
import com.chaoji.mod.ui.adapter.AppletsLeYuanAdapter
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar

class FragmentNavigation13 : BaseTitleBarFragment<Navigation13Model, FragmentNavigation13Binding>(), StatusAction, ScrollableContainer {
    var appList: MutableList<AppletsLeYuan> = mutableListOf()
    var appListAdapter = AppletsLeYuanAdapter(appList)

    override fun layoutId(): Int = R.layout.fragment_navigation1_3

    companion object {
        fun newInstance(): FragmentNavigation13 {
            return FragmentNavigation13()
        }
    }


    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        showLoading()
        immersionBar {
            statusBarDarkFont(true)
            init()
        }
        appListAdapter.setDiffCallback(AppletsInfoLeYuanDiffCallback())

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = appListAdapter
        }


        appListAdapter.addChildClickViewIds(R.id.dianzanLayout)
        appListAdapter.setOnItemClickListener { adapter, view, position ->
            val applets = adapter.data[position] as AppletsLeYuan
            CommonActivityBrowser.start(appContext, applets.redirect)
        }

        appListAdapter.setOnItemChildClickListener() { adapter, view, position ->

            if (view.id == R.id.dianzanLayout) {
                val applets = adapter.data[position] as AppletsLeYuan
                applets.select = !applets.select
                adapter.notifyItemChanged(position, "LIKE_UPDATE")
            }


//            if (view.id == R.id.dianzanLayout) {
//                val applets = adapter.data[position] as AppletsInfoLeYuan
//                applets.select = !applets.select
//                adapter.notifyItemChanged(position)
//            }
        }

    }

    override fun initData() {

    }

    override fun createObserver() {
        mViewModel.postDataAppApiByDataIdResult.observe(this) { it ->
            parseModState(it, {
                it?.let {
                    appList = it.marketjson.list_data
                    appListAdapter.setDiffNewData(appList)
                }

            }, {
                Toaster.show(it.errorLog)
            })
            showComplete()
        }


    }

    override fun lazyLoadData() {
        mViewModel.updateConversation()
        mViewModel.postDataAppApiByDataId("322")
    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun search() {
        }

    }


    class AppletsInfoLeYuanDiffCallback : DiffUtil.ItemCallback<AppletsLeYuan>() {
        override fun areItemsTheSame(oldItem: AppletsLeYuan, newItem: AppletsLeYuan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppletsLeYuan, newItem: AppletsLeYuan): Boolean {
            return oldItem == newItem
        }
    }

    override fun getStatusLayout(): StatusLayout? {
        return mDataBinding.hlHint
    }

    override fun getScrollableView(): View? {
        return mDataBinding.recyclerView
    }


}



