package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.action.StatusAction
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.data.model.ModDataBean
import com.box.common.data.model.ModYouHuiQuanBean
import com.box.common.network.apiService
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.common.ui.layout.StatusLayout
import com.box.common.utils.logsE
import com.box.mod.BR.modData
import com.box.mod.BR.position
import com.box.mod.R
import com.box.mod.databinding.ModFragmentShengchengqiBinding
import com.box.mod.databinding.ModItemRoleTypeBinding
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import kotlin.text.get


class ModFragmentShengChengQi : BaseTitleBarFragment<ModFragmentShengChengQi.Model, ModFragmentShengchengqiBinding>() , StatusAction{
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
            // 如果点击的已经是选中的，直接返回，不做任何操作
            if (clickedItem.isSelect) {
                return@setOnItemClickListener
            }
            // 查找之前选中的 item 的位置
            val oldSelectedPosition = currentList.indexOfFirst { (it as ModDataBean).isSelect }
            // 更新数据模型
            // 如果存在旧的选中项，将其取消选中
            if (oldSelectedPosition != -1) {
                (currentList[oldSelectedPosition] as ModDataBean).isSelect = false
            }
            //设置新点击的项为选中
            clickedItem.isSelect = true
            // 更新 ViewModel
            mViewModel.dataBean.value = clickedItem
            // 使用 notifyItemChanged 通知适配器
            // 这将触发默认的 item 动画 (如 cross-fade)，而不是闪烁
            if (oldSelectedPosition != -1) {
                adapter.notifyItemChanged(oldSelectedPosition) // 更新旧的
            }
            adapter.notifyItemChanged(position) // 更新新的
        }

    }


    override fun createObserver() {
        mViewModel.roleTypeResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE(GsonUtils.toJson(data))
                    roleTypeAdapter.setDiffNewData(data)

                },
                onError = {
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
        fun text3() {
            mViewModel.isSelect.set(0)
        }
        fun text4() {
            mViewModel.isSelect.set(1)
        }
        fun text5() {
            mViewModel.isSelect.set(2)
        }
        fun confirm() {

        }

    }

    /**********************************************Adapter**************************************************/
    class RoleTypeAdapter constructor(list: MutableList<ModDataBean>) :
        BaseQuickAdapter<ModDataBean, BaseDataBindingHolder<ModItemRoleTypeBinding>>(
            R.layout.mod_item_role_type, list
        ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemRoleTypeBinding>, item: ModDataBean) {
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


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "角色名生成器") {
        var pic = IntObservableField(0)
        var isSelect = IntObservableField(0)
        var dataBean = MutableLiveData<ModDataBean>()

        var roleTypeResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModDataBean>>>()

        fun getRoleTypeData() {
            modRequestWithMsg({
                apiService.getRoleType()
            }, roleTypeResult)
        }


    }


}


