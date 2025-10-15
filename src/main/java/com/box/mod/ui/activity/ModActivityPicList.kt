package com.box.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.recyclerview.widget.GridLayoutManager
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.ext.parseState
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.data.model.AppletsInfo
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.com.R as RC
import com.box.mod.R
import com.box.mod.databinding.ModActivityPicListBinding
import com.box.mod.ui.adapter.AppletPicAdapter
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar

class ModActivityPicList : BaseVmDbActivity<ModActivityPicListModel, ModActivityPicListBinding>() {
    var appletsInfoGson = ""
    var appPicList: MutableList<AppletsInfo> = mutableListOf()
    var appPicAdapter = AppletPicAdapter(appPicList)

    override fun layoutId(): Int = R.layout.mod_activity_pic_list

    companion object {
        const val INTENT_KEY_IN_APPLETS_INFO: String = "appletsInfo"
        fun start(context: Context, url: String) {
            if (TextUtils.isEmpty(url)) {
                return
            }
            val intent = Intent(context, ModActivityPicList::class.java)
            intent.putExtra(INTENT_KEY_IN_APPLETS_INFO, url)
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
        appletsInfoGson = intent.getStringExtra(INTENT_KEY_IN_APPLETS_INFO) ?: ""
        if(!StringUtils.isEmpty(appletsInfoGson)){
           mViewModel.appletsInfo.postValue(GsonUtils.fromJson(appletsInfoGson, AppletsInfo::class.java))
        }


        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 2).toInt()))
            adapter = appPicAdapter
        }

        appPicAdapter.setOnItemClickListener { adapter, view, position ->
            val appletsInfo = adapter.data[position] as AppletsInfo
            ModActivityPicDown.start(appContext, appletsInfo.pic2)
        }
    }

    override fun createObserver() {
        mViewModel.appletsInfo.observe(this){
            mViewModel.titleT.postValue(it.title)
            mViewModel.postDataAppApiByGameIdResul(it.id)
        }

        mViewModel.postDataAppApiByGameIdResult.observe(this) { it ->
            parseState(it, {
                it?.let {
                    appPicAdapter.setList(it.marketjson.list_data)
                }

            }, {
                Toaster.show(it.errorMsg)
            })
        }
    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {

    }


}