package com.box.mod.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.action.StatusAction
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appContext
import com.box.common.data.model.ModDataBean
import com.box.common.eventViewModel
import com.box.common.network.apiService
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.ui.layout.StatusLayout
import com.box.common.utils.ext.logsE
import com.box.mod.BR.modData
import com.box.mod.R
import com.box.mod.databinding.ModActivityMyGujiaBinding
import com.box.mod.databinding.ModItemMyGujiaListBinding
import com.box.mod.ui.activity.ModActivityMyShouCang.Companion.INTENT_KEY_TYPE_RANK
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

@SuppressLint("CustomSplashScreen")
class ModActivityMyGujia : BaseVmDbActivity<ModActivityMyGujia.Model, ModActivityMyGujiaBinding>() , StatusAction{
    private var type = 1
    private val pageSize = 10
    private var currentPage = 1
    private val myGujiaAdapter = ModMyGujiaAdapter()


    override fun layoutId(): Int {
        return R.layout.mod_activity_my_gujia
    }
    /**
     * 加载状态
     */
    override fun getStatusLayout(): StatusLayout {
        return mDataBinding.statusLoading
    }

    companion object {
        const val INTENT_KEY_TYPE_GUJIA: String = "gujiaType"
        var resultLauncher: ActivityResultLauncher<Intent>? = null
        fun start(context: Context) {
            val intent = Intent(context, ModActivityMyGujia::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }

        fun start(context: Context,rankType:Int) {
            val intent = Intent(context, ModActivityMyGujia::class.java)
            intent.putExtra(INTENT_KEY_TYPE_GUJIA, rankType)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }

    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        mViewModel.isSelect.set(intent.getIntExtra(INTENT_KEY_TYPE_GUJIA,0))
        showLoading()
        immersionBar {
            navigationBarColor(com.box.com.R.color.white_pressed_color)
            statusBarDarkFont(true)
            init()
        }

        mDataBinding.tab.tabDefaultIndex = intent.getIntExtra(INTENT_KEY_TYPE_RANK,0)
        mDataBinding.tab.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            mViewModel.isSelect.set(toIndex)
            type = when (toIndex) {
                0 -> {
                    1
                }
                1 -> {
                    0
                }
                else -> 0
            }
            mDataBinding.root.postDelayed({
                mViewModel.getMyCommitListData(type,currentPage, pageSize)
            }, 100) // 延迟100毫秒
        }


        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = myGujiaAdapter
        }
        myGujiaAdapter.setOnItemClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            ModActivityMyGujiaXiangqing.start(appContext,clickedItem.id.toString())

        }

        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.getMyCommitListData(type,currentPage, pageSize, )
            }

            setOnLoadMoreListener {
                currentPage++
                mViewModel.getMyCommitListData(type,currentPage, pageSize, )
            }
        }

        mViewModel.getMyCommitListData(type,currentPage, pageSize, )


    }

    override fun createObserver() {
        mViewModel.myCommitListResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data.isNullOrEmpty()) {
                        if (currentPage == 1) { // 刷新时没有数据
                            mDataBinding.refreshLayout.finishRefresh()
                            myGujiaAdapter.updateList(mutableListOf()) // 清空列表
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else { // 加载更多时没有数据
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        }
                        return@parseModStateWithMsg // 提前退出
                    }

                    if (currentPage == 1) {
                        mDataBinding.refreshLayout.finishRefresh()
                        myGujiaAdapter.updateList(data)
                        mDataBinding.refreshLayout.resetNoMoreData()
                        myGujiaAdapter.resetAnimationState()
                    } else {
                        mDataBinding.refreshLayout.finishLoadMore()
                        myGujiaAdapter.addData(data)
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

        eventViewModel.closeMyGujiaActivity.observe(this) { shouldClose ->
            if (shouldClose) {
                finish()
            }
        }
    }

    override fun onNetworkStateChanged(netState: NetState) {
    }


    inner class ProxyClick {
        fun confirm() {

        }

    }


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "我的账号估算") {
        var hasData = BooleanObservableField(false)
        var isSelect = IntObservableField(0)

        var myCommitListResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()

        fun getMyCommitListData(checkState: Int,pageNum: Int,pageSize: Int) {
            modRequestWithMsg({
                apiService.getValuationCommitList(checkState, pageNum,pageSize)
            }, myCommitListResult)
        }

    }


    class ModMyGujiaAdapter : BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemMyGujiaListBinding>>(
        R.layout.mod_item_my_gujia_list
    ) {
        private var lastPosition = -1
        override fun convert(holder: BaseDataBindingHolder<ModItemMyGujiaListBinding>, item: ModDataBean) {
            // 绑定逻辑保持不变
            holder.dataBinding?.let {
                it.setVariable(modData, item)
                it.executePendingBindings()
            }
            if (holder.layoutPosition > lastPosition) {
                val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_slide_up_fade_in)
                animation.startOffset = 50L * holder.layoutPosition.toLong()
                holder.itemView.startAnimation(animation)
                lastPosition = holder.layoutPosition
            }

        }

        override fun onViewRecycled(holder: BaseDataBindingHolder<ModItemMyGujiaListBinding>) {
            holder.itemView.clearAnimation()
            super.onViewRecycled(holder)
        }

        fun resetAnimationState() {
            lastPosition = -1
        }

        fun updateList(newList: List<ModDataBean>) {
            val diffResult = DiffUtil.calculateDiff(ModMyGujiaDiffCallback(data, newList))
            data.clear()
            data.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    class ModMyGujiaDiffCallback(
        private val oldList: List<ModDataBean>,
        private val newList: List<ModDataBean>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }


}