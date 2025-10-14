package com.chaoji.mod.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModStateWithMsg
import com.chaoji.base.network.NetState
import com.chaoji.im.data.model.ModMeYouHuiQuanBean
import com.chaoji.im.data.model.ModMyLiBaoBean
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.mod.BR
import com.chaoji.common.R as RC
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityMyLibaoBinding
import com.chaoji.mod.databinding.ModItemLibaoMyListBinding
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.ClipboardUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar

class ModActivityMyLiBao : BaseVmDbActivity<ModActivityMyLiBaoModel, ModActivityMyLibaoBinding>() {
    var appList: MutableList<ModMyLiBaoBean> = mutableListOf()
    var appAdapter = ModMyLiBaoListAdapter(appList)

    override fun layoutId(): Int = R.layout.mod_activity_my_libao

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityMyLiBao::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 4).toInt()))
            adapter = appAdapter
        }

        appAdapter.addChildClickViewIds(R.id.copy)
        appAdapter.setOnItemClickListener { adapter, view, position ->
            val myLiBaoBean = adapter.data[position] as ModMyLiBaoBean

        }

        appAdapter.setOnItemChildClickListener { adapter, view, position ->
            val myLiBaoBean = adapter.data[position] as ModMyLiBaoBean
            ClipboardUtils.copyText(myLiBaoBean.card)
            Toaster.show("礼包码已复制")
        }

        mViewModel.postMyLiBao()
    }

    override fun createObserver() {
        mViewModel.postDataAppApiByGameIdResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    if(data!=null){
                        mViewModel.hasData.set(true)
                        appAdapter.setList(data)
                    }
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

    }

    class ModMyLiBaoListAdapter constructor(list: MutableList<ModMyLiBaoBean>) : BaseQuickAdapter<ModMyLiBaoBean, BaseDataBindingHolder<ModItemLibaoMyListBinding>>(
        R.layout.mod_item_libao_my_list, list
    ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemLibaoMyListBinding>, item: ModMyLiBaoBean) {
            holder.dataBinding?.setVariable(BR.modMyLiBaoBean, item)
        }
    }



    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {

    }


}