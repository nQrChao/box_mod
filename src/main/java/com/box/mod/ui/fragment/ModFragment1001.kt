package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.action.StatusAction
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appContext
import com.box.common.data.model.ModDataBean
import com.box.common.network.apiService
import com.box.common.ui.activity.CommonActivityRichText
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.ui.layout.StatusLayout
import com.box.common.utils.logsE
import com.box.mod.BR.modData
import com.box.mod.R
import com.box.mod.databinding.ModFragment1001Binding
import com.box.mod.databinding.ModItemNewsBinding
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder


class ModFragment1001 : BaseTitleBarFragment<ModFragment1001.Model, ModFragment1001Binding>(),
    StatusAction {
    override val mViewModel: Model by viewModels()
    override fun layoutId(): Int = R.layout.mod_fragment_1001

    companion object {
        fun newInstance(): ModFragment1001 {
            return ModFragment1001()
        }
    }

    private val pageSize = 10
    private var currentPage = 1
    var clickData = ModDataBean()
    var listData: MutableList<ModDataBean> = mutableListOf()
    var listAdapter = ItemNewsAdapter(listData)


    /**
     * 懒加载
     */
    override fun lazyLoadData() {
        showLoading()
        mViewModel.getNewsListData(currentPage, pageSize)
    }

    /**
     * 加载状态
     */
    override fun getStatusLayout(): StatusLayout {
        return mDataBinding.statusLoading
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
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = listAdapter
        }
        listAdapter.setOnItemClickListener { adapter, view, position ->
            clickData = adapter.data[position] as ModDataBean
            mViewModel.getNewsDetailData(clickData.id)
        }

        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.getNewsListData(currentPage, pageSize)
            }
            setOnLoadMoreListener {
                currentPage++
                mViewModel.getNewsListData(currentPage, pageSize)
            }
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    override fun createObserver() {
        mViewModel.newsListResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (currentPage == 1) { // 下拉刷新
                        mDataBinding.refreshLayout.finishRefresh()
                        // 如果是排序后没有数据，也要清空列表
                        if (data.isNullOrEmpty()) {
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else {
                            listAdapter.setList(data)
                            mDataBinding.refreshLayout.resetNoMoreData()
                        }
                    } else { // 场景：上拉加载更多
                        if (data.isNullOrEmpty()) {
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                            return@parseModStateWithMsg
                        }
                        mDataBinding.refreshLayout.finishLoadMore()
                        listAdapter.addData(data)
                        listAdapter.notifyDataSetChanged()
                    }
                },
                onError = {
                    if (currentPage > 1) {
                        currentPage--
                        mDataBinding.refreshLayout.finishLoadMore(false)
                    } else {
                        mDataBinding.refreshLayout.finishRefresh(false)
                    }
                    Toaster.show(it.msg)
                }
            )
            showComplete()
        }

        mViewModel.newsDetailResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    CommonActivityRichText.start(appContext, clickData.title,data?.content ?: "")
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

    }


    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun confirm() {

        }

    }


    /**********************************************Adapter**************************************************/
    class ItemNewsAdapter constructor(list: MutableList<ModDataBean>) :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemNewsBinding>>(
            R.layout.mod_item_news, list
        ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemNewsBinding>, item: ModDataBean) {
            holder.dataBinding?.setVariable(modData, item)
        }
    }

    class ItemCreateDiffCallback : DiffUtil.ItemCallback<ModDataBean>() {
        override fun areItemsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem == newItem
        }
    }


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "") {
        var pic = IntObservableField(0)
        var newsListResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        var newsDetailResult = MutableLiveData<ModResultStateWithMsg<ModDataBean>>()

        fun getNewsListData(pageNum: Int, pageSize: Int) {
            modRequestWithMsg({
                apiService.getNewsList(pageNum, pageSize)
            }, newsListResult)
        }

        fun getNewsDetailData(id: Int) {
            modRequestWithMsg({
                apiService.getNewsDetailById(id)
            }, newsDetailResult)
        }

    }


}


