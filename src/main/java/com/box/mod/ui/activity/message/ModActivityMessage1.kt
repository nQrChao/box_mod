package com.box.mod.ui.activity.message

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.action.StatusAction
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appContext
import com.box.common.appViewModel
import com.box.common.data.model.ModDataBean
import com.box.common.eventViewModel
import com.box.common.network.apiService
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.common.ui.activity.CommonActivityRichText
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.ui.layout.StatusLayout
import com.box.common.utils.ext.logsE
import com.box.mod.BR.modData
import com.box.mod.R
import com.box.mod.databinding.ModActivityMessage1Binding
import com.box.mod.databinding.ModItemMessage1Binding
import com.box.mod.ui.activity.ModActivityLogout
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.com.R as RC

class ModActivityMessage1 :
    BaseVmDbActivity<ModActivityMessage1.Model, ModActivityMessage1Binding>(), StatusAction {


    private val pageSize = 10
    private var currentPage = 1
    var messageList: MutableList<ModDataBean> = mutableListOf()
    var messageAdapter = MessageAdapter(messageList)

    override fun layoutId(): Int = R.layout.mod_activity_message_1


    /**
     * 加载状态
     */
    override fun getStatusLayout(): StatusLayout {
        return mDataBinding.statusLoading
    }


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityMessage1::class.java)
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

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
//            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
//            ContextCompat.getDrawable(context, R.drawable.shape_divider_gray)?.let {
//                divider.setDrawable(it)
//            }
//            addItemDecoration(divider)
            adapter = messageAdapter
        }

        messageAdapter.setOnItemClickListener { adapter, view, position ->
            val modDataBean = adapter.data[position] as ModDataBean
            val wasUnread = modDataBean.readStatus != 1
            mViewModel.getReadNoticeData(modDataBean.noticeId)
            CommonActivityRichText.start(appContext, modDataBean.noticeTitle, modDataBean.noticeContent)
            if (wasUnread) {
                modDataBean.readStatus = 1
                adapter.notifyItemChanged(position, "READ_STATUS_UPDATE")
            }
            eventViewModel.updateMessage.value = true
        }


        mDataBinding.refreshLayout.apply {
            setOnRefreshListener {
                currentPage = 1
                mViewModel.getMessageData(currentPage, pageSize)
            }

            setOnLoadMoreListener {
                currentPage++
                mViewModel.getMessageData(currentPage, pageSize)
            }
        }

        mViewModel.getMessageData(currentPage, pageSize)

    }

    override fun createObserver() {
        mViewModel.messageResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data.isNullOrEmpty()) {
                        if (currentPage == 1) {
                            mViewModel.hasData.set(false)
                            mDataBinding.refreshLayout.finishRefresh()
                            messageAdapter.setList(mutableListOf()) // 清空列表
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else { // 加载更多时没有数据
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        }
                        return@parseModStateWithMsg
                    }
                    mViewModel.hasData.set(true)
                    if (currentPage == 1) { // 下拉刷新
                        mDataBinding.refreshLayout.finishRefresh()
                        // 如果是排序后没有数据，也要清空列表
                        if (data.isEmpty()) {
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else {
                            messageAdapter.setList(data)
                            mDataBinding.refreshLayout.resetNoMoreData()
                        }
                    } else { // 场景：上拉加载更多
                        if (data.isEmpty()) {
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                            return@parseModStateWithMsg
                        }
                        mDataBinding.refreshLayout.finishLoadMore()
                        messageAdapter.addData(data)
                        messageAdapter.notifyDataSetChanged()
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

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun zhuxiao() {
            ModActivityLogout.Companion.start(appContext)
        }

        fun yonghuxieyi() {
            appViewModel.modInitBean.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.userAgreementLink)
                }
            }
        }

        fun yinsixieyi() {
            appViewModel.modInitBean.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.privacyPolicyLink)
                }
            }
        }


    }

    /**********************************************Adapter**************************************************/
    class MessageAdapter constructor(list: MutableList<ModDataBean>) :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemMessage1Binding>>(
            R.layout.mod_item_message_1, list
        ) {
        private var lastPosition = -1

        override fun convert(
            holder: BaseDataBindingHolder<ModItemMessage1Binding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.setVariable(modData, item)
            holder.dataBinding?.executePendingBindings()

            if (holder.layoutPosition > lastPosition) {
                val animation = AnimationUtils.loadAnimation(
                    holder.itemView.context,
                    R.anim.item_slide_up_fade_in
                )
                animation.startOffset = 50L * holder.layoutPosition.toLong()
                holder.itemView.startAnimation(animation)
                lastPosition = holder.layoutPosition
            }
        }

        override fun onBindViewHolder(
            holder: BaseDataBindingHolder<ModItemMessage1Binding>,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
                return
            }
            if (payloads.any { it == "SHOUCANG_UPDATE" || it == "READ_STATUS_UPDATE" }) {
                val item = getItem(position)
                holder.dataBinding?.setVariable(modData, item)
                holder.dataBinding?.executePendingBindings() // 确保数据立即绑定
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }

        override fun onViewRecycled(holder: BaseDataBindingHolder<ModItemMessage1Binding>) {
            holder.itemView.clearAnimation()
            super.onViewRecycled(holder)
        }

        fun resetAnimationState() {
            lastPosition = -1
        }

        fun updateList(newList: List<ModDataBean>) {
            val diffResult = DiffUtil.calculateDiff(MessageDiffCallback(data, newList))
            data.clear()
            data.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    class MessageDiffCallback(
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



    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "我的消息") {
        var hasData = BooleanObservableField(false)
        var isLogin = BooleanObservableField(false)
        var messageResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        var getReadNoticeResult = MutableLiveData<ModResultStateWithMsg<Any>>()

        fun getMessageData(pageNum: Int, pageSize: Int) {
            modRequestWithMsg({
                apiService.getMessageList(pageNum, pageSize)
            }, messageResult)
        }

        fun getReadNoticeData(noticeId: String) {
            modRequestWithMsg({
                apiService.getReadNotice(noticeId)
            }, getReadNoticeResult)
        }
    }


}