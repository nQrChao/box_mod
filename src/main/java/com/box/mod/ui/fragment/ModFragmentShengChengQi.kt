package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
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
import com.box.common.utils.mmkv.MMKVConfig
import com.box.common.appContext
import com.box.common.data.model.ModDataBean
import com.box.common.network.apiService
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.ui.layout.StatusLayout
import com.box.common.utils.ext.logsE
import com.box.mod.BR.modData
import com.box.mod.BR.position
import com.box.mod.R
import com.box.mod.databinding.ModFragmentShengchengqiBinding
import com.box.mod.databinding.ModItemJueseListBinding
import com.box.mod.databinding.ModItemRoleTypeBinding
import com.box.mod.ui.activity.ModActivityShouCang
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

        roleTypeAdapter.setDiffCallback(RoleTypeDiffCallback())
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


        randomNameAdapter.setDiffCallback(RandomNameDiffCallback())
        mDataBinding.recyclerView2.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = randomNameAdapter
        }
        randomNameAdapter.addChildClickViewIds(R.id.copy, R.id.shoucang,R.id.del)
        randomNameAdapter.setOnItemClickListener { adapter, view, position ->

        }
        randomNameAdapter.setOnItemChildClickListener { adapter, view, position ->
            val currentList = adapter.data
            val clickedItem = currentList[position] as ModDataBean
            if (view.id == R.id.copy) {
                ClipboardUtils.copyText(clickedItem.name)
                Toaster.show("角色名已复制")
            }else if (view.id == R.id.del) {
                MMKVConfig.removeRandomNameList(clickedItem) //
                val latestList = MMKVConfig.getRandomName() //
                randomNameAdapter.setDiffNewData(latestList) //
                Toaster.show("角色名已删除")
                if(latestList.isEmpty()){ //
                    mDataBinding.randomNameLayout.visibility = View.GONE //
                }
                //adapter.removeAt(position)
            }  else if (view.id == R.id.shoucang) {
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
                randomNameAdapter.setDiffNewData(latestList) //
                if(newState) {
                    Toaster.show("收藏成功：${clickedItem.name}")
                }

            }
        }

        if (!MMKVConfig.getRandomName().isEmpty()) {
            mDataBinding.randomNameLayout.visibility = View.VISIBLE
            randomNameAdapter.setDiffNewData(MMKVConfig.getRandomName())
        }

    }


    override fun createObserver() {
        mViewModel.roleTypeResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    mViewModel.typeName.set(data?.get(0)?.dictLabel)
                    data?.firstOrNull()?.also { it.isSelect = true }
                    roleTypeAdapter.setDiffNewData(data)

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
                        randomNameAdapter.setDiffNewData(MMKVConfig.getRandomName())

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
                                    val currentPosition = randomNameAdapter.data.indexOfFirst { bean ->
                                        bean.name == data.name
                                    }
                                    if (currentPosition != -1) {
                                        randomNameAdapter.getItem(currentPosition).isShouCang = true
                                        randomNameAdapter.notifyItemChanged(currentPosition, "SHOUCANG_UPDATE")
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
            ModActivityShouCang.start(appContext)
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
        randomNameAdapter.setDiffNewData(latestList)
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
        override fun convert(
            holder: BaseDataBindingHolder<ModItemRoleTypeBinding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.setVariable(modData, item)
            holder.dataBinding?.setVariable(position, holder.bindingAdapterPosition)
        }
    }

    class RoleTypeDiffCallback : DiffUtil.ItemCallback<ModDataBean>() {
        override fun areItemsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem.dictValue == newItem.dictValue
        }

        override fun areContentsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem == newItem
        }
    }


    class RandomNameAdapter constructor() : BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemJueseListBinding>>(
            R.layout.mod_item_juese_list
        ) {
        override fun convert(
            holder: BaseDataBindingHolder<ModItemJueseListBinding>,
            item: ModDataBean
        ) {
            holder.dataBinding?.setVariable(modData, item)
            holder.dataBinding?.executePendingBindings()
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
    }

    class RandomNameDiffCallback : DiffUtil.ItemCallback<ModDataBean>() {
        override fun areItemsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ModDataBean, newItem: ModDataBean): Boolean {
            return oldItem == newItem
        }
    }


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "角色名生成器") {
        var pic = IntObservableField(0)
        var isSelect = IntObservableField(3)
        var typeName = StringObservableField("")
        var lengthName = StringObservableField("三字")
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


