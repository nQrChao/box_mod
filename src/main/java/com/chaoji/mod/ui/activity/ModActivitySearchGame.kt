package com.chaoji.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.base.base.action.StatusAction
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModState
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.data.model.ModGameHallList
import com.chaoji.im.data.model.ModGameLabelBean
import com.chaoji.im.ui.adapter.HorizontalSpaceItemDecoration
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.im.ui.layout.StatusLayout
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ItemHotSearchTagBinding
import com.chaoji.mod.databinding.ModActivitySearchGameBinding
import com.chaoji.mod.databinding.ModItemGameSearchBinding
import com.chaoji.mod.ui.adapter.ModGameHallListItemAdapter
import com.chaoji.mod.ui.adapter.ModGameLabelAdapter
import com.chaoji.common.R as RC
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager

class ModActivitySearchGame : BaseVmDbActivity<ModActivitySearchGameModel, ModActivitySearchGameBinding>(), StatusAction {
    private var currentPage = 1
    var hotSearchTags: MutableList<String> = mutableListOf()
    val modHotSearchAdapter = ModHotSearchAdapter(hotSearchTags)

    var gameItemList: MutableList<ModGameHallList> = mutableListOf()
    var gamItemListAdapter = ModGameSearchItemAdapter(gameItemList)
    override fun layoutId(): Int = R.layout.mod_activity_search_game

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivitySearchGame::class.java)
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
            titleBar(R.id.sc_ll)
            navigationBarColor(RC.color.white)
            init()
        }

        mDataBinding.recyclerViewHotSearch.run {
            layoutManager = FlexboxLayoutManager(this@ModActivitySearchGame).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                alignItems = AlignItems.STRETCH
            }
            addItemDecoration(HorizontalSpaceItemDecoration(2))
            adapter = modHotSearchAdapter
        }
        modHotSearchAdapter.setOnItemClickListener { adapter, view, position ->
            val tag = adapter.getItem(position) as String
            mViewModel.searchKey.set(tag)
            currentPage = 1
            showLoading()
            mViewModel.postGameHallList( mViewModel.searchKey.get(), currentPage.toString())
        }


        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = gamItemListAdapter
        }

        gamItemListAdapter.setOnItemClickListener { adapter, view, position ->
            val hallBean = adapter.data[position] as ModGameHallList
            ModActivityGameDetails.start(appContext, hallBean.gameid)
        }


        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                showLoading()
                currentPage = 1
                mViewModel.postGameHallList( mViewModel.searchKey.get(), currentPage.toString())
            }

            setOnLoadMoreListener {
                currentPage++
                mViewModel.postGameHallList( mViewModel.searchKey.get(), currentPage.toString())
            }
        }


        mDataBinding.searchEdit.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                showLoading()
                val imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(mDataBinding.searchEdit.windowToken, 0)
                mViewModel.postGameHallList(mViewModel.searchKey.get(), currentPage.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


        mViewModel.postSearchHotGame()


    }


    override fun createObserver() {
        mViewModel.searchHotGameBeanResult.observe(this) { it ->
            parseModState(it, {
                it?.let {
                    hotSearchTags = it.search_hot_word
                    modHotSearchAdapter.setList(hotSearchTags)
                    currentPage = 1
                    mViewModel.postGameHallList( mViewModel.searchKey.get(), currentPage.toString())

                }
            }, {
                Toaster.show(it.errorLog)
            })
        }
        mViewModel.gameHallListResult.observe(this) { result ->
            parseModState(result, { gameList ->
                if (currentPage == 1) {
                    gamItemListAdapter.setList(null)
                    mDataBinding.refreshLayout.finishRefresh()
                    gamItemListAdapter.setList(gameList)
                    mDataBinding.recyclerView.scrollToPosition(0)
                    mDataBinding.refreshLayout.resetNoMoreData()
                } else {
                    if (gameList.isNullOrEmpty()) {
                        mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                    } else {
                        mDataBinding.refreshLayout.finishLoadMore()
                        gamItemListAdapter.addData(gameList)
                    }
                }
                showComplete()
            }, { error ->
                if (currentPage > 1) {
                    currentPage--
                    mDataBinding.refreshLayout.finishLoadMore(false)
                } else {
                    if (mDataBinding.refreshLayout.isRefreshing) {
                        mDataBinding.refreshLayout.finishRefresh(false)
                    }
                }
                showComplete()
                Toaster.show(error.errorLog)
            })
        }


    }


    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun leftIcon() {
            finish()
        }

        fun search(view: View) {
            val imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            showLoading()
            mViewModel.postGameHallList(mViewModel.searchKey.get(), currentPage.toString())
        }

        fun del() {

        }


    }

    class ModGameSearchItemAdapter constructor(list: MutableList<ModGameHallList>) : BaseQuickAdapter<ModGameHallList, BaseDataBindingHolder<ModItemGameSearchBinding>>(
        R.layout.mod_item_game_search, list
    ) {
        var appList: MutableList<ModGameLabelBean> = mutableListOf()
        var appListAdapter = ModGameLabelAdapter(appList)
        override fun convert(holder: BaseDataBindingHolder<ModItemGameSearchBinding>, item: ModGameHallList) {
            holder.dataBinding?.setVariable(BR.gameHallListBean, item)
            holder.dataBinding?.recyclerView?.run {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                addItemDecoration(HorizontalSpaceItemDecoration(2))
                adapter = appListAdapter
            }

            appListAdapter.setList(item.game_labels)
        }
    }

    class ModHotSearchAdapter constructor(list: MutableList<String>) : BaseQuickAdapter<String, BaseDataBindingHolder<ItemHotSearchTagBinding>>(
        R.layout.item_hot_search_tag, list
    ) {
        override fun convert(holder: BaseDataBindingHolder<ItemHotSearchTagBinding>, item: String) {
            holder.dataBinding?.setVariable(BR.tagName, item)
        }
    }

    override fun getStatusLayout(): StatusLayout {
        return mDataBinding.hlHint
    }
}