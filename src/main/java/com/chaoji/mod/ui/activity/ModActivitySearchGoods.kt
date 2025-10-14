package com.chaoji.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.network.NetState
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityJiaoyiSearchGoodsBinding
import com.chaoji.common.R as RC
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.immersionbar.immersionBar

class ModActivitySearchGoods : BaseVmDbActivity<ModActivitySearchGoodsModel, ModActivityJiaoyiSearchGoodsBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_search_goods

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivitySearchGoods::class.java)
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
    }



    override fun createObserver() {

    }


    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun leftIcon() {

        }
        fun search() {

        }

        fun del() {

        }


    }


}