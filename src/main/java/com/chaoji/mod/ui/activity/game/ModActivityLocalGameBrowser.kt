package com.chaoji.mod.ui.activity.game

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import androidx.lifecycle.lifecycleScope
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.countClick
import com.chaoji.im.data.model.MarketInit
import com.chaoji.im.localExit
import com.chaoji.im.runOnBuildConfig
import com.chaoji.im.sdk.ImSDK
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.mod.BuildConfig
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityLocalGameBrowserBinding
import com.chaoji.mod.game.ModGameBridgeHost
import com.chaoji.mod.game.ModGameUnifiedJsBridge
import com.chaoji.mod.game.ModManager
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import kotlinx.coroutines.launch

class ModActivityLocalGameBrowser : BaseVmDbActivity<ModActivityLocalGameBrowserModel, ModActivityLocalGameBrowserBinding>(), ModGameBridgeHost {
    private lateinit var jsBridge: ModGameUnifiedJsBridge
    var mHandler: Handler = Handler(Looper.getMainLooper())
    override fun layoutId(): Int = R.layout.mod_activity_local_game_browser

    companion object {
        const val INTENT_KEY_IN_URL: String = "url"
        var GAME_URL = ModManager.GAME_URL
        fun start(context: Context) {
            val intent = Intent(context, ModActivityLocalGameBrowser::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            ActivityUtils.startActivity(intent)
        }

        fun start(context: Context, url: String) {
            val intent = Intent(context, CommonActivityBrowser::class.java)
            intent.putExtra(INTENT_KEY_IN_URL, url)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            ActivityUtils.startActivity(intent)
        }

        fun exit(activity: Activity) {
            localExit(activity)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView(savedInstanceState: Bundle?) {
        immersionBar {
            transparentBar()
            init()
        }
        jsBridge = ModGameUnifiedJsBridge(this)
        runOnBuildConfig(
            onDebug = {
                startGame()
            },
            onRelease = { agreeInit() }
        )


    }

    private fun agreeInit() {
        MMKVUtil.saveShouQuan("SQ")
        val goTest = ImSDK.eventViewModelInstance.goTest.value
        val marketInit by lazy { GsonUtils.fromJson(MMKVUtil.getMarketInit(), MarketInit::class.java) }
        if (goTest == 2 || marketInit?.status == 1) {
            startGame()
        } else {
            lifecycleScope.launch {

            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    fun startGame() {
        mViewModel.showGame.set(true)
        mDataBinding.wvBrowserView.setLifecycleOwner(this)
        mDataBinding.wvBrowserView.apply {
            //setBrowserViewClient(AppBrowserViewClient())
            //setBrowserChromeClient(AppBrowserChromeClient(this))
            isDrawingCacheEnabled = true
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            //设置自适应屏幕，两者合用
            //将图片调整到适合webview的大小
            settings.useWideViewPort = true
            // 缩放至屏幕的大小
            settings.loadWithOverviewMode = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.javaScriptEnabled = true // 启用JS
            // init webview settings
            settings.allowContentAccess = true // 允许访问ContentProvider资源
            settings.databaseEnabled = true
            settings.domStorageEnabled = true // 启用DOM存储
            settings.allowFileAccess = true // 允许访问本地文件
            settings.savePassword = false
            settings.saveFormData = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            addJavascriptInterface(jsBridge, "AndroidBridge")
            loadUrl(GAME_URL)
        }

        mDataBinding.wvBrowserView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Logs.e(
                    "WebViewConsole",
                    "${consoleMessage?.message()} -- (line ${consoleMessage?.lineNumber()})"
                )
                if (consoleMessage?.message()?.contains("load oper") == true) {
                    val marketInit by lazy { GsonUtils.fromJson(MMKVUtil.getMarketInit(), MarketInit::class.java) }
                    if (marketInit?.status == 1) {
                        mHandler.post { jsBridge.enterLogin("ok") }
                    }
                }
                return true
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            exit(this)
            return true
        } else {
            return super.dispatchKeyEvent(event)
        }
    }


    override fun createObserver() {


    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun text() {
            countClick {

            }
        }
    }


    //================================================================
    // Region: 实现 ModGameBridgeHost 接口 (为Bridge提供能力)
    //================================================================

    // --- 这个Activity支持的功能 ---
    override fun getWebView() = mDataBinding.wvBrowserView
    override fun getHandler() = mHandler
    override fun getAppContext(): Context = appContext

    override fun onOpenWebView(url: String) {
        // 这个Activity有自己的start方法，但为了通用性，我们调用CommonActivityBrowser
        CommonActivityBrowser.start(this, url)
    }

    override fun getAndroidId(): String = "" // 按需提供真实实现
    override fun getTgid(): String = "" // 按需提供真实实现
    override fun getPackageName(): String = "" // 按需提供真实实现

    override fun onStartXDMain() {
    }

    override fun onCheckUserLogin(): Boolean {
        return true
    }

    override fun onHandleJumpAction(action: String) {
    }

    override fun onShowToast(message: String?) {
       Toaster.show(message)
    }

    override fun onInitFloatView() {
    }

    override fun onFetchGameList(type: String) {
    }

    override fun onPostCocosExchange(msg: String, uid: String, token: String) {
    }

    override fun onStartBindPhoneActivity(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    }

    override fun onStartLoginActivity(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    }

    override fun getGoTestValue(): Int? = null
    override fun getMarketInitJson(): String? = null
    override fun getUserUid(): String = ""
    override fun getUserToken(): String = ""
    override fun getPrivacyPolicyUrl(): String? = appViewModel.appInfo.value?.marketjson?.xieyitanchuang_url_yinsi
    override fun getOaid(): String = ""

    override fun setOaid(oaid: String) {
    }

    override var exchangeType: String = ""


}