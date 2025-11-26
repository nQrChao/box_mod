package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
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
import com.box.mod.databinding.ModFragmentMainBinding
import com.box.mod.databinding.ModItemGameEventBinding
import com.box.mod.databinding.ModItemGameGusuanBinding
import com.box.mod.databinding.ModItemGujiaGameHotBinding
import com.box.mod.ui.activity.ModActivityGuSuanXiangqing
import com.box.mod.ui.activity.ModActivityLogin
import com.box.mod.ui.activity.ModActivityMyShouCang
import com.box.mod.ui.activity.ModActivityShengChengQi
import com.box.mod.ui.adapter.MainBannerAdapter
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.indicator.DrawableIndicator
import com.zhpan.indicator.base.IIndicator
import com.zhpan.indicator.enums.IndicatorSlideMode


class ModFragmentMain : BaseTitleBarFragment<ModFragmentMain.Model, ModFragmentMainBinding>(),
    StatusAction {
    override val mViewModel: Model by viewModels()
    override fun layoutId(): Int = R.layout.mod_fragment_main

    companion object {
        fun newInstance(): ModFragmentMain {
            return ModFragmentMain()
        }
    }

    private val pageSize = 10
    private var currentPage = 1
    var clickData = ModDataBean()
    //var gameEventList: MutableList<ModDataBean> = mutableListOf()
    //var gameEventAdapter = GameEventAdapter(gameEventList)

    var gameGuSuanList: MutableList<ModDataBean> = mutableListOf()
    var gameGuSuanAdapter = GameGuSuanAdapter(gameGuSuanList)

    var gameHotAdapter = ModGamesHotAdapter()
    val marqueeList: MutableList<String> = mutableListOf(
        "用户xxx《王者荣耀》账号专业估价为：1234",
        "用户xxx《王者荣耀》账号专业估价为：12345",
        "用户xxx《王者荣耀》账号专业估价为：123456",
        "用户xxx《王者荣耀》账号专业估价为：1234567"
    )
    var bannerList: MutableList<ModDataBean> = mutableListOf()

    /**
     * 懒加载
     */
    override fun lazyLoadData() {
        showLoading()
        mViewModel.getGameTodayListData()
        mViewModel.getGameBannerData()
        mViewModel.getGameListData()
        mViewModel.getGameHotListData(currentPage, pageSize)
        //mViewModel.getGameEventListData(currentPage, pageSize)
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


        gameGuSuanAdapter.setDiffCallback(GameEventDiffCallback())
        mDataBinding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 1)
            isNestedScrollingEnabled = false  // ✅ 关键
            overScrollMode = View.OVER_SCROLL_NEVER
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = gameGuSuanAdapter
        }

        gameGuSuanAdapter.setOnItemClickListener { adapter, view, position ->
            clickData = adapter.data[position] as ModDataBean
            ModActivityGuSuanXiangqing.start(appContext, clickData.id.toString())
//            mViewModel.getEventDetailData(clickData.id)
        }


        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.getGameHotListData(currentPage, pageSize)
            }
            setOnLoadMoreListener {
                currentPage++
                mViewModel.getGameHotListData(currentPage, pageSize)
            }
        }



        mDataBinding.recyclerViewHotGame.apply {
            layoutManager = GridLayoutManager(context, 3)
            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = gameHotAdapter
        }
        gameHotAdapter.addChildClickViewIds(R.id.commit)
        gameHotAdapter.setOnItemClickListener { adapter, view, position ->
            clickData = adapter.data[position] as ModDataBean

        }


        gameHotAdapter.setOnItemChildClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            if (view.id == R.id.commit) {
                eventViewModel.setMainCurrentItem.value = 1
                eventViewModel.guJiaStringCurrentItem.value = clickedItem.name
            }
        }


    }


    @SuppressLint("NotifyDataSetChanged")
    override fun createObserver() {
        mViewModel.gameTodayListResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }
        mViewModel.gameBannerResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data != null) {
                        bannerList = data
                        setBanner(data)
                    }
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        mViewModel.gameListResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data != null) {
                        val filteredData = data.filter { it.name != "通用游戏" }
                        gameHotAdapter.setList(filteredData.shuffled().take(6))
                    }
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        mViewModel.gameHotListResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data.isNullOrEmpty()) {
                        if (currentPage == 1) {
                            mDataBinding.refreshLayout.finishRefresh()
                            gameGuSuanAdapter.setList(mutableListOf()) // 清空列表
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
                        mDataBinding.marqueeview.startWithList(marqueeList)
                        // 如果是排序后没有数据，也要清空列表
                        if (data.isEmpty()) {
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else {
                            gameGuSuanAdapter.setList(data)
                            mDataBinding.refreshLayout.resetNoMoreData()
                        }
                    } else { // 场景：上拉加载更多
                        if (data.isEmpty()) {
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                            return@parseModStateWithMsg
                        }
                        mDataBinding.refreshLayout.finishLoadMore()
                        gameGuSuanAdapter.addData(data)
                        gameGuSuanAdapter.notifyDataSetChanged()
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
                    CommonActivityRichText.start(
                        appContext,
                        clickData.title,
                        data?.content ?: "",
                        data?.views ?: "-1"
                    )
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
            eventViewModel.isLogin.value = true
            appViewModel.modUserInfo.value = savedUser
        }

    }

    fun setBanner(list: MutableList<ModDataBean>) {
        mDataBinding.indicatorView.visibility = View.VISIBLE
        (mDataBinding.bannerView as BannerViewPager<ModDataBean>)
            .setCanLoop(true)
            .setOrientation(ViewPager2.ORIENTATION_HORIZONTAL)
            .setIndicatorVisibility(View.GONE)
            .setIndicatorSlideMode(IndicatorSlideMode.SMOOTH)
            //.setIndicatorView(getDrawableIndicator())
            .setIndicatorView(mDataBinding.indicatorView)
            .setInterval(2000)
            .setAdapter(MainBannerAdapter()) // 链式调用
            .registerLifecycleObserver(viewLifecycleOwner.lifecycle)
            .setOnPageClickListener { clickedView: View?, position: Int ->
                ModActivityGuSuanXiangqing.start(appContext, bannerList[position].id.toString())
            }
            .create(list)
    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    override fun isStatusBarEnabled(): Boolean {
        return true
    }

    private fun getDrawableIndicator(): IIndicator {
        val dp10 = resources.getDimensionPixelOffset(com.box.com.R.dimen.idp_10)
        return DrawableIndicator(context)
            .setIndicatorGap(resources.getDimensionPixelOffset(com.box.com.R.dimen.idp_2_5))
            .setIndicatorDrawable(R.drawable.mod_heart_empty, R.drawable.mod_heart_red)
            .setIndicatorSize(dp10, dp10, dp10, dp10)
    }

    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun user() {
            if (!isLogin()) {
                ModActivityLogin.start(appContext)
            }
        }

        fun img1() {
            val targetView = mDataBinding.titleText
            val scrollView = mDataBinding.nestedScrollView
            val yPosition = targetView.top
            scrollView.smoothScrollTo(0, yPosition)
        }

        fun img2() {
            eventViewModel.setMainCurrentItem.value = 1
        }

        fun img3() {
            ModActivityShengChengQi.start(appContext)
            //eventViewModel.setMainCurrentItem.value = 2
        }

        fun img4() {
            //eventViewModel.setMainCurrentItem.value = 3
        }

        fun shoucang() {

            if (isLogin()) {
                ModActivityMyShouCang.start(appContext)
            } else {
                Toaster.show("请先登录")
                ModActivityLogin.start(appContext)
            }

        }

        fun test() {
            val assetGame = "file:///android_asset/out/gomoku.html"
            CommonActivityBrowser.start(appContext, assetGame)
        }

        fun confirm() {

        }

    }


    /**********************************************Adapter**************************************************/

    class ModGamesHotAdapter :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemGujiaGameHotBinding>>(R.layout.mod_item_gujia_game_hot) {
        override fun convert(
            holder: BaseDataBindingHolder<ModItemGujiaGameHotBinding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.setVariable(modData, item)
        }
    }


    class GameEventAdapter(list: MutableList<ModDataBean>) :
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


    class GameGuSuanAdapter(list: MutableList<ModDataBean>) :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemGameGusuanBinding>>(
            R.layout.mod_item_game_gusuan, list
        ) {
        override fun convert(
            holder: BaseDataBindingHolder<ModItemGameGusuanBinding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.setVariable(modData, item)
        }

        override fun onBindViewHolder(
            holder: BaseDataBindingHolder<ModItemGameGusuanBinding>,
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

    class GameGuSuanDiffCallback : DiffUtil.ItemCallback<ModDataBean>() {
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
        var gameTodayListResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        fun getGameTodayListData() {
            modRequestWithMsg({
                apiService.getValuationCommitTodayList()
            }, gameTodayListResult)
        }

        var gameBannerResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        fun getGameBannerData() {
            modRequestWithMsg({
                apiService.getValuationCommitBanner()
            }, gameBannerResult)
        }

        var gameListResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()

        fun getGameListData() {
            modRequestWithMsg({
                apiService.getValuationCommitGameList()
            }, gameListResult)
        }

        var gameHotListResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        fun getGameHotListData(pageNum: Int, pageSize: Int) {
            modRequestWithMsg({
                apiService.getValuationCommitHotList(pageNum, pageSize)
            }, gameHotListResult)
        }

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


