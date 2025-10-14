package com.chaoji.mod.ui.fragment.fragment1

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.base.base.action.StatusAction

import com.chaoji.base.base.fragment.BaseTitleBarFragment
import com.chaoji.base.ext.parseModState
import com.chaoji.base.network.NetState
import com.chaoji.mod.BR
import com.chaoji.im.appContext
import com.chaoji.im.data.model.AppletsLeYuan
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.im.ui.layout.StatusLayout
import com.chaoji.mod.R
import com.chaoji.mod.databinding.FragmentNavigation14Binding
import com.chaoji.mod.databinding.ModItemAppletsReliaoBinding
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar

class FragmentNavigation14 : BaseTitleBarFragment<Navigation14Model, FragmentNavigation14Binding>() , StatusAction {
    var appList: MutableList<AppletsLeYuan> = mutableListOf()
    var appListAdapter = AppletsReLiaoAdapter(appList)

    override fun layoutId(): Int = R.layout.fragment_navigation1_4

    companion object {
        fun newInstance(): FragmentNavigation14 {
            return FragmentNavigation14()
        }
    }


    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        showLoading()
        immersionBar {
            statusBarDarkFont(true)
            init()
        }
        appListAdapter.setDiffCallback(AppletsInfoReLiaoDiffCallback())

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = appListAdapter
        }


        appListAdapter.addChildClickViewIds(R.id.dianzanLayout)
        appListAdapter.setOnItemClickListener { adapter, view, position ->
            val applets = adapter.data[position] as AppletsLeYuan
            CommonActivityBrowser.start(appContext, applets.redirect)
        }

        appListAdapter.setOnItemChildClickListener() { adapter, view, position ->
            if (view.id == R.id.dianzanLayout) {
                val applets = adapter.data[position] as AppletsLeYuan
                applets.select = !applets.select
                adapter.notifyItemChanged(position, "LIKE_UPDATE")
            }
        }

        mDataBinding.sendEdit.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                mDataBinding.click?.search(textView)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }

    override fun initData() {

    }

    override fun createObserver() {
        mViewModel.postDataAppApiByDataIdResult.observe(this) { it ->
            parseModState(it, {
                it?.let {
                    appList = it.marketjson.list_data
                    appListAdapter.setDiffNewData(appList)
                }

            }, {
                Toaster.show(it.errorLog)
            })
            showComplete()
        }


    }

    override fun lazyLoadData() {
        mViewModel.updateConversation()
        mViewModel.postDataAppApiByDataId("404")
    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun search(view: View) {
            if (StringUtils.isEmpty(mViewModel.sendKey.get())) {
                Toaster.show("请输入发送内容")
                return
            }
            val keys = mViewModel.sendKey.get() ?: ""
            Toaster.show("提交成功，请等待审核通过！")
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            mViewModel.sendKey.set("")
        }

    }

    class AppletsReLiaoAdapter constructor(list: MutableList<AppletsLeYuan>) : BaseQuickAdapter<AppletsLeYuan, BaseDataBindingHolder<ModItemAppletsReliaoBinding>>(
        R.layout.mod_item_applets_reliao, list
    ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemAppletsReliaoBinding>, item: AppletsLeYuan) {
            holder.dataBinding?.setVariable(BR.appletInfoLeYuan, item)
        }

        override fun onBindViewHolder(holder: BaseDataBindingHolder<ModItemAppletsReliaoBinding>, position: Int, payloads: MutableList<Any>) {
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
                    binding?.setVariable(BR.appletInfoLeYuan, item)
                }
            }
        }

    }

    class AppletsInfoReLiaoDiffCallback : DiffUtil.ItemCallback<AppletsLeYuan>() {
        override fun areItemsTheSame(oldItem: AppletsLeYuan, newItem: AppletsLeYuan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppletsLeYuan, newItem: AppletsLeYuan): Boolean {
            return oldItem == newItem
        }
    }

    override fun getStatusLayout(): StatusLayout? {
        return mDataBinding.hlHint
    }
}




