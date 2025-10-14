package com.chaoji.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModStateWithMsg
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.data.model.ModLiBaoBean
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityLibaoInfoBinding
import com.chaoji.mod.databinding.ModItemLibaoInfoListBinding
import com.chaoji.mod.ui.activity.login.ModActivityXDLogin
import com.chaoji.mod.ui.copyCodeX
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.ClickUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.common.R as RC


class ModActivityLiBaoInfo : BaseVmDbActivity<ModActivityLiBaoInfoModel, ModActivityLibaoInfoBinding>() {
    var gameID = ""
    var appList: MutableList<ModLiBaoBean> = mutableListOf()
    var appAdapter = ModLiBaoListAdapter(appList)

    override fun layoutId(): Int = R.layout.mod_activity_libao_info

    companion object {
        const val INTENT_KEY_IN_GAME_ID: String = "gameID"
        fun start(context: Context, gameID: String) {
            if (TextUtils.isEmpty(gameID)) {
                Toaster.show("数据错误")
                return
            }
            val intent = Intent(context, ModActivityLiBaoInfo::class.java)
            intent.putExtra(INTENT_KEY_IN_GAME_ID, gameID)
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
        gameID = intent.getStringExtra(INTENT_KEY_IN_GAME_ID) ?: ""
        if (!StringUtils.isEmpty(gameID)) {
            mViewModel.postLiBaoListBean(gameID)
        }

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 1)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 4).toInt()))
            adapter = appAdapter
        }

        appAdapter.addChildClickViewIds(R.id.lingqu)
        appAdapter.setOnItemClickListener { adapter, view, position ->
            val modLiBaoBean = adapter.data[position] as ModLiBaoBean

        }
        appAdapter.setOnItemChildClickListener { adapter, view, position ->
            if(view.id == R.id.lingqu){
                val modLiBaoBean = adapter.data[position] as ModLiBaoBean
                if(isLogin()){
                    mViewModel.postLiBaoLingQu(modLiBaoBean.cardid)
                }else{
                    ModActivityXDLogin.start(appContext)
                }
            }
        }

    }

    override fun createObserver() {
        mViewModel.postDataAppApiByGameIdResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    if (data != null) {
                        mViewModel.libaoInfo.value = data
                        mViewModel.hasData.set(true)
                        appAdapter.setList(data.cardlist)
                    }
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        mViewModel.postLiBaoLingQuResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    if (data != null) {
                        mViewModel.postLiBaoListBean(gameID)
                        copyCodeX(this@ModActivityLiBaoInfo, data.card)
                    }else{
                        Toaster.show(msg)
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

    class ModLiBaoListAdapter constructor(list: MutableList<ModLiBaoBean>) : BaseQuickAdapter<ModLiBaoBean, BaseDataBindingHolder<ModItemLibaoInfoListBinding>>(
        R.layout.mod_item_libao_info_list, list
    ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemLibaoInfoListBinding>, item: ModLiBaoBean) {
            holder.dataBinding?.setVariable(BR.modLiBaoBean, item)
        }
    }


    /**********************************************Click**************************************************/

    inner class ProxyClick {
        val aboutIconMultiClickListener: View.OnClickListener = object : ClickUtils.OnMultiClickListener(1) {
            override fun onTriggerClick(v: View) {
                // 触发后的逻辑
                Logs.d("MultiClick", "onTriggerClick: 连续点击已触发！")
                click1() // 调用你希望执行的最终方法
            }

            override fun onBeforeTriggerClick(v: View, count: Int) {
                // 触发前的逻辑 (可选)
                val remaining = 5 - count
                Toast.makeText(v.context, "再点击 $remaining 次", Toast.LENGTH_SHORT).show()
            }
        }

        fun click1() {

        }

    }


}