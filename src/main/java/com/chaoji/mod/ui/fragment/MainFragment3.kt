package com.chaoji.mod.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.base.base.action.HandlerAction
import com.chaoji.base.base.action.StatusAction
import com.chaoji.base.base.fragment.BaseTitleBarFragment
import com.chaoji.base.ext.parseModState
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.data.model.AppletsLunTan
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.im.ui.layout.StatusLayout
import com.chaoji.mod.R
import com.chaoji.mod.databinding.MainFragment3Binding
import com.chaoji.mod.databinding.ModItemAppletsLuntan1Binding
import com.chaoji.mod.databinding.ModItemAppletsLuntan2Binding
import com.chaoji.mod.databinding.ModItemAppletsLuntan3Binding
import com.chaoji.mod.databinding.ModItemAppletsLuntan4Binding
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar

class MainFragment3 : BaseTitleBarFragment<MainFragment3Model, MainFragment3Binding>(), HandlerAction, StatusAction {
    val apiNumber = "501"
    var appList: MutableList<AppletsLunTan> = mutableListOf()
    var appListAdapter = AppletsLunTanAdapter(appList)
    override fun layoutId(): Int = R.layout.main_fragment_3

    companion object {
        fun newInstance(): MainFragment3 {
            return MainFragment3()
        }
    }

    override fun lazyLoadData() {
        mViewModel.updateConversation()
        mViewModel.postDataAppApiByDataId(apiNumber)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }
        showLoading()

        appListAdapter.setDiffCallback(AppletsInfoLunTanDiffCallback())

        val manager = GridLayoutManager(context, 2)
        mDataBinding.recyclerView.layoutManager = manager
        mDataBinding.recyclerView.adapter = appListAdapter
        mDataBinding.recyclerView.addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 7).toInt()))
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (appListAdapter.getItemViewType(position)) {
                    AppletsLunTan.TYPE_GRID_ITEM -> 1
                    else -> 2
                }
            }
        }

        appListAdapter.setOnItemClickListener { adapter, view, position ->
            val applets = adapter.data[position] as AppletsLunTan
            if (applets.redirect.isNotEmpty()) {
                CommonActivityBrowser.start(appContext, applets.redirect)
            }
        }


    }

    override fun initData() {
    }

    override fun createObserver() {
        mViewModel.postDataAppApiByDataIdResult.observe(this) { it ->
            parseModState(it, {
                it?.let {
                    val apiList = it.marketjson.list_data
                    if (apiList.isNullOrEmpty()) {
                        appListAdapter.setList(emptyList())
                        return@parseModState
                    }
                    val finalList = mutableListOf<AppletsLunTan>()
                    apiList.forEachIndexed { index, item ->
                        if (index == 0) {
                            finalList.add(
                                AppletsLunTan(
                                    title = "热游攻略",
                                    viewType = AppletsLunTan.TYPE_TITLE
                                )
                            )
                        }
                        when (index) {
//                            0 -> {
//                                item.viewType = AppletsLunTan.TYPE_LARGE_IMAGE
//                            }

                            3, 4 -> {
                                item.viewType = AppletsLunTan.TYPE_GRID_ITEM
                            }

                            else -> {
                                item.viewType = AppletsLunTan.TYPE_NORMAL
                            }
                        }
                        finalList.add(item)
                    }

                    appListAdapter.setList(finalList)
                }
                showComplete()
            }, {
                Toaster.show(it.errorLog)
            })
        }
    }

    override fun onRightClick(view: TitleBar) {
    }


    override fun onNetworkStateChanged(it: NetState) {
    }

    class AppletsLunTanAdapter(data: MutableList<AppletsLunTan>) :
        BaseMultiItemQuickAdapter<AppletsLunTan, BaseDataBindingHolder<*>>(data) {

        init {
            // 绑定类型和布局
            addItemType(AppletsLunTan.TYPE_NORMAL, R.layout.mod_item_applets_luntan_1)
            addItemType(AppletsLunTan.TYPE_LARGE_IMAGE, R.layout.mod_item_applets_luntan_2)
            addItemType(AppletsLunTan.TYPE_GRID_ITEM, R.layout.mod_item_applets_luntan_3)
            addItemType(AppletsLunTan.TYPE_TITLE, R.layout.mod_item_applets_luntan_4)
        }

        override fun convert(holder: BaseDataBindingHolder<*>, item: AppletsLunTan) {
            // 根据 holder 的类型来获取对应的 DataBinding
            when (holder.itemViewType) {
                AppletsLunTan.TYPE_NORMAL -> {
                    val binding = holder.dataBinding as? ModItemAppletsLuntan1Binding
                    binding?.let {
                        it.appletInfoLunTan = item
                        it.executePendingBindings()
                    }
                }

                AppletsLunTan.TYPE_LARGE_IMAGE -> {
                    val binding = holder.dataBinding as? ModItemAppletsLuntan2Binding
                    binding?.let {
                        it.appletInfoLunTan = item
                        it.executePendingBindings()
                    }
                }

                AppletsLunTan.TYPE_GRID_ITEM -> {
                    val binding = holder.dataBinding as? ModItemAppletsLuntan3Binding
                    binding?.let {
                        it.appletInfoLunTan = item
                        it.executePendingBindings()
                    }
                }

                AppletsLunTan.TYPE_TITLE -> {
                    val binding = holder.dataBinding as? ModItemAppletsLuntan4Binding
                    binding?.let {
                        it.titleText = item.title
                        it.executePendingBindings()
                    }
                }
            }
        }
    }

    class AppletsInfoLunTanDiffCallback : DiffUtil.ItemCallback<AppletsLunTan>() {
        override fun areItemsTheSame(oldItem: AppletsLunTan, newItem: AppletsLunTan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppletsLunTan, newItem: AppletsLunTan): Boolean {
            return oldItem == newItem
        }
    }

    /**********************************************Click**************************************************/
    inner class ProxyClick {


    }

    override fun getStatusLayout(): StatusLayout? {
        return mDataBinding.hlHint
    }


}