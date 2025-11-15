package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
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
import com.box.common.appViewModel
import com.box.common.data.model.ModDataBean
import com.box.common.data.model.ModUserInfo
import com.box.common.eventViewModel
import com.box.common.network.apiService
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.common.ui.activity.CommonActivityRichText
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.ui.layout.StatusLayout
import com.box.common.utils.ext.logsE
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.BR.modData
import com.box.mod.R
import com.box.mod.databinding.ModFragment1Binding
import com.box.mod.databinding.ModItemGameEventBinding
import com.box.mod.ui.activity.ModActivityLogin
import com.box.mod.ui.activity.ModActivityMyShouCang
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder


class ModFragment1 : BaseTitleBarFragment<ModFragment1.Model, ModFragment1Binding>(), StatusAction {
    override val mViewModel: Model by viewModels()
    override fun layoutId(): Int = R.layout.mod_fragment_1

    companion object {
        fun newInstance(): ModFragment1 {
            return ModFragment1()
        }
    }

    private val pageSize = 10
    private var currentPage = 1
    var clickData = ModDataBean()
    var gameEventList: MutableList<ModDataBean> = mutableListOf()
    var gameEventAdapter = GameEventAdapter(gameEventList)

    /**
     * 懒加载
     */
    override fun lazyLoadData() {
        showLoading()
        mViewModel.getGameEventListData(currentPage, pageSize)
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
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }


        gameEventAdapter.setDiffCallback(GameEventDiffCallback())
        mDataBinding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 1)
            isNestedScrollingEnabled = false  // ✅ 关键
            overScrollMode = View.OVER_SCROLL_NEVER
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = gameEventAdapter
        }
        gameEventAdapter.addChildClickViewIds(R.id.button)
        gameEventAdapter.setOnItemClickListener { adapter, view, position ->
            clickData = adapter.data[position] as ModDataBean
            mViewModel.getEventDetailData(clickData.id)
        }

        gameEventAdapter.setOnItemChildClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            if (view.id == R.id.button) {
                if (clickedItem.isShouCang) {
                    clickedItem.isShouCang = false
                    MMKVConfig.removeGameEventList(clickedItem)
                } else {
                    clickedItem.isShouCang = true
                    Toaster.showReSuccess("1")
                    MMKVConfig.addGameEventList(clickedItem)
                }

                adapter.notifyItemChanged(position, "SHOUCANG_UPDATE")

            }

        }

        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.getGameEventListData(currentPage, pageSize)
            }
            setOnLoadMoreListener {
                currentPage++
                mViewModel.getGameEventListData(currentPage, pageSize)
            }
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    override fun createObserver() {
        mViewModel.gameEventListResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))

                    if (data.isNullOrEmpty()) {
                        if (currentPage == 1) {
                            mDataBinding.refreshLayout.finishRefresh()
                            gameEventAdapter.setList(mutableListOf()) // 清空列表
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else { // 加载更多时没有数据
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        }
                        return@parseModStateWithMsg
                    }

                    val shoucangId = MMKVConfig.gameEventList.map { it.id }.toSet()
                    data.forEach { item ->
                        item.isShouCang = item.id in shoucangId
                    }


                    if (currentPage == 1) { // 下拉刷新
                        mDataBinding.refreshLayout.finishRefresh()
                        // 如果是排序后没有数据，也要清空列表
                        if (data.isEmpty()) {
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else {
                            gameEventAdapter.setList(data)
                            mDataBinding.refreshLayout.resetNoMoreData()
                        }
                    } else { // 场景：上拉加载更多
                        if (data.isEmpty()) {
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                            return@parseModStateWithMsg
                        }
                        mDataBinding.refreshLayout.finishLoadMore()
                        gameEventAdapter.addData(data)
                        gameEventAdapter.notifyDataSetChanged()
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

        mViewModel.gameEventDetailResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    CommonActivityRichText.start(appContext, clickData.title, data?.content ?: "",data?.views?:"-1")
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        appViewModel.modUserInfo.observe(this) {
            mViewModel.modUserInfo.value = it
        }

        MMKVConfig.userInfo?.let { savedUser ->
            eventViewModel.isLogin.value= true
            appViewModel.modUserInfo.value = savedUser
        }

    }


    override fun onNetworkStateChanged(it: NetState) {
    }


    override fun isStatusBarEnabled(): Boolean {
        return true
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun user() {
            if (!isLogin()) {
                ModActivityLogin.start(appContext)
            }
        }

        fun img1() {
            val targetView = mDataBinding.saishiText
            val scrollView = mDataBinding.nestedScrollView
            val yPosition = targetView.top
            scrollView.smoothScrollTo(0, yPosition)
        }

        fun img2() {
            eventViewModel.setMainCurrentItem.value = 1
        }

        fun img3() {
            eventViewModel.setMainCurrentItem.value = 2
        }

        fun img4() {
            eventViewModel.setMainCurrentItem.value = 3
        }

        fun shoucang() {

            if (isLogin()) {
                ModActivityMyShouCang.start(appContext)
            }else{
                Toaster.show("请先登录")
                ModActivityLogin.start(appContext)
            }

        }

        fun test() {
            val assetGame = "file:///android_asset/out/gomoku.html"
            CommonActivityBrowser.start(appContext,assetGame)
        }

        fun confirm() {

        }

    }


    /**********************************************Adapter**************************************************/
    class GameEventAdapter constructor(list: MutableList<ModDataBean>) :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemGameEventBinding>>(
            R.layout.mod_item_game_event, list
        ) {
        override fun convert(
            holder: BaseDataBindingHolder<ModItemGameEventBinding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.setVariable(modData, item)
        }

        override fun onBindViewHolder(
            holder: BaseDataBindingHolder<ModItemGameEventBinding>,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
                return
            }
            if (payloads.any { it == "SHOUCANG_UPDATE" }) {
                val item = getItem(position)
                holder.dataBinding?.setVariable(modData, item)
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    class GameEventDiffCallback : DiffUtil.ItemCallback<ModDataBean>() {
        override fun areItemsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem == newItem
        }
    }


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "　　　　　") {
        var modUserInfo = MutableLiveData<ModUserInfo>()

        var pic = IntObservableField(0)
        var gameEventListResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        var gameEventDetailResult = MutableLiveData<ModResultStateWithMsg<ModDataBean>>()

        fun getGameEventListData(pageNum: Int, pageSize: Int) {
            modRequestWithMsg({
                apiService.getNewsList(pageNum, pageSize)
            }, gameEventListResult)
        }

        fun getEventDetailData(id: Int) {
            modRequestWithMsg({
                apiService.getNewsDetailById(id)
            }, gameEventDetailResult)
        }

    }


}


