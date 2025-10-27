package com.box.mod.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import com.box.base.base.activity.BaseModVmDbActivity
import com.box.base.ext.parseState
import com.box.base.network.NetState
import com.box.common.AppInit
import com.box.common.MMKVConfig
import com.box.common.appViewModel
import com.box.common.eventViewModel
import com.box.mod.ui.xpop.ModXPopupCenterProtocol
import com.box.mod.ui.xpop.ModXPopupCenterTip
import com.box.mod.R
import com.box.com.R as RC
import com.box.mod.databinding.ModActivitySplashBinding
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.xpopup.XPopup

@SuppressLint("CustomSplashScreen")
class ModSplashActivity : BaseModVmDbActivity<ModSplashModel, ModActivitySplashBinding>() {
    override val mViewModel: ModSplashModel by viewModels()
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
                if (MMKVConfig.permissionsUser) {
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
        MMKVConfig.permissionsUser = true
        AppInit.initCNOAID()

        startMain()
    }

    private fun startMain() {
        ModActivityMain.start(this@ModSplashActivity)
        finish()
    }

    override fun onNetworkStateChanged(it: NetState) {

    }


}