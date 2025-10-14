package com.box.mod.ui.activity

import android.os.Bundle
import android.view.WindowManager
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.ext.parseState
import com.box.base.network.NetState
import com.box.common.getOAIDWithRetry
import com.box.common.sdk.ImSDK
import com.box.common.sdk.appViewModel
import com.box.common.sdk.eventViewModel
import com.box.mod.ui.xpop.ModXPopupCenterProtocol
import com.box.mod.ui.xpop.ModXPopupCenterTip
import com.box.common.utils.MMKVUtil
import com.box.mod.R
import com.box.common.R as RC
import com.box.mod.databinding.ModActivitySplashBinding
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.xpopup.XPopup

class ModSplashActivity : BaseVmDbActivity<ModSplashModel, ModActivitySplashBinding>() {
    override fun layoutId(): Int {
        return R.layout.mod_activity_splash
    }

    override fun initView(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        //throw IllegalArgumentException("are you ok?")
        mViewModel.xyInit()
    }

    override fun createObserver() {
        mViewModel.marketInitResult.observe(this) { it ->
            parseState(it, {
                appViewModel.appInfo.postValue(it)
                if (!StringUtils.isEmpty(MMKVUtil.getShouQuan())) {
                    //已同意
                    agreeInit()
                } else {
                    XPopup.Builder(this@ModSplashActivity)
                        .dismissOnTouchOutside(false)
                        .dismissOnBackPressed(false)
                        .isDestroyOnDismiss(true)
                        .hasStatusBar(true)
                        .isLightStatusBar(true)
                        .animationDuration(5)
                        .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                        .hasNavigationBar(true)
                        .asCustom(
                            it?.let { it1 ->
                                ModXPopupCenterProtocol(this@ModSplashActivity, it1, {
                                    //取消，弹预览模式
                                    XPopup.Builder(this@ModSplashActivity)
                                        .dismissOnTouchOutside(false)
                                        .dismissOnBackPressed(false)
                                        .isDestroyOnDismiss(true)
                                        .hasStatusBar(true)
                                        .isLightStatusBar(true)
                                        .animationDuration(5)
                                        .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                                        .hasNavigationBar(true)
                                        .asCustom(
                                            ModXPopupCenterTip(this@ModSplashActivity, it, {
                                                //浏览
                                                ModActivityPreview.start(this@ModSplashActivity)
                                            }) {
                                                //返回
                                            })
                                        .show()
                                }) {
                                    //同意
                                    agreeInit()
                                }
                            })
                        .show()

                }
            }, {
                Toaster.show(it.errorMsg)
            })
        }

        eventViewModel.splashResult.observe(this) {
            if (it) {
                //startActivity(Intent(applicationContext, ActivityModMain::class.java))
                //finish()
            } else {
                //startActivity(Intent(applicationContext, ActivityModMain::class.java))
                //finish()
            }
        }
    }

    private fun agreeInit() {
        MMKVUtil.saveShouQuan("SQ")
        ImSDK.instance.initDeviceInfo()
        ImSDK.instance.initUmeng(application)
        ImSDK.instance.initCNOAID()
        getOAIDWithRetry(this) {
            Logs.e("getOAIDWithRetry:$it")
        }
        startMain()
    }

    private fun startMain() {
        ModActivityMain.start(this@ModSplashActivity)
        finish()
    }

    override fun onNetworkStateChanged(it: NetState) {

    }


}