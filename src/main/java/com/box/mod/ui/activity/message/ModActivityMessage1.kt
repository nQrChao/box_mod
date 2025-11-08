package com.box.mod.ui.activity.message

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

class ModActivityMessage1 : BaseVmDbActivity<ModActivityMessage1.Model, ModActivityMessage1Binding>(),StatusAction {

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


        messageAdapter.setDiffCallback(MessageDiffCallback())
        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = messageAdapter
        }
        messageAdapter.setOnItemClickListener { adapter, view, position ->
            val modDataBean = adapter.data[position] as ModDataBean
            CommonActivityRichText.start(appContext, modDataBean.noticeTitle, modDataBean.noticeContent)
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


        mViewModel.getMessageData(currentPage,pageSize)

    }

    override fun createObserver() {
        mViewModel.messageResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data.isNullOrEmpty()) {
                        if (currentPage == 1) {
                            mDataBinding.refreshLayout.finishRefresh()
                            messageAdapter.setList(mutableListOf()) // 清空列表
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else { // 加载更多时没有数据
                            mDataBinding.refreshLayout.finishLoadMoreWithNoMoreData()
                        }
                        return@parseModStateWithMsg
                    }

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
        override fun convert(
            holder: BaseDataBindingHolder<ModItemMessage1Binding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.setVariable(modData, item)
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
            if (payloads.any { it == "SHOUCANG_UPDATE" }) {
                val item = getItem(position)
                holder.dataBinding?.setVariable(modData, item)
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ModDataBean>() {
        override fun areItemsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem == newItem
        }
    }


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "我的消息") {
        var isLogin = BooleanObservableField(false)

        var messageResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()

        fun getMessageData(pageNum: Int, pageSize: Int) {
            modRequestWithMsg({
                apiService.getMessageList(pageNum, pageSize)
            }, messageResult)
        }
    }


}