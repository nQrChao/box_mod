package com.box.mod.ui.activity.modmain.mod2

import android.content.Intent
import android.os.Bundle
import com.box.base.base.action.HandlerAction
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.network.NetState
import com.box.common.CAMERAPermission
import com.box.common.appContext
import com.box.common.countClick
import com.box.other.hjq.titlebar.TitleBar
import com.box.common.network.ApiService
import com.box.common.sdk.ImSDK
import com.box.common.sdk.appViewModel
import com.box.common.sdk.eventViewModel
import com.box.common.toBrowser
import com.box.mod.ui.activity.about.ModActivityAbout
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.mod.ui.activity.ModActivityFanKui
import com.box.common.ui.view.SwitchButton
import com.box.mod.R
import com.box.common.R as RC
import com.box.mod.databinding.Mod2Fragment4Binding
import com.box.other.blankj.utilcode.util.ClipboardUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.kongzue.baseokhttp.HttpRequest
import com.box.other.kongzue.baseokhttp.listener.ResponseListener
import com.box.other.kongzue.baseokhttp.util.Parameter
import com.box.other.xpopup.XPopup
import com.hjq.permissions.XXPermissions
import java.util.UUID

class Mod2Fragment4 : BaseTitleBarFragment<Mod2Fragment4Model, Mod2Fragment4Binding>(), HandlerAction {
    override fun layoutId(): Int = R.layout.mod2_fragment_4

    companion object {
        fun newInstance(): Mod2Fragment4 {
            return Mod2Fragment4()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }

        appViewModel.appInfo.value.let {
            if (it != null) {
                mDataBinding.userSetting4.setRightText(it.marketjson.app_beianhao)
            }
        }
        mDataBinding.userSetting1Switch.setOnCheckedChangeListener(object :
            SwitchButton.OnCheckedChangeListener {
            override fun onCheckedChanged(button: SwitchButton, checked: Boolean) {
                val message = if (checked) "已开启个性推送" else "已关闭个性推送"
                Toaster.show(message)
            }
        })

    }

    override fun createObserver() {
        eventViewModel.getAiChatCount.observe(this) {
            mViewModel.getCount()
        }

        mViewModel.tuiSong.observe(this) {
            Toaster.show(it)
        }

    }

    override fun lazyLoadData() {
        appViewModel.userInfo.value?.run {
            //mDataBinding.meSwipe.isRefreshing = true
        }
    }

    override fun onNetworkStateChanged(it: NetState) {
    }

    override fun onLeftClick(view: TitleBar) {
        XXPermissions.with(this).permission(CAMERAPermission).request { _, all ->
            if (all) {
            } else {
                Toaster.show("请开权限")
            }
        }
    }

    override fun onTitleClick(view: TitleBar) {
        super.onTitleClick(view)
    }

    override fun onRightClick(view: TitleBar) {

    }

    fun getCount() {
        HttpRequest.GET(context, ApiService.D_API_URL,
            Parameter()
                .add("Authorization", "Bearer " + ImSDK.appViewModelInstance.userInfo.value?.token)
                .add("operationID", System.currentTimeMillis().toString() + "")
                .add("deviceID", UUID.randomUUID().toString()),
            Parameter(),
            object : ResponseListener() {
                override fun onResponse(main: String, error: Exception?) {
                    if (error == null) {
                        Logs.e("PAR:$main")
                    } else {
                        Toaster.show("请求失败")
                    }
                }
            })
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {

        fun userIcon() {
            countClick {
                ImSDK.eventViewModelInstance.showInfoView.postValue(true)
//                XPopup.Builder(mActivity)
//                    .dismissOnTouchOutside(false)
//                    .isDestroyOnDismiss(true)
//                    .hasStatusBar(true)
//                    .isLightStatusBar(true)
//                    .animationDuration(10)
//                    .navigationBarColor(ColorUtils.getColor(R.color.xpop_shadow_color))
//                    .hasNavigationBar(true)
//                    .asCustom(XXXPopupCenter(mActivity))
//                    .show()
            }
        }

        fun tuiSong() {
            Toaster.show("已开启个性推送")
        }

        fun fanKui() {
            ModActivityFanKui.start(appContext)
        }

        fun keFu() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    toBrowser(it.marketjson.wechat_url)
                }
            }
        }

        fun beiAn() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.marketjson.app_beianhao_url)
                }
            }
        }

        fun uXy() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.marketjson.xieyitanchuang_url_fuwu)
                }
            }
        }

        fun yXy() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.marketjson.xieyitanchuang_url_yinsi)
                }
            }
        }

        fun fangChenMi() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    CommonActivityBrowser.start(appContext, it.marketjson.xieyitanchuang_url_fcm)
                }
            }
        }


        fun copy() {
            Toaster.show("已复制到剪切版")
            ClipboardUtils.copyText(mViewModel.id.get())
        }

        fun goAbout() {
            startActivity(Intent(mActivity, ModActivityAbout::class.java))
        }

        fun clearAi() {
            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .animationDuration(10)
                .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                .isLightStatusBar(true)
                .hasNavigationBar(true)
                .asConfirm(
                    "提示", "是否清除所有AI对话记录？",
                    "取消", "确定",
                    {
                        mViewModel.delChatMessage()
                    }, null, false, RC.layout.xpopup_confirm
                ).show()
        }

        fun loginOut() {
            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .animationDuration(10)
                .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                .isLightStatusBar(true)
                .hasNavigationBar(true)
                .asConfirm(
                    "退出", "确认退出登录？",
                    "取消", "确定",
                    {
                        mViewModel.loginOut()
                    }, null, false, RC.layout.xpopup_confirm
                ).show()
        }

    }

}