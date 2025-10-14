package com.chaoji.mod.ui.activity.jiaoyi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityJiaoyiPayWebviewBinding
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.AppUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.common.R as RC

class ModActivityPayWeb : BaseVmDbActivity<ModActivityPayWebModel, ModActivityJiaoyiPayWebviewBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_pay_webview

    companion object {
        const val INTENT_KEY_IN_URL: String = "url"
        fun start(context: Context, url: String) {
            if (TextUtils.isEmpty(url)) {
                return
            }
            val intent = Intent(context, ModActivityPayWeb::class.java)
            intent.putExtra(INTENT_KEY_IN_URL, url)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            navigationBarColor(RC.color.white)
            init()
        }

        mDataBinding.webview.settings.domStorageEnabled = true
        mDataBinding.webview.settings.databaseEnabled = true
        mDataBinding.webview.settings.useWideViewPort = true
        mDataBinding.webview.settings.javaScriptEnabled = true
        mDataBinding.webview.addJavascriptInterface(AndroidBridge(),"webApi")
        mDataBinding.webview.addJavascriptInterface(AndroidBridge(),"sdkcall")
        val userAgentString: String = mDataBinding.webview.settings.userAgentString
        mDataBinding.webview.settings.userAgentString = userAgentString
        mDataBinding.webview.settings.loadsImagesAutomatically = true
        WebView.setWebContentsDebuggingEnabled(true)
        mDataBinding.webview.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                var titleText = title
                super.onReceivedTitle(view, title)
                if (!TextUtils.isEmpty(title)) {
                    if (title.length > 10) {
                        titleText = title.substring(0, 10) + "..."
                    }
                }
                //getTitleBar()?.title = titleText
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                //Logs.e("process："+ newProgress);
                try {
                    mDataBinding.progress.progress = newProgress
                    if (newProgress == 100) {
                        mDataBinding.progress.visibility = View.GONE
                    } else {
                        mDataBinding.progress.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // For Android >= 5.0
            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
                return true
            }
        }
        mDataBinding.webview.webViewClient = object : WebViewClient() {
            override fun onReceivedError(webView: WebView, i: Int, s: String, s1: String) {
                super.onReceivedError(webView, i, s, s1)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                Logs.e("onPageFinished：$url")
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.contains("alipays://platformapi")) {
                    if (!AppUtils.isAppInstalled("com.eg.android.AlipayGphone")) {
                        Toaster.show("请先下载安装支付宝")
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                    return true
                } else if (url.startsWith("weixin://wap/pay?")) {
                    if (!AppUtils.isAppInstalled("com.tencent.mm")) {
                        Toaster.show("请先下载安装微信")
                    } else {
                        val intent = Intent()
                        intent.setAction(Intent.ACTION_VIEW)
                        intent.setData(Uri.parse(url))
                        startActivity(intent)
                        //exit.setText("温馨提示：支付成功将自动跳转，请耐心等待，您也可以选择提前退出，稍后查询充值结果。");
                    }
                    return true
                } else if (url.startsWith("i939l3://")) {
                    if (!AppUtils.isAppInstalled("com.mc.bourse")) {
                        Toaster.show("请先下载安装YESGO")
                    } else {
                        val intent = Intent()
                        intent.setAction(Intent.ACTION_VIEW)
                        intent.setData(Uri.parse(url))
                        startActivity(intent)
                        //exit.setText("温馨提示：支付成功将自动跳转，请耐心等待，您也可以选择提前退出，稍后查询充值结果。");
                    }
                    return true
                } else {
    //                    if (payInfo.getPayType() == PLAY_TYPE_WX) {
    //                        HashMap<String, String> extraHeaders = new HashMap();
    //                        extraHeaders.put("Referer", "https://cyfapi.chuanyf.com");
    //                        view.loadUrl(url, extraHeaders);
    //                        return true;
    //                    } else {
                    view.loadUrl(url)
                    //}
                    return super.shouldOverrideUrlLoading(view, url)
                }
            }

            override fun onReceivedError(webView: WebView, webResourceRequest: WebResourceRequest, webResourceError: WebResourceError) {
                super.onReceivedError(webView, webResourceRequest, webResourceError)
                if (webResourceRequest.isForMainFrame) {
                    Logs.e(webResourceRequest.url)
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) { // 注意这里的 Bitmap?
                super.onPageStarted(view, url, favicon)
                if (favicon != null) {
                }
            }
        }

        mDataBinding.webview.loadUrl(intent.getStringExtra(INTENT_KEY_IN_URL) ?: "")

    }

    override fun onRightClick(view: TitleBar) {
        super.onRightClick(view)

    }

    override fun createObserver() {

    }


    override fun onNetworkStateChanged(it: NetState) {
    }

    override fun onResume() {
        super.onResume()

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun finish() {
            finish()
        }

    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun h5Goback() {

        }
        @JavascriptInterface
        fun goBackGame() {
            finish()
        }
        @JavascriptInterface
        fun closeMe() {
        }
        @JavascriptInterface
        fun JumpAppAction(msg: String?) {
            Logs.e("AppJumpAction Json = $msg")
        }
        @JavascriptInterface
        fun wxH5PayBack(msg: String?) {
            Logs.e("wxH5PayBack")
            Logs.e("infoJson:$msg")
        }
        @JavascriptInterface
        fun payCallBack(msg: String?) {
        }

        @JavascriptInterface
        fun startWebView(msg: String) {
            try {
                appViewModel.appInfo.value?.marketjson?.let {
                    CommonActivityBrowser.start(appContext, it.xieyitanchuang_url_yinsi)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

}


