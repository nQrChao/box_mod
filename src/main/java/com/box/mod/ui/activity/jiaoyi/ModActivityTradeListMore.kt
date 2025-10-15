package com.box.mod.ui.activity.jiaoyi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.ext.parseModState
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.com.R as RC
import com.box.mod.R
import com.box.common.data.model.ModTradeGoodDetailBean
import com.box.mod.databinding.ModActivityJiaoyiTradeListMoreBinding
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar

class ModActivityTradeListMore : BaseVmDbActivity<ModActivityTradeListMoreModel, ModActivityJiaoyiTradeListMoreBinding>() {
    private val diyListAdapter = ModJiaoyiTradeGoodDetailAdapter()

    private var currentPage = 1
    private val PAGE_SIZE = "20"
    private var scene = "normal"
    private var currentOrderby = ""

    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_trade_list_more

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityTradeListMore::class.java)
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
        mDataBinding.tab.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            mViewModel.isSelect.set(toIndex)
            when (toIndex) {
                0 -> {
                    scene= "normal"
                    currentOrderby = ""
                }
                1 -> {
                    scene= "normal"
                    currentOrderby = "profit_rate_asc"
                }
                2 -> {
                    scene= "normal"
                    mViewModel.isPriceTranslate.set(!mViewModel.isPriceTranslate.get())
                    currentOrderby = if(mViewModel.isPriceTranslate.get()){
                        "price_up"
                    }else{
                        "price_down"
                    }

                }
                3 -> {
                    scene= "trends"
                    currentOrderby = ""
                }
                else -> ""
            }

            mDataBinding.recyclerView.scrollToPosition(0)
            mDataBinding.root.postDelayed({
                mDataBinding.refreshLayout.autoRefresh()
            }, 500)
        }

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = diyListAdapter
        }

        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.postDiyTradeGoodsList(scene,currentOrderby, currentPage.toString(), PAGE_SIZE)
            }

            setOnLoadMoreListener {
                currentPage++
                mViewModel.postDiyTradeGoodsList(scene,currentOrderby, currentPage.toString(), PAGE_SIZE)
            }
        }

        mDataBinding.searchEdit.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mDataBinding.click?.search(textView)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        diyListAdapter.setOnItemClickListener { adapter, view, position ->
            val goodDetail = adapter.data[position] as ModTradeGoodDetailBean
            if(goodDetail.goods_status == 10){
                Toaster.show("该商品已售出")
            }else{
                ModActivityTradeDetails.start(appContext, goodDetail.gid)
            }

        }


        currentPage = 1
        mViewModel.postDiyTradeGoodsList(scene,currentOrderby, "1", PAGE_SIZE)
    }
    private fun setPhotoList(bean: ModTradeGoodDetailBean) {
        mViewModel.photoList.clear()
        bean.pic.forEachIndexed { index, pic ->
            mViewModel.photoList.add(pic.pic_path)
        }
    }

    override fun createObserver() {
        mViewModel.tradeDiyGoodsListResult.observe(this) { result ->
            parseModState(result, { newGoodsList ->
                // 判断是刷新/排序 (page=1)，还是加载更多 (page>1)
                if (currentPage == 1) {
                    // 场景：刷新 或 排序
                    mDataBinding.refreshLayout.finishRefresh()
                    diyListAdapter.setList(newGoodsList)
                    mDataBinding.recyclerView.scrollToPosition(0)
                    // 在这里，我们不应该判断有没有更多数据。
                    // 无论第一页返回多少数据，我们都应该假设可能有下一页。
                    // 我们需要重置“没有更多数据”的状态，以确保上拉加载功能总是可用的。
                    mDataBinding.refreshLayout.resetNoMoreData()
                } else {
                    // 场景：加载更多
                    if (newGoodsList.isNullOrEmpty()) {
                        //只有在加载更多时返回空数据，才代表真的没有更多了。
                        mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                    } else {
                        mDataBinding.refreshLayout.finishLoadMore()
                        diyListAdapter.addData(newGoodsList)
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
        //ModActivityJiaoYiTip.start(appContext)
    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun search(view: View) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            mViewModel.postDiyTradeGoodsList(scene,currentOrderby, currentPage.toString(), PAGE_SIZE)
        }
    }

}