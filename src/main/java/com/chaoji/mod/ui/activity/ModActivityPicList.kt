package com.chaoji.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.recyclerview.widget.GridLayoutManager
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseState
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.common.R as RC
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityPicListBinding
import com.chaoji.mod.ui.adapter.AppletPicAdapter
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar

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