package com.box.mod.ui.activity.jiaoyi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.sdk.appViewModel
import com.box.mod.R
import com.box.mod.databinding.ModActivityJiaoyiShimingBinding
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.common.R as RC

class ModActivityJiaoYiShiMing : BaseVmDbActivity<ModActivityJiaoYiShiMingModel, ModActivityJiaoyiShimingBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_shiming

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityJiaoYiShiMing::class.java)
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
        mViewModel.shiMingResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    mViewModel.postRealName()
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }
        mViewModel.modUserRealName.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    Logs.e("modUserRealName:${GsonUtils.toJson(data)}")
                    appViewModel.modUserRealName.postValue(data)
                    finish()
                },
                onError = {
                    finish()
                    Toaster.show(it.msg)
                }
            )
        }


    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun shiMing() {
            Toaster.show(mViewModel.rName.get())
            if (StringUtils.isEmpty(mViewModel.rName.get())) {
                mDataBinding.editRealName.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                //Toaster.show("请输入姓名")
                return
            }
            if (StringUtils.isEmpty(mViewModel.cardId.get()) || mViewModel.cardId.get().length < 15) {
                mDataBinding.editIdCardNumber.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("请输入正确的身份证号码")
                return
            }
            mViewModel.postShiMing()
        }

    }


}