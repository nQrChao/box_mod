package com.chaoji.mod.ui.fragment.fragment1

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.chaoji.base.base.action.HandlerAction
import com.chaoji.base.base.fragment.BaseTitleBarFragment
import com.chaoji.base.ext.parseModState
import com.chaoji.base.network.NetState
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.mod.R
import com.chaoji.mod.databinding.FragmentNavigation12Binding
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.mod.ui.activity.jiaoyi.ModActivityTradeDetails
import com.chaoji.mod.ui.activity.image.ModActivityPreviewImageVideo
import com.chaoji.mod.ui.activity.jiaoyi.ModJiaoyiTradeGoodDetailAdapter
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import kotlinx.coroutines.launch

class FragmentNavigation12 : BaseTitleBarFragment<Navigation12Model, FragmentNavigation12Binding>(), HandlerAction {
    private val diyListAdapter = ModJiaoyiTradeGoodDetailAdapter()

    private var currentPage = 1
    private val PAGE_SIZE = "20"
    private var currentOrderby = ""

    override fun layoutId(): Int = R.layout.fragment_navigation1_2

    companion object {
        fun newInstance(): FragmentNavigation12 {
            return FragmentNavigation12()
        }
    }


    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            statusBarDarkFont(true)
            init()
        }

        mDataBinding.tab.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            mViewModel.isSelect.set(toIndex)
            currentOrderby = when (toIndex) {
                0 -> ""
                1 -> "profit_rate_asc"
                2 -> "price_up"
                3 -> "price_down"
                else -> ""
            }
            mDataBinding.recyclerView.scrollToPosition(0)
            mDataBinding.root.postDelayed({
                mDataBinding.refreshLayout.autoRefresh()
            }, 500)
        }

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = diyListAdapter
        }

        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.postDiyTradeGoodsList(currentOrderby, currentPage.toString(), PAGE_SIZE)
            }

            setOnLoadMoreListener {
                currentPage++
                mViewModel.postDiyTradeGoodsList(currentOrderby, currentPage.toString(), PAGE_SIZE)
            }
        }

        mDataBinding.searchEdit.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mDataBinding.click?.search(textView)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        diyListAdapter.addChildClickViewIds(R.id.pic1,R.id.pic2,R.id.pic3)
        diyListAdapter.setOnItemClickListener { adapter, view, position ->
            val goodDetail = adapter.data[position] as ModTradeGoodDetailBean
            ModActivityTradeDetails.start(mActivity, goodDetail.gid)
        }
        diyListAdapter.setOnItemChildClickListener { adapter, view, position ->
            val goodDetail = adapter.data[position] as ModTradeGoodDetailBean
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
    }
    private fun setPhotoList(bean: ModTradeGoodDetailBean) {
        mViewModel.photoList.clear()
        bean.pic.forEachIndexed { index, pic ->
            mViewModel.photoList.add(pic.pic_path)
        }
    }


    override fun initData() {

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

    override fun lazyLoadData() {
        currentPage = 1
        mViewModel.postDiyTradeGoodsList(currentOrderby, "1", PAGE_SIZE)
    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun search(view: View) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            mViewModel.postDiyTradeGoodsList(currentOrderby, currentPage.toString(), PAGE_SIZE)
        }


    }

}
