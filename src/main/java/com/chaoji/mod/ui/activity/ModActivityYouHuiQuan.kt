package com.chaoji.mod.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModStateWithMsg
import com.chaoji.base.network.NetState
import com.chaoji.im.data.model.ModMeYouHuiQuanBean
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.common.R as RC
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityYouhuiquanBinding
import com.chaoji.mod.ui.adapter.ModMeYouHuiQuanAdapter
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar

class ModActivityYouHuiQuan : BaseVmDbActivity<ModActivityYouHuiQuanModel, ModActivityYouhuiquanBinding>() {
    var appList: MutableList<ModMeYouHuiQuanBean> = mutableListOf()
    var appAdapter = ModMeYouHuiQuanAdapter(appList)

    override fun layoutId(): Int = R.layout.mod_activity_youhuiquan

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityYouHuiQuan::class.java)
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

        appAdapter.setOnItemClickListener { adapter, view, position ->
            val appletsInfo = adapter.data[position] as ModMeYouHuiQuanBean

        }

        mViewModel.postYouHuiQuan()
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

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {

    }


}