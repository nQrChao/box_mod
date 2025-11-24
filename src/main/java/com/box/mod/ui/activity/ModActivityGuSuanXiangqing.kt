package com.box.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.data.GameValuationCommitRequest
import com.box.common.data.model.ModDataBean
import com.box.common.data.model.ModValuationCommitBean
import com.box.common.eventViewModel
import com.box.common.network.apiService
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.utils.ext.logsE
import com.box.mod.BR.modData
import com.box.mod.BR.picUrl
import com.box.mod.BR.valuationCommitBean
import com.box.mod.R
import com.box.mod.databinding.ModActivityMyGusuanXiangqingBinding
import com.box.mod.databinding.ModItemCustomFormXiangqingBinding
import com.box.mod.databinding.ModItemCustomFormXiangqingPicBinding
import com.box.mod.databinding.ModItemGujiaGameReBinding
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.com.R as RC

class ModActivityGuSuanXiangqing : BaseVmDbActivity<ModActivityGuSuanXiangqing.Model, ModActivityMyGusuanXiangqingBinding>() {
    private val fromsAdapter = ModXiangqingFromsAdapter()
    private val gamesAdapter = ModXiangqingGamesAdapter(mutableListOf())

    private var picAdapter = ModXiangqingPicAdapter(mutableListOf())


    override fun layoutId(): Int = R.layout.mod_activity_my_gusuan_xiangqing

    companion object {
        const val INTENT_KEY_TYPE_GUJIA_ID: String = "gujiaId"
        fun start(context: Context, id: String) {
            val intent = Intent(context, ModActivityGuSuanXiangqing::class.java)
            intent.putExtra(INTENT_KEY_TYPE_GUJIA_ID, id)
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
        mViewModel.isLogin.set(eventViewModel.isLogin.value ?: false)
        mViewModel.getGameListData()
//        if (intent.getStringExtra(INTENT_KEY_TYPE_GUJIA_ID) != null) {
//            mViewModel.postValuationCommitDetail(
//                intent.getStringExtra(INTENT_KEY_TYPE_GUJIA_ID) ?: ""
//            )
//            mViewModel.getGameListData()
//        }

        mDataBinding.recyclerView3.run {
            layoutManager =  LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 3).toInt()))
            adapter = gamesAdapter
        }

        gamesAdapter.addChildClickViewIds(R.id.commit)
        gamesAdapter.setOnItemClickListener { adapter, view, position ->

        }

        gamesAdapter.setOnItemChildClickListener { adapter, view, position ->
            if(view.id == R.id.commit) {
                eventViewModel.setMainCurrentItem.value = 1
                eventViewModel.guJiaCurrentItem.value = position
                eventViewModel.closeMyGujiaActivity.value = true
                finish()
            }
        }



    }

    override fun createObserver() {
        mViewModel.gameListResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    gamesAdapter.setList(data)
                    // 将游戏列表保存到 ViewModel
                    mViewModel.gameList.value = data
                    //尝试进行匹配
                    tryMatchGameIcon()
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        mViewModel.postValuationCommitDetailResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    mViewModel.modData.value = data
                    fromsAdapter.updateList(data?.froms ?: mutableListOf())
                    picAdapter.setList(data?.fileNames ?: mutableListOf())
                    tryMatchGameIcon()
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**
     * 尝试匹配游戏图标
     * 只有在 gameList 和 modData 都加载完毕后才会执行
     */
    private fun tryMatchGameIcon() {
        // 获取两边的数据
        val detailData = mViewModel.modData.value
        val gameList = mViewModel.gameList.value
        // 检查是否两边都准备好了
        if (detailData == null || gameList == null) {
            logsE("数据尚未准备齐全，等待下一次调用...")
            return
        }
        //  检查是否已经匹配过了 (防止重复工作)
        if (detailData.gameIcon.isNotEmpty()) {
            return // 已经有图标了，不再匹配
        }
        // 查找匹配项
        val gameIdToMatch = detailData.gameId
        val matchedGame = gameList.find { it.id.toString() == gameIdToMatch }

        if (matchedGame != null) {
            logsE("匹配成功! GameID: $gameIdToMatch, Icon: ${matchedGame.url}")
            // 将图标 URL 赋值给 detailData 的 gameIcon 属性
            detailData.gameIcon = matchedGame.url // 假设 ModDataBean 中图标的属性名为 icon
            // 重新设置 LiveData 的 value，以通知 DataBinding 更新 UI
            mViewModel.modData.value = detailData
        } else {
            logsE("未能在 gameList 中找到匹配的 GameID: $gameIdToMatch")
        }
    }
    /**********************************************Click**************************************************/

    inner class ProxyClick {

    }

    class Model : BaseViewModel(title = "账号估算结果") {
        var isLogin = BooleanObservableField(false)
        var modData = MutableLiveData<GameValuationCommitRequest>()
        var gameList = MutableLiveData<MutableList<ModDataBean>>()
        var gameListResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        var postValuationCommitDetailResult =
            MutableLiveData<ModResultStateWithMsg<GameValuationCommitRequest>>()

        fun postValuationCommitDetail(id: String) {
            modRequestWithMsg(
                { apiService.getValuationCommitDetail(id) },
                postValuationCommitDetailResult,
            )
        }

        fun getGameListData() {
            modRequestWithMsg({
                apiService.getValuationCommitGameList()
            }, gameListResult)
        }

    }

    /**********************************************Adapter**************************************************/

    class ModXiangqingGamesAdapter constructor(list: MutableList<ModDataBean>) :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemGujiaGameReBinding>>(
            R.layout.mod_item_gujia_game_re, list
        ) {
        override fun convert(
            holder: BaseDataBindingHolder<ModItemGujiaGameReBinding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.setVariable(modData, item)
        }
    }

    class ModXiangqingPicAdapter constructor(list: MutableList<String>) :
        BaseQuickAdapter<String, BaseDataBindingHolder<ModItemCustomFormXiangqingPicBinding>>(
            R.layout.mod_item_custom_form_xiangqing_pic, list
        ) {
        override fun convert(
            holder: BaseDataBindingHolder<ModItemCustomFormXiangqingPicBinding>,
            item: String
        ) {
            holder.dataBinding?.setVariable(picUrl, item)
        }
    }

    class ModXiangqingPicDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }


    class ModXiangqingFromsAdapter :
        BaseQuickAdapter<ModValuationCommitBean, BaseDataBindingHolder<ModItemCustomFormXiangqingBinding>>(
            R.layout.mod_item_custom_form_xiangqing
        ) {
        private var lastPosition = -1
        override fun convert(
            holder: BaseDataBindingHolder<ModItemCustomFormXiangqingBinding>,
            item: ModValuationCommitBean
        ) {
            // 绑定逻辑保持不变
            holder.dataBinding?.let {
                it.setVariable(valuationCommitBean, item)
                it.executePendingBindings()
            }
            if (holder.layoutPosition > lastPosition) {
                val animation = AnimationUtils.loadAnimation(
                    holder.itemView.context,
                    R.anim.item_slide_up_fade_in_short
                )
                animation.startOffset = 50L * holder.layoutPosition.toLong()
                holder.itemView.startAnimation(animation)
                lastPosition = holder.layoutPosition
            }
        }

        override fun onViewRecycled(holder: BaseDataBindingHolder<ModItemCustomFormXiangqingBinding>) {
            holder.itemView.clearAnimation()
            super.onViewRecycled(holder)
        }

        fun resetAnimationState() {
            lastPosition = -1
        }

        fun updateList(newList: List<ModValuationCommitBean>) {
            setList(newList)
//            val diffResult = DiffUtil.calculateDiff(ModXiangqingFromsDiffCallback(data, newList))
//            data.clear()
//            data.addAll(newList)
//            diffResult.dispatchUpdatesTo(this)
        }

    }

    class ModXiangqingFromsDiffCallback(
        private val oldList: List<ModValuationCommitBean>,
        private val newList: List<ModValuationCommitBean>
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