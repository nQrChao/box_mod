package com.box.mod.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import com.box.base.base.activity.BaseModVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.network.NetState
import com.box.mod.R
import com.box.mod.databinding.ModActivityMyGujiaShuomingBinding
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.immersionbar.immersionBar

@SuppressLint("CustomSplashScreen")
class ModActivityMyGuJiaShuoming :
    BaseModVmDbActivity<ModActivityMyGuJiaShuoming.Model, ModActivityMyGujiaShuomingBinding>() {

    override val mViewModel: Model by viewModels()
    override fun layoutId(): Int {
        return R.layout.mod_activity_my_gujia_shuoming
    }

    companion object {
        const val INTENT_KEY_TYPE_RANK: String = "rankType"
        var resultLauncher: ActivityResultLauncher<Intent>? = null
        fun start(context: Context) {
            val intent = Intent(context, ModActivityMyGuJiaShuoming::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }

        fun start(context: Context, rankType: Int) {
            val intent = Intent(context, ModActivityMyGuJiaShuoming::class.java)
            intent.putExtra(INTENT_KEY_TYPE_RANK, rankType)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }

    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        mViewModel.isSelect.set(intent.getIntExtra(INTENT_KEY_TYPE_RANK, 0))
        immersionBar {
            titleBar(mDataBinding.titleBar)
            navigationBarColor(com.box.com.R.color.white_pressed_color)
            statusBarDarkFont(true)
            init()
        }


    }

    override fun createObserver() {

    }

    override fun onNetworkStateChanged(netState: NetState) {
    }


    inner class ProxyClick {
        fun confirm() {

        }

    }


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "游戏账号估值常见问答") {
        var hasData = BooleanObservableField(false)
        var isSelect = IntObservableField(0)

    }


}