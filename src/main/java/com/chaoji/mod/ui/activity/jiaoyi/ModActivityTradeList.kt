package com.chaoji.mod.ui.activity.jiaoyi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModState
import com.chaoji.base.network.NetState
import com.chaoji.mod.BR
import com.chaoji.im.appContext
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.common.R as RC
import com.chaoji.mod.R
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.toBrowser
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.mod.databinding.ModActivityJiaoyiTradeListBinding
import com.chaoji.mod.databinding.ModItemJiaoyiDetailsMoreBinding
import com.chaoji.mod.ui.activity.image.ModActivityPreviewImageVideo
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar

class ModActivityTradeList : BaseVmDbActivity<ModActivityTradeListModel, ModActivityJiaoyiTradeListBinding>() {
    var currentPage = 1
    val PAGE_SIZE = "20"
    var currentOrderby = ""
    var goodDetailList: MutableList<ModTradeGoodDetailBean> = mutableListOf()
    var appDetailAdapter = ModTradeGoodDetailAdapter(goodDetailList)

    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_trade_list

    companion object {
        const val GAME_ID: String = "goodId"
        const val GAME_NAME: String = "goodName"
        const val GAME_ICON: String = "goodIcon"
        fun start(context: Context, goodId: String, goodName: String, goodIcon: String) {
            if (TextUtils.isEmpty(goodId)) {
                return
            }
            val intent = Intent(context, ModActivityTradeList::class.java)
            intent.putExtra(GAME_ID, goodId)
            intent.putExtra(GAME_NAME, goodName)
            intent.putExtra(GAME_ICON, goodIcon)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }

    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            navigationBarColor(RC.color.white)
            init()
        }
        appDetailAdapter.setDiffCallback(ModTradeGoodDetailDiffCallback())
        mViewModel.gameId = intent.getStringExtra(GAME_ID) ?: ""
        mViewModel.gameName = intent.getStringExtra(GAME_NAME) ?: ""
        mViewModel.gameIcon = intent.getStringExtra(GAME_ICON) ?: ""
        if (!StringUtils.isEmpty(mViewModel.gameId)) {
            mViewModel.postTradeGoodBeanList(mViewModel.gameId, currentPage, PAGE_SIZE)
        }
        if (!StringUtils.isEmpty(mViewModel.gameName)) {
            mViewModel.titleT.value =  mViewModel.gameName
        }

        mDataBinding.innerRecyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = appDetailAdapter
        }

        appDetailAdapter.setOnItemClickListener { adapter, view, position ->
            val clickedItem = adapter.getItem(position) as ModTradeGoodDetailBean
            ModActivityTradeDetails.start(this@ModActivityTradeList, clickedItem.gid)
        }

        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.postTradeGoodBeanList(mViewModel.gameId, currentPage, PAGE_SIZE)
            }

            setOnLoadMoreListener {
                currentPage++
                mViewModel.postTradeGoodBeanList(mViewModel.gameId, currentPage, PAGE_SIZE)
            }
        }

    }

    override fun createObserver() {
        mViewModel.tradeGoodsListResult.observe(this) { result ->
            parseModState(result, { newGoodsList ->
                if (currentPage == 1) {
                    mDataBinding.refreshLayout.finishRefresh()
                    appDetailAdapter.setList(newGoodsList)
                    mDataBinding.innerRecyclerView.scrollToPosition(0)
                    mDataBinding.refreshLayout.resetNoMoreData()
                } else {
                    if (newGoodsList.isNullOrEmpty()) {
                        mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                    } else {
                        mDataBinding.refreshLayout.finishLoadMore()
                        appDetailAdapter.addData(newGoodsList)
                    }
                }

            }, { error ->
                if (currentPage > 1) {
                    currentPage--
                    mDataBinding.refreshLayout.finishLoadMore(false)
                } else {
                    if (mDataBinding.refreshLayout.isRefreshing) {
                        mDataBinding.refreshLayout.finishRefresh(false)
                    }
                }
                Toaster.show(error.errorLog)
            })
        }


    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    override fun onRightClick(view: TitleBar) {
        super.onRightClick(view)
        CommonActivityBrowser.start(appContext, "https://mobile.xiaodianyouxi.com/index.php/Index/market_view/?id=588")
    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun kefu() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    toBrowser(it.marketjson.wechat_url)
                }
            }
        }

        fun pic1() {
            ModActivityPreviewImageVideo.start(this@ModActivityTradeList, mViewModel.photoList, 0)
        }

        fun pic2() {
            ModActivityPreviewImageVideo.start(this@ModActivityTradeList, mViewModel.photoList, 1)
        }

        fun pic3() {
            ModActivityPreviewImageVideo.start(this@ModActivityTradeList, mViewModel.photoList, 2)
        }


    }


    class ModTradeGoodDetailAdapter constructor(list: MutableList<ModTradeGoodDetailBean>) : BaseQuickAdapter<ModTradeGoodDetailBean, BaseDataBindingHolder<ModItemJiaoyiDetailsMoreBinding>>(
        R.layout.mod_item_jiaoyi_details_more, list
    ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemJiaoyiDetailsMoreBinding>, item: ModTradeGoodDetailBean) {
            holder.dataBinding?.setVariable(BR.goodDetailBean, item)
        }

        override fun onBindViewHolder(holder: BaseDataBindingHolder<ModItemJiaoyiDetailsMoreBinding>, position: Int, payloads: MutableList<Any>) {
            super.onBindViewHolder(holder, position, payloads)
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
                return
            }

            val binding = holder.dataBinding
            val item = getItem(position)

            // 遍历所有的 payload
            for (payload in payloads) {
                if (payload == "LIKE_UPDATE") {
                    binding?.setVariable(BR.goodDetailBean, item)
                }
            }
        }

    }

    class ModTradeGoodDetailDiffCallback : DiffUtil.ItemCallback<ModTradeGoodDetailBean>() {
        override fun areItemsTheSame(oldItem: ModTradeGoodDetailBean, newItem: ModTradeGoodDetailBean): Boolean {
            return oldItem.gid == newItem.gid
        }

        override fun areContentsTheSame(oldItem: ModTradeGoodDetailBean, newItem: ModTradeGoodDetailBean): Boolean {
            return oldItem == newItem
        }
    }


}