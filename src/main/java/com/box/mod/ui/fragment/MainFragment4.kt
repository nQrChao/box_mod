package com.box.mod.ui.fragment

import android.content.Intent
import android.os.Bundle
import com.box.base.base.action.HandlerAction
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.network.NetState
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
import com.box.common.ui.widget.XCollapsingToolbarLayout
import com.box.common.utils.MMKVUtil
import com.box.mod.R
import com.box.mod.databinding.MainFragment4Binding
import com.box.mod.ui.activity.jiaoyi.ModActivityJiaoYiList
import com.box.mod.ui.activity.ModActivityMessage
import com.box.mod.ui.activity.login.ModActivitySafety
import com.box.mod.ui.activity.jiaoyi.ModActivityShouCang
import com.box.mod.ui.activity.login.ModActivityXDLogin
import com.box.com.R as RC
import com.box.other.blankj.utilcode.util.ClipboardUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.kongzue.baseokhttp.HttpRequest
import com.box.other.kongzue.baseokhttp.listener.ResponseListener
import com.box.other.kongzue.baseokhttp.util.Parameter
import com.box.other.xpopup.XPopup
import com.google.android.material.appbar.AppBarLayout
import java.util.UUID

class MainFragment4 : BaseTitleBarFragment<MainFragment4Model, MainFragment4Binding>(), XCollapsingToolbarLayout.OnScrimsListener, HandlerAction {
    override fun layoutId(): Int = R.layout.main_fragment_4

    companion object {
        fun newInstance(): MainFragment4 {
            return MainFragment4()
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.isLogin.set(eventViewModel.isLogin.value ?: false)
        Logs.e("eventViewModel.4")
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

        if (isLogin()) {
            mViewModel.modUser.postValue(appViewModel.modUserInfo.value)
        }

        mDataBinding.ctlBar.setOnScrimsListener(this)

        mDataBinding.appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                Logs.e("verticalOffset:", verticalOffset)
            }
        })

    }

    override fun createObserver() {
        eventViewModel.isLogin.observe(this){
            Logs.e("eventViewModel.isLogin2")
            //mViewModel.isLogin.set(it)
        }

        appViewModel.modUserInfo.observe(this) {
            MMKVUtil.saveModUser(it)
            mViewModel.modUser.postValue(it)
        }

        eventViewModel.getAiChatCount.observe(this) {

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

    override fun onScrimsStateChange(layout: XCollapsingToolbarLayout?, shown: Boolean) {
        //getStatusBarConfig().statusBarDarkFont(shown).init()
        //immersionBar { statusBarDarkFont(shown).init() }
        mViewModel.shown.set(shown)
        //mDataBinding.tvTitle.visibility
        //mDataBinding.tvBack.setImageResource(if (shown) com.chaoji.common.R.drawable.arrows_left_b_ic else com.chaoji.common.R.drawable.arrows_left_b_w_ic)
        //mDataBinding.tvTitle.setTextColor(ContextCompat.getColor(this, if (shown) com.chaoji.common.R.color.black80 else com.chaoji.common.R.color.white))
    }

    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun top1() {
            if (isLogin()) {
                ModActivityJiaoYiList.start(appContext)
            } else {
                Toaster.show("您还未登录，请先登录")
                ModActivityXDLogin.start(appContext)
            }
        }

        fun top2() {
            if (isLogin()) {
                ModActivityShouCang.start(appContext)
            } else {
                Toaster.show("您还未登录，请先登录")
                ModActivityXDLogin.start(appContext)
            }
        }

        fun top3() {
            if (isLogin()) {
                ModActivityMessage.start(appContext)
            } else {
                Toaster.show("您还未登录，请先登录")
                ModActivityXDLogin.start(appContext)
            }
        }

        fun copyGZH() {
            Toaster.show("已复制到剪切版")
            ClipboardUtils.copyText(mViewModel.modUser.value?.uid.toString())
        }

        fun login() {
            if (!isLogin()) {
                ModActivityXDLogin.start(appContext)
            }
        }

        fun tuiSong() {
            Toaster.show("已开启个性推送")
        }

        fun fanKui() {
            //ModActivityTest.start(appContext)
            ModActivityFanKui.start(appContext)
        }

        fun qqKeFu() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    XPopup.Builder(context)
                        .isDestroyOnDismiss(true)
                        .hasStatusBar(true)
                        .animationDuration(5)
                        .navigationBarColor(ColorUtils.getColor(com.box.com.R.color.xpop_shadow_color))
                        .isLightStatusBar(true)
                        .hasNavigationBar(true)
                        .asConfirm(
                            "提示", "客服QQ号："+it.marketjson.qq+"\n请自行添加客服咨询。\n点击确定复制客服QQ号码" ,
                            "取消", "确定",
                            {
                                ClipboardUtils.copyText(it.marketjson.qq)
                                Toaster.show("已复制客服QQ号，请自行添加客服咨询")
                            }, null, true, R.layout.xpopup_confirm_mod
                        ).show()
                }
            }
        }

        fun jiaoyi() {
            if (isLogin()) {
                ModActivityJiaoYiList.start(appContext)
            } else {
                Toaster.show("您还未登录，请先登录")
                ModActivityXDLogin.start(appContext)
            }
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

        fun uAq() {
            ModActivitySafety.start(appContext)
        }
        fun quit() {
            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .animationDuration(5)
                .navigationBarColor(ColorUtils.getColor(com.box.com.R.color.xpop_shadow_color))
                .isLightStatusBar(true)
                .hasNavigationBar(true)
                .asConfirm(
                    "退出", "是否退出该账号",
                    "取消", "确定",
                    {
                        Toaster.show("账号已退出")
                        MMKVUtil.saveModUser(null)
                        mViewModel.modUser.postValue(null)
                        appViewModel.modUserInfo.postValue(null)
                        eventViewModel.isLogin.postValue(false)
                    }, null, false, com.box.com.R.layout.xpopup_confirm
                ).show()
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

                    }, null, false, R.layout.xpopup_confirm_mod
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
                        Toaster.show("账号已退出")
                        mViewModel.loginOut()
                    }, null, false, R.layout.xpopup_confirm_mod
                ).show()
        }

        fun testInfo() {
            countClick {
                ImSDK.eventViewModelInstance.showInfoView.postValue(true)
            }
        }

    }

}