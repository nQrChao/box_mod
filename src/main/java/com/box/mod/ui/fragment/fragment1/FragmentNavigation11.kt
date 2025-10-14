package com.box.mod.ui.fragment.fragment1

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.box.base.base.action.StatusAction

import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.ext.parseModState
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.common.sdk.eventViewModel
import com.box.other.hjq.titlebar.TitleBar
import com.box.common.ui.xpop.XPopupDrawerAiList
import com.box.mod.R
import com.box.mod.databinding.FragmentNavigation11Binding
import com.box.common.data.model.ModTradeGoodDetailBean
import com.box.common.ui.layout.StatusLayout
import com.box.mod.BuildConfig
import com.box.mod.ui.activity.jiaoyi.ModActivityTradeDetails
import com.box.mod.ui.activity.image.ModActivityPreviewImageVideo
import com.box.mod.ui.activity.jiaoyi.ModXPopupJiaoyiShangJiaGroup
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import kotlinx.coroutines.launch
import com.box.common.R as RC

class FragmentNavigation11 : BaseTitleBarFragment<Navigation11Model, FragmentNavigation11Binding>(), Nav1ClickHandler, StatusAction {
    private val unifiedAdapter = Navigation11Adapter()
    private var currentPage = 1
    private val PAGE_SIZE = "20"
    private var currentOrderby = ""

    // 用于缓存顶部不会随排序变化的数据
    private var topImageItem: Navigation11ListItem.HeaderImageItem? = null
    private var topTitleItem: Navigation11ListItem.TitleItem? = null
    private var topGoodsItem: Navigation11ListItem.TopGoodsItem? = null
    private var topTitleItem2: Navigation11ListItem.Title2Item? = null
    private var topImages = if (BuildConfig.APP_UPDATE_ID == "27") {
        listOf(R.drawable.mod_banner, R.drawable.mod_banner3)
    } else {
        listOf(R.drawable.mod_banner, R.drawable.mod_banner5)
    }
    override fun layoutId(): Int = R.layout.fragment_navigation1_1

    companion object {
        fun newInstance(): FragmentNavigation11 = FragmentNavigation11()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.viewmodel = mViewModel
        //mDataBinding.click = ProxyClick()
        showLoading()
        unifiedAdapter.setClickHandler(this)
        immersionBar {
            statusBarDarkFont(true)
            init()
        }


        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.postAllTradeGoodsList()
            }
            setOnLoadMoreListener {
                currentPage++
                mViewModel.postDiyTradeGoodsList(currentOrderby, currentPage.toString(), PAGE_SIZE)
            }
        }
    }

    override fun createObserver() {
        mViewModel.topGoods.observe(this) {

        }

        mViewModel.topGameInfo.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    topImageItem = Navigation11ListItem.HeaderImageItem(topImages)
                    topTitleItem = Navigation11ListItem.TitleItem("热门成交", false,false)
                    topGoodsItem = mViewModel.topGoodsList.value?.take(10)?.let {
                        Navigation11ListItem.TopGoodsItem(it)
                    }
                    topTitleItem2 = Navigation11ListItem.Title2Item("最新发布", true,true)
                    refreshList()
                    Logs.e("topGameInfo:${GsonUtils.toJson(data)}")
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        mViewModel.tradeAllGoodsListResult.observe(this) { result ->
            parseModState(result, { initialList ->
                if (!initialList.isNullOrEmpty()) {
                    mViewModel.topGoodsList.value = initialList
                    mViewModel.topGoods.value = initialList.firstOrNull()
                    mViewModel.topGoods.value?.let {
                        mViewModel.postGetGameInfo(it.gameid)
                    }
                }
            }, {
                mDataBinding.refreshLayout.finishRefresh(false)
            })
        }


        mViewModel.tradeDiyGoodsListResult.observe(this) { result ->
            parseModState(result, { newGoodsList ->
                if (currentPage == 1) { // 场景：下拉刷新 或 排序
                    mDataBinding.refreshLayout.finishRefresh()
                    // 如果是排序后没有数据，也要清空列表
                    if (newGoodsList.isNullOrEmpty()) {
                        buildRefreshedList(emptyList())
                        mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                    } else {
                        buildRefreshedList(newGoodsList)
                        mDataBinding.refreshLayout.resetNoMoreData()
                    }
                } else { // 场景：上拉加载更多
                    if (newGoodsList.isNullOrEmpty()) {
                        mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        return@parseModState
                    }
                    mDataBinding.refreshLayout.finishLoadMore()
                    val currentList = unifiedAdapter.currentList.toMutableList()
                    newGoodsList.forEach { good ->
                        currentList.add(Navigation11ListItem.GoodsItem(good))
                    }
                    unifiedAdapter.submitList(currentList)
                }

            }, { error ->
                if (currentPage > 1) {
                    currentPage--
                    mDataBinding.refreshLayout.finishLoadMore(false)
                } else {
                    mDataBinding.refreshLayout.finishRefresh(false)
                }
                Toaster.show(error.errorLog)
            })
            showComplete()
        }

        eventViewModel.toShangJiaView.observe(this) {
            currentOrderby = when (it) {
                "view1" -> ""
                "view2" -> "price_up"
                "view3" -> "price_down"
                "view4" -> "profit_rate_asc"
                else -> ""
            }
            mViewModel.postDiyTradeGoodsList(currentOrderby, "1", PAGE_SIZE)
        }
    }

    private fun setPhotoList(bean: ModTradeGoodDetailBean) {
        mViewModel.photoList.clear()
        bean.pic.forEachIndexed { index, pic ->
            mViewModel.photoList.add(pic.pic_path)
        }
    }

    private fun refreshList() {
        currentPage = 1
        mDataBinding.refreshLayout.post { mDataBinding.refreshLayout.autoRefreshAnimationOnly() }
        mViewModel.postDiyTradeGoodsList(currentOrderby, "1", PAGE_SIZE)
    }

    private fun buildRefreshedList(goodsList: List<ModTradeGoodDetailBean>) {
        val listItems = mutableListOf<Navigation11ListItem>()
        topImageItem?.let { listItems.add(it) }
        topTitleItem?.let { listItems.add(it) }
        topGoodsItem?.let { listItems.add(it) }
        topTitleItem2?.let { listItems.add(it) }
        if (goodsList.isNotEmpty()) {
            goodsList.forEach { good ->
                listItems.add(Navigation11ListItem.GoodsItem(good))
            }
        }

        unifiedAdapter.submitList(listItems)
    }


    override fun lazyLoadData() {
        mViewModel.postAllTradeGoodsList()
    }


    // 以下方法保持不变
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
                }, null, false, R.layout.xpopup_confirm_mod
            ).show()
    }

    override fun onTitleImageClick(goodDetail: ModTradeGoodDetailBean) {
        ModActivityTradeDetails.start(mActivity, goodDetail.gid)
    }

    override fun onTopGoodClick(goodDetail: ModTradeGoodDetailBean) {
        ModActivityTradeDetails.start(mActivity, goodDetail.gid)
    }

    override fun onSearchTitleClick(view: View) {
        XPopup.Builder(context)
            .hasShadowBg(false)
            .hasStatusBar(true)
            .isLightStatusBar(true)
            .hasNavigationBar(true)
            .isDestroyOnDismiss(false)
            .atView(view)
            .asCustom(context?.let { ModXPopupJiaoyiShangJiaGroup(it) })
            .show()
    }

    override fun onSearchClick() {
        Toaster.show("onSearchClick")
    }

    override fun onJiaoYiClick() {
        eventViewModel.fragment1Tab.value = 1
    }

    override fun onLeYuanClick() {
        eventViewModel.fragment1Tab.value = 2
    }

    override fun onReLiaoClick() {
        eventViewModel.fragment1Tab.value = 3
    }

    override fun onTopGoodsItemClick(goodDetail: ModTradeGoodDetailBean) {
        ModActivityTradeDetails.start(mActivity, goodDetail.gid)
    }

    override fun onTopGoodsItemChildClick(goodDetail: ModTradeGoodDetailBean, view: View) {

    }

    override fun onMainGoodsItemClick(goodDetail: ModTradeGoodDetailBean) {
        ModActivityTradeDetails.start(mActivity, goodDetail.gid)
    }

    override fun onMainGoodsItemChildClick(goodDetail: ModTradeGoodDetailBean, view: View) {
        lifecycleScope.launch {
            setPhotoList(goodDetail)
            val imageIndex = when (view.id) {
                R.id.pic1 -> 0
                R.id.pic2 -> 1
                R.id.pic3 -> 2
                else -> -1 // 其他情况使用无效索引 -1
            }
            if (imageIndex != -1) {
                goodDetail.pic.getOrNull(imageIndex)?.let {
                    ModActivityPreviewImageVideo.start(mActivity, mViewModel.photoList, imageIndex)
                }
            } else if (view.id == R.id.icon) {
                ModActivityTradeDetails.start(mActivity, goodDetail.gid)
            }
        }
    }

    override fun getStatusLayout(): StatusLayout? {
        return mDataBinding.hlHint
    }


}