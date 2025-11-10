package com.box.mod.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.angcyo.dsladapter.L.it
import com.box.base.base.action.StatusAction
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.utils.mmkv.MMKVConfig
import com.box.common.appContext
import com.box.common.data.model.ModDataBean
import com.box.common.network.apiService
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.ui.layout.StatusLayout
import com.box.common.utils.ext.logsE
import com.box.mod.BR.modData
import com.box.mod.R
import com.box.mod.databinding.ModFragmentGameRankListBinding
import com.box.mod.databinding.ModItemRankBinding
import com.box.mod.ui.activity.ModActivityLogin
import com.box.mod.ui.activity.ModActivityShouCang
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder


class ModFragmentGameRankList :
    BaseTitleBarFragment<ModFragmentGameRankList.Model, ModFragmentGameRankListBinding>(), StatusAction {
    private var type = 0
    private val pageSize = 10
    private var currentPage = 1
    var rankShoucangList: MutableList<ModDataBean> = mutableListOf()
    private val rankListAdapter = ModGameRankAdapter()


    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_game_rank_list

    companion object {
        fun newInstance(): ModFragmentGameRankList {
            return ModFragmentGameRankList()
        }
    }
    override fun lazyLoadData() {
        mViewModel.getRoleTypeData(currentPage,pageSize,type)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        showLoading()
        rankShoucangList = MMKVConfig.getGameRank()

        immersionBar {
            statusBarDarkFont(true)
            init()
        }

        rankListAdapter.setDiffCallback(ModGameRankDiffCallback())

        mDataBinding.tab.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            mViewModel.isSelect.set(toIndex)
            type = when (toIndex) {
                0 -> 0
                1 -> 1
                else -> 0
            }
            mDataBinding.recyclerView.scrollToPosition(0)
            mDataBinding.root.postDelayed({
                mDataBinding.refreshLayout.autoRefresh()
            }, 500) // 延迟500毫秒
        }

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = rankListAdapter
        }


        rankListAdapter.setOnItemClickListener { adapter, view, position ->
            val modDataBean = adapter.data[position] as ModDataBean

        }
        rankListAdapter.addChildClickViewIds(R.id.shoucang)
        rankListAdapter.setOnItemChildClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            if (view.id == R.id.shoucang) {
                if (clickedItem.isShouCang) {
                    clickedItem.isShouCang = false
                    MMKVConfig.removeGameRankList(clickedItem)
                    Toaster.show("已取消收藏")
                } else {
                    clickedItem.isShouCang = true
                    MMKVConfig.addGameRankList(clickedItem)
                    Toaster.show("收藏成功")
                }

                adapter.notifyItemChanged(position, "SHOUCANG_UPDATE")

            }
        }


        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.getRoleTypeData(currentPage, pageSize, type)
            }

            setOnLoadMoreListener {
                currentPage++
                mViewModel.getRoleTypeData(currentPage, pageSize, type)
            }
        }

    }


    override fun createObserver() {
        mViewModel.gameRankResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data.isNullOrEmpty()) {
                        if (currentPage == 1) { // 刷新时没有数据
                            mDataBinding.refreshLayout.finishRefresh()
                            rankListAdapter.setList(mutableListOf()) // 清空列表
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else { // 加载更多时没有数据
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        }
                        return@parseModStateWithMsg // 提前退出
                    }

                    val shoucangNames = MMKVConfig.gameRankList.map { it.name }.toSet()
                    data.forEach { item ->
                        item.isShouCang = item.name in shoucangNames
                    }

                    if (currentPage == 1) {
                        mDataBinding.refreshLayout.finishRefresh()
                        data.forEachIndexed { index, item ->
                            item.rank = index + 1
                        }
                        rankListAdapter.setList(data)
                        mDataBinding.refreshLayout.resetNoMoreData()
                    } else {
                        mDataBinding.refreshLayout.finishLoadMore()
                        val currentItemCount = rankListAdapter.data.size
                        data.forEachIndexed { index, item ->
                            item.rank = currentItemCount + index + 1
                        }
                        rankListAdapter.addData(data)
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
    }


    override fun onNetworkStateChanged(it: NetState) {
    }

    /**
     * 加载状态
     */
    override fun getStatusLayout(): StatusLayout {
        return mDataBinding.statusLoading
    }

    override fun onResume() {
        super.onResume()
        val currentApiList = rankListAdapter.data
        if (currentApiList.isEmpty()) {
            return
        }
        val shoucangNamesSet = MMKVConfig.gameRankList.map { it.name }.toSet()
        val updateList = currentApiList.map { item ->
            val isShouCang = item.name in shoucangNamesSet
            if (item.isShouCang != isShouCang) {
                item.copy(isShouCang = isShouCang)
            } else {
                item
            }
        }
        rankListAdapter.setDiffNewData(updateList as MutableList<ModDataBean>?)
    }


    override fun onRightClick(view: TitleBar) {
        super.onRightClick(view)
        if (isLogin()) {
            ModActivityShouCang.start(appContext,1)
        }else{
            Toaster.show("请先登录")
            ModActivityLogin.start(appContext)
        }

    }

    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun confirm() {

        }

    }

    /**********************************************Adapter**************************************************/
    class ModGameRankAdapter : BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemRankBinding>>(
            R.layout.mod_item_rank
        ) {

        override fun convert(holder: BaseDataBindingHolder<ModItemRankBinding>, item: ModDataBean) {
            // 绑定逻辑保持不变
            holder.dataBinding?.let {
                it.setVariable(modData, item)
                it.executePendingBindings()
                it.topText.text = item.rank.toString()
            }
            if (item.rank < 4) {
                holder.dataBinding?.topText?.visibility = View.GONE
                holder.dataBinding?.topIcon?.visibility = View.VISIBLE
            } else {
                holder.dataBinding?.topIcon?.visibility = View.GONE
                holder.dataBinding?.topText?.visibility = View.VISIBLE
            }
        }
        override fun onBindViewHolder(
            holder: BaseDataBindingHolder<ModItemRankBinding>,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
                return
            }
            if (payloads.any { it == "SHOUCANG_UPDATE" }) {
                val item = getItem(position)
                holder.dataBinding?.let {
                    it.setVariable(modData, item)
                    it.executePendingBindings()
                    it.topText.text = item.rank.toString()
                }
                if (item.rank < 4) {
                    holder.dataBinding?.topText?.visibility = View.GONE
                    holder.dataBinding?.topIcon?.visibility = View.VISIBLE
                } else {
                    holder.dataBinding?.topIcon?.visibility = View.GONE
                    holder.dataBinding?.topText?.visibility = View.VISIBLE
                }
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    class ModGameRankDiffCallback : DiffUtil.ItemCallback<ModDataBean>() {
        override fun areItemsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem.name == newItem.name
        }
        override fun areContentsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem == newItem
        }
    }


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "小游戏排行榜") {
        var pic = IntObservableField(0)
        var isSelect = IntObservableField(0)

        var gameRankResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()

        fun getRoleTypeData(pageNum: Int, pageSize: Int, type: Int) {
            modRequestWithMsg({
                apiService.getModGameRankList(pageNum, pageSize, type)
            }, gameRankResult)
        }


    }


}


