package com.box.mod.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
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
import com.box.common.utils.ext.logsE
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.R
import com.box.mod.databinding.ModFragmentGameZixunBinding
import com.box.mod.ui.fragment.ModFragmentGuJiaGuangchang.GameEventAdapter
import com.box.mod.ui.fragment.ModFragmentMain.GameEventDiffCallback
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar


class ModFragmentGameZiXun :
    BaseTitleBarFragment<ModFragmentGameZiXun.Model, ModFragmentGameZixunBinding>(), StatusAction {
    private var type = 0
    private val pageSize = 10
    private var currentPage = 1
    var clickData = ModDataBean()
    var gameEventList: MutableList<ModDataBean> = mutableListOf()

    var gameEventAdapter = GameEventAdapter(gameEventList)


    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_game_zixun

    companion object {
        fun newInstance(): ModFragmentGameZiXun {
            return ModFragmentGameZiXun()
        }
    }
    override fun lazyLoadData() {
        showLoading()
        mViewModel.getGameEventListData(currentPage, pageSize)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()

        immersionBar {
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
                    CommonActivityRichText.start(appContext, clickData.title, data?.content ?: "", data?.views ?: "-1")
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
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
    }


    override fun onRightClick(view: TitleBar) {
        super.onRightClick(view)

    }

    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun confirm() {

        }

    }

    /**********************************************Adapter**************************************************/



    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "估值速递") {
        var pic = IntObservableField(0)
        var isSelect = IntObservableField(0)

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


