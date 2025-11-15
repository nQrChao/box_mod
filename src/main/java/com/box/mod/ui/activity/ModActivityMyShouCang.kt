package com.box.mod.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.activity.BaseModVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.network.NetState
import com.box.common.data.model.ModDataBean
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.utils.mmkv.MMKVConfig
import com.box.common.utils.mmkv.MMKVConfig.gameRankList
import com.box.mod.BR.modData
import com.box.mod.R
import com.box.mod.databinding.ModActivityShoucangBinding
import com.box.mod.databinding.ModItemRankShoucangBinding
import com.box.mod.ui.fragment.ModFragmentShengChengQi.RandomNameAdapter
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.ClipboardUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

@SuppressLint("CustomSplashScreen")
class ModActivityMyShouCang :
    BaseModVmDbActivity<ModActivityMyShouCang.Model, ModActivityShoucangBinding>() {
    private var type = 0

    var rankList: MutableList<ModDataBean> = mutableListOf()
    var randomNameList: MutableList<ModDataBean> = mutableListOf()
    private val rankListAdapter = ModGameRankShoucangAdapter()
    private var randomNameAdapter = RandomNameAdapter()


    override val mViewModel: Model by viewModels()
    override fun layoutId(): Int {
        return R.layout.mod_activity_shoucang
    }

    companion object {
        const val INTENT_KEY_TYPE_RANK: String = "rankType"
        var resultLauncher: ActivityResultLauncher<Intent>? = null
        fun start(context: Context) {
            val intent = Intent(context, ModActivityMyShouCang::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }

        fun start(context: Context,rankType:Int) {
            val intent = Intent(context, ModActivityMyShouCang::class.java)
            intent.putExtra(INTENT_KEY_TYPE_RANK, rankType)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }

    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        mViewModel.isSelect.set(intent.getIntExtra(INTENT_KEY_TYPE_RANK,0))
        immersionBar {
            titleBar(mDataBinding.titleBar)
            navigationBarColor(com.box.com.R.color.white_pressed_color)
            statusBarDarkFont(true)
            init()
        }

        mDataBinding.tab.tabDefaultIndex = intent.getIntExtra(INTENT_KEY_TYPE_RANK,0)
        mDataBinding.tab.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            mViewModel.isSelect.set(toIndex)
            type = when (toIndex) {
                0 -> {
                    randomNameList = MMKVConfig.getShouCangNameList()
                    mViewModel.hasData.set(!randomNameList.isEmpty())
                    0

                }

                1 -> {
                    rankList = gameRankList
                    mViewModel.hasData.set(!gameRankList.isEmpty())
                    1
                }

                else -> 0
            }
            mDataBinding.root.postDelayed({
            }, 500) // 延迟500毫秒
        }

        rankList = MMKVConfig.getGameRank()
        randomNameList = MMKVConfig.getShouCangNameList()

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = randomNameAdapter
        }
        randomNameAdapter.addChildClickViewIds(R.id.shoucang,R.id.del,R.id.copy)
        randomNameAdapter.setOnItemChildClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            if (view.id == R.id.shoucang) {
                if (clickedItem.isShouCang) {
                    clickedItem.isShouCang = false
                    MMKVConfig.updateRandomNameStatusByName(clickedItem.name, false)
                } else {
                    Toaster.show("收藏成功")
                    clickedItem.isShouCang = true
                    MMKVConfig.updateRandomNameStatusByName(clickedItem.name, true)
                }
                adapter.removeAt(position)
                if(adapter.data.isEmpty()){
                    mViewModel.hasData.set(false)
                }
            } else if (view.id == R.id.del) {
                MMKVConfig.removeRandomNameList(clickedItem)
                adapter.removeAt(position)
                Toaster.show("角色名已删除")
                if(adapter.data.isEmpty()){
                    mViewModel.hasData.set(false)
                }
            }else if (view.id == R.id.copy) {
                ClipboardUtils.copyText(clickedItem.name)
                Toaster.show("角色名已复制")
            }
        }
        randomNameAdapter.setList(randomNameList)



        mDataBinding.recyclerView2.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = rankListAdapter
        }

        rankListAdapter.addChildClickViewIds(R.id.shoucang)
        rankListAdapter.setOnItemChildClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            if (view.id == R.id.shoucang) {
                if (clickedItem.isShouCang) {
                    clickedItem.isShouCang = false
                    MMKVConfig.removeGameRankList(clickedItem)
                    Toaster.show("已取消收藏")
                    adapter.removeAt(position)
                } else {
                    clickedItem.isShouCang = true
                    MMKVConfig.addGameRankList(clickedItem)
                    Toaster.show("收藏成功")
                }

                adapter.notifyItemChanged(position, "SHOUCANG_UPDATE")

            }
        }

        rankListAdapter.setList(rankList)


    }

    override fun createObserver() {

    }

    override fun onNetworkStateChanged(netState: NetState) {
    }


    inner class ProxyClick {
        fun confirm() {

        }

    }


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "我的收藏") {
        var hasData = BooleanObservableField(false)
        var isSelect = IntObservableField(0)

    }

    class ModGameRankShoucangAdapter : BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemRankShoucangBinding>>(
        R.layout.mod_item_rank_shoucang
    ) {
        private var lastPosition = -1
        override fun convert(holder: BaseDataBindingHolder<ModItemRankShoucangBinding>, item: ModDataBean) {
            // 绑定逻辑保持不变
            holder.dataBinding?.let {
                it.setVariable(modData, item)
                it.executePendingBindings()
            }
            if (holder.layoutPosition > lastPosition) {
                val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_slide_up_fade_in_short)
                animation.startOffset = 50L * holder.layoutPosition.toLong()
                holder.itemView.startAnimation(animation)
                lastPosition = holder.layoutPosition
            }
        }
        override fun onViewRecycled(holder: BaseDataBindingHolder<ModItemRankShoucangBinding>) {
            holder.itemView.clearAnimation()
            super.onViewRecycled(holder)
        }

        fun resetAnimationState() {
            lastPosition = -1
        }

        fun updateList(newList: List<ModDataBean>) {
            val diffResult = DiffUtil.calculateDiff(ModGameRankShoucangDiffCallback(data, newList))
            data.clear()
            data.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }

    }

    class  ModGameRankShoucangDiffCallback(
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