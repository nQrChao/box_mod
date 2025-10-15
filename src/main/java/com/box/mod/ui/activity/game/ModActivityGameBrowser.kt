package com.box.mod.ui.activity.game

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.ext.parseModState
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.countClick
import com.box.common.data.model.MarketInit
import com.box.common.data.model.ModGameAppletsInfo
import com.box.common.localExit
import com.box.common.sdk.ApkUtils
import com.box.common.sdk.ImSDK
import com.box.common.sdk.appViewModel
import com.box.common.sdk.eventViewModel
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.common.ui.xpop.mod.ModXPopupBottomGameList
import com.box.common.utils.MMKVUtil
import com.box.common.utils.floattoast.XToast
import com.box.mod.BuildConfig
import com.box.mod.R
import com.box.mod.callback.CallbackManager
import com.box.mod.databinding.ModActivityGameBrowserBinding
import com.box.mod.game.ModGameBridgeHost
import com.box.mod.game.ModGameUnifiedJsBridge
import com.box.mod.game.ModManager
import com.box.mod.game.ModManager.BIND_PHONE_OK
import com.box.mod.game.ModManager.LOGIN_OK
import com.box.mod.game.sdk.ModSdkManager
import com.box.mod.view.FloatViewHelper
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.DeviceUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.blankj.utilcode.util.ResourceUtils
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.box.other.xpopup.core.BasePopupView
import com.box.other.xpopup.enums.PopupAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess
import com.box.com.R as RC

class ModActivityGameBrowser :
    BaseVmDbActivity<ModActivityGameBrowserModel, ModActivityGameBrowserBinding>(), ModGameBridgeHost {
    var gameSdk = ModSdkManager.getChannelSdk()
    private lateinit var jsBridge: ModGameUnifiedJsBridge

    private var floatToast: XToast<*>? = null
    private var gameListPopup: BasePopupView? = null
    private val jumpAction = CallbackManager.jumpCallback
    private lateinit var loginLauncher: ActivityResultLauncher<Intent>

    var mHandler: Handler = Handler(Looper.getMainLooper())

    override fun layoutId(): Int = R.layout.mod_activity_game_browser

    companion object {
        const val INTENT_KEY_IN_URL: String = "url"
        var GAME_URL = ModManager.GAME_URL
        fun start(context: Context) {
            val intent = Intent(context, ModActivityGameBrowser::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            ActivityUtils.startActivity(intent)
        }

        fun start(context: Context, url: String) {
            val intent = Intent(context, CommonActivityBrowser::class.java)
            intent.putExtra(INTENT_KEY_IN_URL, url)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            ActivityUtils.startActivity(intent)
        }

        fun start(context: Context, jump: ((String) -> Unit)?) {
            CallbackManager.jumpCallback = jump
            val intent = Intent(context, ModActivityGameBrowser::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            ActivityUtils.startActivity(intent)
        }

        fun start(context: Context, url: String, jump: ((String) -> Unit)?) {
            if (url != GAME_URL) {
                ActivityUtils.finishActivity(ModActivityGameBrowser::class.java)
            }
            GAME_URL = if (StringUtils.isEmpty(url)) {
                ModManager.GAME_URL
            } else {
                url
            }
            CallbackManager.jumpCallback = jump
            val intent = Intent(context, ModActivityGameBrowser::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            ActivityUtils.startActivity(intent)
        }

        fun exit(activity: Activity) {
            val goTest = ImSDK.eventViewModelInstance.goTest.value
            val marketInit by lazy { GsonUtils.fromJson(MMKVUtil.getMarketInit(), MarketInit::class.java) }
            if (goTest == 2 || marketInit?.status == 1) {
                localExit(activity)
            } else {
                ModSdkManager.getChannelSdk().onExit(activity) {
                    ActivityUtils.finishAllActivities()
                    exitProcess(0)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView(savedInstanceState: Bundle?) {
        immersionBar {
            transparentBar()
            init()
        }

        gameSdk = ModSdkManager.getChannelSdk()
        jsBridge = ModGameUnifiedJsBridge(this)

        agreeInit()

//        runOnBuildConfig(
//            onDebug = {
//                startGame()
//                initFloatView()
//            },
//            onRelease = {
//                agreeInit()
//            }
//        )

        // 在这里注册专门处理登录结果的回调
        loginLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                // "result" 就是从登录页返回的结果
                Logs.e("result.resultCode:", result.resultCode)
                when (result.resultCode) {
                    LOGIN_OK -> {
                        mHandler.postDelayed({ jsBridge.enterLogin("ok") }, 500)
                    }
                    BIND_PHONE_OK -> {
                        if (!exchangeType.isNullOrEmpty()) {
                            val uid = ModManager.provider.getUserUid()
                            val token = ModManager.provider.getUserToken()
                            mViewModel.postCocosExchangeWithMsg(exchangeType, uid, token)
                        }
                    }
                    else -> {
                        //Toaster.show("您需要登录才能继续")
                    }
                }
            }
    }

    private fun agreeInit() {
        MMKVUtil.saveShouQuan("SQ")
        val goTest = ImSDK.eventViewModelInstance.goTest.value
        val marketInit by lazy { GsonUtils.fromJson(MMKVUtil.getMarketInit(), MarketInit::class.java) }
        if (goTest == 2 || marketInit?.status == 1) {
            startGame()
            initFloatView()
        } else {
            lifecycleScope.launch {
                gameSdk.init(this@ModActivityGameBrowser, BuildConfig.GAME_O_ID)
                delay(500)
                gameSdk.doLogin(
                    activity = this@ModActivityGameBrowser,
                    onLoginSuccess = {
                        // 在渠道SDK内部所有步骤都完成后才被调用
                        Logs.e("Activity", "Channel login flow complete. Starting game...")
                        startGame()
                    },
                    onLoginFailure = { message, code ->
                        Logs.e("Activity", "Channel login flow failed: $message, code: $code")
                    }
                )
            }
        }
    }

    override fun onResume() {
        //mViewModel.hideLoading.set(true)
        super.onResume()
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
                        mHandler.postDelayed({
                            jsBridge.enterLogin("ok")

                            jsBridge.takeTGID(ApkUtils.getTgid())
                            jsBridge.takeAndroidId(DeviceUtils.getAndroidID())
                            jsBridge.takePackageName(AppUtils.getAppPackageName())
                            jsBridge.takeDeviceId(appViewModel.oaid)
                        }, 100)
                    }
                }
                return true
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        CallbackManager.jumpCallback = null
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


    /**
     * 悬浮球，初始化
     */
    fun initFloatView() {
        if (floatToast == null) {
            floatToast = FloatViewHelper.createModFloatView(
                this@ModActivityGameBrowser,
                ResourceUtils.getDrawable(R.mipmap.mod_icon)
            ) {
                startXDMain()
                null
            }
        }
    }

    override fun createObserver() {
        mViewModel.postInfoAppApi325Result.observe(this) { modResultState ->
            parseModState(modResultState, { gameListInfo ->
                gameListInfo?.let { listInfo ->
                    if (listInfo.marketjson.list_data.isEmpty()) {
                        Toaster.show("数据为空")
                    } else {
                        jsBridge.sendGameList1(GsonUtils.toJson(listInfo.marketjson))
                    }
                }
            }, {
                Toaster.show(it.msg)
            })
        }

        mViewModel.postInfoAppApi326Result.observe(this) { modResultState ->
            parseModState(modResultState, { gameListInfo ->
                gameListInfo?.let { listInfo ->
                    if (listInfo.marketjson.list_data.isEmpty()) {
                        Toaster.show("数据为空")
                    } else {
                        jsBridge.sendGameList2(GsonUtils.toJson(listInfo.marketjson))
                    }
                }
            }, {
                Toaster.show(it.msg)
            })
        }

        mViewModel.postInfoAppApi327Result.observe(this) { modResultState ->
            parseModState(modResultState, { gameListInfo ->
                gameListInfo?.let { listInfo ->
                    if (listInfo.marketjson.list_data.isEmpty()) {
                        Toaster.show("数据为空")
                    } else {
                        showGameListPopup(
                            listInfo.marketjson.list_data,
                            listInfo.marketjson.list_data2
                        )
                    }
                }
            }, {
                Toaster.show(it.msg)
            })
        }
        mViewModel.postCocosExchangeResultWithMsg.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    Toaster.show(msg)
                    jsBridge.exchangeResult(exchangeType)
                    // ✅ data 为 null 的判断
                    Logs.e("操作成功: $msg")
                    exchangeType = ""
                },
                onError = {
                    if (it.state == "jump" && it.msg.contains("绑定手机")) {
                        ModManager.provider.startBindPhone(
                            this@ModActivityGameBrowser,
                            loginLauncher
                        )
                    }
                    Toaster.show(it.msg)
                }
            )
        }

    }

    private fun showGameListPopup(
        gameAppList: MutableList<ModGameAppletsInfo>,
        gameTip: MutableList<String>
    ) {
        gameListPopup = XPopup.Builder(this)
            .isViewMode(true)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .isDestroyOnDismiss(true)
            .hasStatusBar(false)
            .enableDrag(false)
            .popupAnimation(PopupAnimation.TranslateFromBottom)
            .animationDuration(500)
            .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
            .hasNavigationBar(false)
            .asCustom(ModXPopupBottomGameList(this, gameAppList, gameTip, {
                //詳情
                jumpAction?.invoke("详情_${it.gameId}")
            }, {
                jumpAction?.invoke("启动_${it.gameId}")
            }, {
                //跳转领取
                if (checkUserLogin()) {
                    jumpAction?.invoke("领取_${it}")
                }
            }, {
                //dismiss
                gameListPopup = null
            }))
            .show()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }


    private fun startXDMain() {
        ModManager.provider.startMainActivity(this)
    }

    /**
     * 检查是否登录
     */
    private fun checkUserLogin(): Boolean {
        if (!ModManager.provider.isUserLoggedIn()) {
            ModManager.provider.startLoginActivity(this, loginLauncher)
            return false
        } else {
            return true
        }
    }

    fun getAppKey(context: Context): String? {
        var appKey: String? = null
        try {
            // 1. 获取 ApplicationInfo 对象
            val applicationInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            // 2. 从 metaData Bundle 中获取值
            // 使用安全调用 ?. 来避免 applicationInfo.metaData 为 null 时崩溃
            appKey = applicationInfo.metaData?.getString("app_key")

            Log.d("MyApp", "Successfully retrieved app_key: $appKey")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("MyApp", "Failed to load meta-data, NameNotFound: " + e.message)
        } catch (e: NullPointerException) {
            Log.e("MyApp", "Failed to load meta-data, NullPointer: " + e.message)
        }
        return appKey
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

    override fun getWebView() = mDataBinding.wvBrowserView
    override fun getHandler() = mHandler
    override fun getAppContext() = appContext

    override fun onStartXDMain() {
        startXDMain()
    }

    override fun onCheckUserLogin(): Boolean {
        return checkUserLogin()
    }

    override fun onHandleJumpAction(action: String) {
        ModManager.provider.handleJumpAction(this, action)
    }

    override fun onShowToast(message: String?) {
        Toaster.show(message)
    }

    override fun onOpenWebView(url: String) {
        CommonActivityBrowser.start(this, url)
    }

    override fun onInitFloatView() {
        initFloatView()
    }

    override fun onFetchGameList(type: String) {
        when (type) {
            "1" -> mViewModel.postInfoAppApi325()
            "2" -> mViewModel.postInfoAppApi326()
            "3" -> mViewModel.postInfoAppApi327()
        }
    }

    override fun onPostCocosExchange(msg: String, uid: String, token: String) {
        mViewModel.postCocosExchangeWithMsg(msg, uid, token)
    }

    override fun onStartBindPhoneActivity(launcher: ActivityResultLauncher<Intent>) {
        ModManager.provider.startBindPhone(this, launcher)
    }

    override fun onStartLoginActivity(launcher: ActivityResultLauncher<Intent>) {
        ModManager.provider.startLoginActivity(this, loginLauncher)
    }

    override fun getGoTestValue(): Int? = eventViewModel.goTest.value
    override fun getMarketInitJson(): String = ModManager.provider.getMarketInitJson()
    override fun getUserUid(): String = ModManager.provider.getUserUid()
    override fun getUserToken(): String = ModManager.provider.getUserToken()
    override fun getPrivacyPolicyUrl(): String? = appViewModel.appInfo.value?.marketjson?.xieyitanchuang_url_yinsi
    override fun getOaid(): String = appViewModel.oaid
    override fun getAndroidId(): String = DeviceUtils.getAndroidID()
    override fun getTgid(): String = ApkUtils.getTgid()
    override fun getPackageName(): String = AppUtils.getAppPackageName()

    override fun setOaid(oaid: String) {
        appViewModel.oaid = oaid
    }

    // 实现接口要求的属性
    override var exchangeType: String = ""






}