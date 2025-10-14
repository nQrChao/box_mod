package com.box.mod.ui.activity.jiaoyi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.network.NetState
import com.box.common.data.model.ModTradeGoodDetailBean
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.utils.MMKVUtil
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModActivityJiaoyiShoucangBinding
import com.box.mod.databinding.ModItemJiaoyiShoucangBinding
import com.box.common.R as RC
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar

class ModActivityShouCang : BaseVmDbActivity<ModActivityShouCangModel, ModActivityJiaoyiShoucangBinding>() {
    var appList: MutableList<ModTradeGoodDetailBean> = mutableListOf()
    var appListAdapter = AppletsShouCangAdapter(appList)
    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_shoucang

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityShouCang::class.java)
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

        mViewModel.hasTradeGoods.set(MMKVUtil.getShouCangInfoList().isNotEmpty())
        if (MMKVUtil.getShouCangInfoList().isNotEmpty()) {
            appListAdapter.setDiffCallback(AppletsShouCangDiffCallback())
            mDataBinding.recyclerView.run {
                layoutManager = GridLayoutManager(context, 1)
                addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
                adapter = appListAdapter
            }

            appListAdapter.addChildClickViewIds(R.id.cancel_shoucang)
            appListAdapter.setOnItemClickListener { adapter, view, position ->
                val goodDetail = adapter.data[position] as ModTradeGoodDetailBean
                ModActivityTradeDetails.start(this@ModActivityShouCang, goodDetail.gid)
            }

            appListAdapter.setOnItemChildClickListener() { adapter, view, position ->
                if (view.id == R.id.cancel_shoucang) {
                    val goodDetail = adapter.data[position] as ModTradeGoodDetailBean
                    val isNowCollected = MMKVUtil.toggleShouCang(goodDetail)
                    Toaster.show(if (isNowCollected) "收藏成功" else "已取消收藏")
                    appListAdapter.setDiffNewData(MMKVUtil.getShouCangInfoList())
                    mViewModel.hasTradeGoods.set(MMKVUtil.getShouCangInfoList().isNotEmpty())
                    adapter.notifyItemChanged(position, "LIKE_UPDATE")
                }
            }

            appListAdapter.setDiffNewData(MMKVUtil.getShouCangInfoList())

        }


    }

    override fun createObserver() {

    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun exitPreview() {
            //退出预览
            finish()
        }

    }

    class AppletsShouCangAdapter constructor(list: MutableList<ModTradeGoodDetailBean>) : BaseQuickAdapter<ModTradeGoodDetailBean, BaseDataBindingHolder<ModItemJiaoyiShoucangBinding>>(
        R.layout.mod_item_jiaoyi_shoucang, list
    ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemJiaoyiShoucangBinding>, item: ModTradeGoodDetailBean) {
            holder.dataBinding?.setVariable(BR.goodDetailBean, item)
        }

        override fun onBindViewHolder(holder: BaseDataBindingHolder<ModItemJiaoyiShoucangBinding>, position: Int, payloads: MutableList<Any>) {
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

    class AppletsShouCangDiffCallback : DiffUtil.ItemCallback<ModTradeGoodDetailBean>() {
        override fun areItemsTheSame(oldItem: ModTradeGoodDetailBean, newItem: ModTradeGoodDetailBean): Boolean {
            return oldItem.gid == newItem.gid
        }

        override fun areContentsTheSame(oldItem: ModTradeGoodDetailBean, newItem: ModTradeGoodDetailBean): Boolean {
            return oldItem == newItem
        }
    }


}