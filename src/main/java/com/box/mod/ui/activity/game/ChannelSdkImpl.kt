package com.box.mod.ui.activity.game

import android.app.Activity
import android.content.Context
import android.view.View
import com.box.mod.game.sdk.IGameChannelSDK

/**
 * 渠道SDK的统一实现类。
 * 这个文件的内容会根据Git分支的不同而变化。
 */
class ChannelSdkImpl : IGameChannelSDK {
    // --- 渠道的实现 ---
    override fun init(context: Context, appId: String) {
    }

    override fun onExit(activity: Activity, onExitConfirm: () -> Unit) {
    }

    override fun initAd(activity: Activity) {
    }

    override fun doAdSplash(
        activity: Activity,
        onAdShow: () -> Unit,
        onAdReady: (view: View) -> Unit,
        onAdClick: () -> Unit,
        onAdSkip: () -> Unit,
        onAdTimeOver: () -> Unit,
        onAdFailed: (message: String) -> Unit
    ) {
    }

    override fun doAdBanner(
        activity: Activity,
        onAdShow: () -> Unit,
        onAdReady: (view: View) -> Unit,
        onAdClick: () -> Unit,
        onAdSkip: () -> Unit,
        onAdTimeOver: () -> Unit,
        onAdFailed: (message: String) -> Unit,
        onAdClose: () -> Unit
    ) {
    }

    /**
     * doLogin
     */
    override fun doLogin(
        activity: Activity,
        onLoginSuccess: () -> Unit,
        onLoginFailure: (message: String, code: Int) -> Unit
    ) {

    }


}