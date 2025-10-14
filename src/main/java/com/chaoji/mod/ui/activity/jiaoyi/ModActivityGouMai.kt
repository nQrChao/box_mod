package com.chaoji.mod.ui.activity.jiaoyi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModStateWithMsg
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.im.sdk.appViewModel
import com.chaoji.common.R as RC
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityJiaoyiGoumaiBinding
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.other.xpopup.XPopup

class ModActivityGouMai : BaseVmDbActivity<ModActivityGouMaiModel, ModActivityJiaoyiGoumaiBinding>() {
    var goodId = ""
    private var gouMaiLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // 成功！
                mViewModel.tradeGoodDetail.value?.goods_price?.let { it1 ->
                    mViewModel.postPayGetOrder(it1)
                }
            } else {
                Toaster.show("您需要实名后才可购买")
            }
        }
    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_goumai

    companion object {
        const val INTENT_KEY_GOOD_ID: String = "goodId"
        fun start(context: Context, tradeGoodDetail: String) {
            if (TextUtils.isEmpty(tradeGoodDetail)) {
                return
            }
            val intent = Intent(context, ModActivityGouMai::class.java)
            intent.putExtra(INTENT_KEY_GOOD_ID, tradeGoodDetail)
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
        goodId = intent.getStringExtra(INTENT_KEY_GOOD_ID) ?: ""
        if (!StringUtils.isEmpty(goodId)) {
            mViewModel.tradeGoodDetail.value = GsonUtils.fromJson(getString(INTENT_KEY_GOOD_ID), ModTradeGoodDetailBean::class.java)
        }

        mDataBinding.aliCheck.setOnCheckedChangeListener { buttonView, isChecked ->
//            mDataBinding.aliCheck.isChecked = !isChecked
            mViewModel.payType.set(if (isChecked) 1 else 2)
        }
        mDataBinding.wxCheck.setOnCheckedChangeListener { buttonView, isChecked ->
//            mDataBinding.wxCheck.isChecked = !isChecked
            mViewModel.payType.set(if (isChecked) 2 else 1)
        }

        XPopup.Builder(this@ModActivityGouMai)
            .isDestroyOnDismiss(true)
            .hasStatusBar(true)
            .dismissOnTouchOutside(false)
            .dismissOnBackPressed(false)
            .isLightStatusBar(true)
            .autoFocusEditText(false)
            .autoOpenSoftInput(false)
            .navigationBarColor(ColorUtils.getColor(com.chaoji.common.R.color.white))
            .hasNavigationBar(true)
            .asCustom(
                ModXPopupJiaoyiBottomGouMai(this@ModActivityGouMai, {
                    //取消
                    finish()
                }, {
                    //购买
                })
            )
            .show()


    }

    override fun createObserver() {
        mViewModel.payResult.observe(this){ resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    if(!data?.act.isNullOrEmpty()){
                        data?.pay_url?.let {
                            ModActivityPayWeb.start(appContext, it)
                        }
                    }else{
                        Toaster.show(msg)
                    }
                },
                onError = {
                    if(it.msg.contains("未成年，暂时无法支付")){
                        if(appViewModel.modUserRealName.value?.isRealName() == false){
                            Toaster.show("未实名认证，请先进行实名认证")
                            val intent = Intent(appContext, ModActivityJiaoYiShiMing::class.java)
                            intent.putExtra("goumai", "shiming")
                            gouMaiLauncher.launch(intent)
                        }
                        if(appViewModel.modUserRealName.value?.isRealName18() == false){
                            Toaster.show("未成年用户不允许购买商品")
                        }
                    }else{
                        Toaster.show(it.msg)
                    }

                }
            )
        }


    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    override fun onRightClick(view: TitleBar) {
        super.onRightClick(view)
        ModActivityJiaoYiTip.start(appContext)
    }

    fun doPay(){

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun pay() {
            if(appViewModel.modUserRealName.value?.isRealName() == false){
                Toaster.show("未实名认证，请先进行实名认证")
                ActivityUtils.startActivity(ModActivityJiaoYiShiMing::class.java)
                return
            }
            if(appViewModel.modUserRealName.value?.isRealName18() == false){
                Toaster.show("未成年用户不允许购买商品")
                return
            }
            mViewModel.tradeGoodDetail.value?.goods_price?.let { it1 ->
                mViewModel.postPayGetOrder(it1)
            }
        }

        fun ali() {
            mViewModel.payType.set(1)
        }

        fun wx() {
            mViewModel.payType.set(2)
        }

    }


}