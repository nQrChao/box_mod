package com.chaoji.mod.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseState
import com.chaoji.base.network.NetState
import com.chaoji.im.getOAIDWithRetry
import com.chaoji.im.network.initializeNetwork
import com.chaoji.im.sdk.ImSDK
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.sdk.eventViewModel
import com.chaoji.mod.ui.xpop.ModXPopupCenterProtocol
import com.chaoji.mod.ui.xpop.ModXPopupCenterTip
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.mod.BuildConfig
import com.chaoji.mod.R
import com.chaoji.common.R as RC
import com.chaoji.mod.databinding.ModActivitySplashBinding
import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.xpopup.XPopup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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