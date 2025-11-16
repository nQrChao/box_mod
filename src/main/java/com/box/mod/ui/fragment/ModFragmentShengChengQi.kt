package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.action.StatusAction
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appContext
import com.box.common.data.model.ModDataBean
import com.box.common.network.apiService
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.ui.layout.StatusLayout
import com.box.common.utils.ext.logsE
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.BR.modData
import com.box.mod.BR.position
import com.box.mod.R
import com.box.mod.databinding.ModFragmentShengchengqiBinding
import com.box.mod.databinding.ModItemJueseListBinding
import com.box.mod.databinding.ModItemRoleTypeBinding
import com.box.mod.ui.activity.ModActivityLogin
import com.box.mod.ui.activity.ModActivityMyShouCang
import com.box.mod.ui.xpop.ModXPopupCenterShengChengQi
import com.box.other.blankj.utilcode.util.ClipboardUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.com.R as RC

class ModFragmentShengChengQi :
    BaseTitleBarFragment<ModFragmentShengChengQi.Model, ModFragmentShengchengqiBinding>(),
    StatusAction {
    private var randomNameAdapter = RandomNameAdapter()
    private var roleTypeAdapter = RoleTypeAdapter(mutableListOf())


    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_shengchengqi

    companion object {
        fun newInstance(): ModFragmentShengChengQi {
            return ModFragmentShengChengQi()
        }
    }

    override fun lazyLoadData() {
        showLoading()
        mViewModel.getRoleTypeData()
    }

    /**
     * 加载状态
     */
    override fun getStatusLayout(): StatusLayout {
        return mDataBinding.statusLoading
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()

        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 10).toInt()))
            adapter = roleTypeAdapter
        }
        roleTypeAdapter.setOnItemClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            if (clickedItem.isSelect) {
                return@setOnItemClickListener
            }
            val oldSelectedPosition = currentList.indexOfFirst { (it as ModDataBean).isSelect }
            if (oldSelectedPosition != -1) {
                (currentList[oldSelectedPosition] as ModDataBean).isSelect = false
            }
            clickedItem.isSelect = true
            mViewModel.dataBean.value = clickedItem
            if (oldSelectedPosition != -1) {
                adapter.notifyItemChanged(oldSelectedPosition) // 更新旧的
            }
            mViewModel.typeName.set(clickedItem.dictLabel)
            adapter.notifyItemChanged(position) // 更新新的
        }


        mDataBinding.recyclerView2.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = randomNameAdapter
        }
        randomNameAdapter.addChildClickViewIds(R.id.copy, R.id.shoucang, R.id.del)
        randomNameAdapter.setOnItemClickListener { adapter, view, position ->

        }
        randomNameAdapter.setOnItemChildClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            if (view.id == R.id.copy) {
                ClipboardUtils.copyText(clickedItem.name)
                Toaster.show("角色名已复制")
            } else if (view.id == R.id.del) {
                MMKVConfig.removeRandomNameList(clickedItem) //
                val latestList = MMKVConfig.getRandomName() //
                randomNameAdapter.updateList(latestList) //
                Toaster.show("角色名已删除")
                if (latestList.isEmpty()) { //
                    mDataBinding.randomNameLayout.visibility = View.GONE //
                }
                //adapter.removeAt(position)
            } else if (view.id == R.id.shoucang) {
//                val newState = !clickedItem.isShouCang // 计算新状态
//                MMKVConfig.updateRandomNameStatusByName(clickedItem.name, newState)
//                val latestList = MMKVConfig.getRandomName()
//                randomNameAdapter.setDiffNewData(latestList)
//                if(newState) {
//                    Toaster.show("收藏成功：${clickedItem.name}")
//                }
//                clickedItem.isShouCang = newState
//                adapter.notifyItemChanged(position, "SHOUCANG_UPDATE")

                val newState = !clickedItem.isShouCang
                MMKVConfig.updateRandomNameStatusByName(clickedItem.name, newState) //
                val latestList = MMKVConfig.getRandomName() //
                randomNameAdapter.updateList(latestList) //
                if (newState) {
                    Toaster.show("收藏成功：${clickedItem.name}")
                }

            }
        }

        if (!MMKVConfig.getRandomName().isEmpty()) {
            mDataBinding.randomNameLayout.visibility = View.VISIBLE
            randomNameAdapter.updateList(MMKVConfig.getRandomName())
        }

    }


    override fun createObserver() {
        mViewModel.roleTypeResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data != null) {
                        roleTypeAdapter.updateList(data)
                    }
                    roleTypeAdapter.resetAnimationState()
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
            showComplete()
        }

        mViewModel.randomNameResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    if (data != null) {
                        data.isShouCang = false
                        mDataBinding.randomNameLayout.visibility = View.VISIBLE
                        MMKVConfig.addRandomNameList(data)
                        randomNameAdapter.updateList(MMKVConfig.getRandomName())
                        randomNameAdapter.resetAnimationState()
                        XPopup.Builder(context)
                            .dismissOnTouchOutside(false)
                            .dismissOnBackPressed(false)
                            .isDestroyOnDismiss(true)
                            .hasStatusBar(true)
                            .isLightStatusBar(true)
                            .animationDuration(5)
                            .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                            .hasNavigationBar(true)
                            .asCustom(
                                ModXPopupCenterShengChengQi(
                                    mActivity,
                                    data
                                ) {
                                    MMKVConfig.updateRandomNameStatusByName(data.name, true)
                                    val currentPosition =
                                        randomNameAdapter.data.indexOfFirst { bean ->
                                            bean.name == data.name
                                        }
                                    if (currentPosition != -1) {
                                        randomNameAdapter.getItem(currentPosition).isShouCang = true
                                        randomNameAdapter.notifyItemChanged(
                                            currentPosition,
                                            "SHOUCANG_UPDATE"
                                        )
                                    }
                                    Toaster.show("收藏成功")
                                })
                            .show()
                    }


                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }
    }


    override fun onNetworkStateChanged(it: NetState) {
    }


    override fun onRightClick(view: TitleBar) {
        super.onRightClick(view)
        if (isLogin()) {
            ModActivityMyShouCang.start(appContext)
        } else {
            Toaster.show("请先登录")
            ModActivityLogin.start(appContext)
        }
    }

    override fun onResume() {
        super.onResume()
        val latestList = MMKVConfig.getRandomName()
        if (latestList.isEmpty()) {
            mDataBinding.randomNameLayout.visibility = View.GONE
        } else {
            mDataBinding.randomNameLayout.visibility = View.VISIBLE
        }
        randomNameAdapter.updateList(latestList)
    }

    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun text3() {
            mViewModel.lengthName.set("三字")
            mViewModel.isSelect.set(3)
        }

        fun text4() {
            mViewModel.lengthName.set("四字")
            mViewModel.isSelect.set(4)
        }

        fun text5() {
            mViewModel.lengthName.set("五字以上")
            mViewModel.isSelect.set(5)
        }

        fun confirm() {
            if (mViewModel.typeName.get() == "0") {
                mDataBinding.recyclerView.startAnimation(
                    AnimationUtils.loadAnimation(
                        appContext,
                        RC.anim.shake_anim
                    )
                )
                Toaster.show("请选择类型")
                return
            }
            if (mViewModel.lengthName.get() == "0") {
                mDataBinding.lengthLayout.startAnimation(
                    AnimationUtils.loadAnimation(
                        appContext,
                        RC.anim.shake_anim
                    )
                )
                Toaster.show("请选择长度")
                return
            }
            val currentList = roleTypeAdapter.data
            val selectedItem: ModDataBean? = currentList.toList().find { it.isSelect }
            if (selectedItem != null) {
                mViewModel.getRandomNameData(
                    selectedItem.dictValue.toInt(),
                    mViewModel.isSelect.get()
                )
            } else {
                logsE("错误：没有找到任何选中的项")
            }
        }

    }

    /**********************************************Adapter**************************************************/
    class RoleTypeAdapter constructor(list: MutableList<ModDataBean>) :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemRoleTypeBinding>>(
            R.layout.mod_item_role_type, list
        ) {
        private var lastPosition = -1
        override fun convert(
            holder: BaseDataBindingHolder<ModItemRoleTypeBinding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.setVariable(modData, item)
            holder.dataBinding?.setVariable(position, holder.bindingAdapterPosition)
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

        override fun onViewRecycled(holder: BaseDataBindingHolder<ModItemRoleTypeBinding>) {
            holder.itemView.clearAnimation()
            super.onViewRecycled(holder)
        }

        fun resetAnimationState() {
            lastPosition = -1
        }

        fun updateList(newList: MutableList<ModDataBean>) {
            val diffResult = DiffUtil.calculateDiff(RoleTypeDiffCallback(data, newList))
            data.clear()
            data.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    class RoleTypeDiffCallback(
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


    class RandomNameAdapter constructor() :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemJueseListBinding>>(
            R.layout.mod_item_juese_list
        ) {

        private var lastPosition = -1
        override fun convert(
            holder: BaseDataBindingHolder<ModItemJueseListBinding>,
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
            holder: BaseDataBindingHolder<ModItemJueseListBinding>,
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

        override fun onViewRecycled(holder: BaseDataBindingHolder<ModItemJueseListBinding>) {
            holder.itemView.clearAnimation()
            super.onViewRecycled(holder)
        }

        fun resetAnimationState() {
            lastPosition = -1
        }

        fun updateList(newList: List<ModDataBean>) {
            setList(newList)
//            val diffResult = DiffUtil.calculateDiff(RandomNameDiffCallback(data, newList))
//            data.clear()
//            data.addAll(newList)
//            diffResult.dispatchUpdatesTo(this)
        }
    }

    class RandomNameDiffCallback(
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
    class Model : BaseViewModel(title = "角色名生成器") {
        var pic = IntObservableField(0)
        var isSelect = IntObservableField(0)
        var typeName = StringObservableField("0")
        var lengthName = StringObservableField("0")
        var dataBean = MutableLiveData<ModDataBean>()
        var roleTypeResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()
        var randomNameResult = MutableLiveData<ModResultStateWithMsg<ModDataBean>>()

        fun getRoleTypeData() {
            modRequestWithMsg({
                apiService.getRoleType()
            }, roleTypeResult)
        }

        fun getRandomNameData(roleType: Int, length: Int) {
            modRequestWithMsg({
                apiService.getRandomName(roleType, length)
            }, randomNameResult)
        }


    }


}


