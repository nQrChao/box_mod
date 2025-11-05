package com.box.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.CompoundButton
import androidx.core.graphics.toColorInt
import androidx.core.text.HtmlCompat
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.appViewModel
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.R
import com.box.mod.databinding.ModActivityLogoutBinding
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.box.com.R as RC

class ModActivityLogout : BaseVmDbActivity<ModActivityLogout.Model, ModActivityLogoutBinding>() {
    private var contentText = "我已阅读并同意《注销协议》"
    private val linkTextColor = "#007BFF".toColorInt()
    override fun layoutId(): Int = R.layout.mod_activity_logout

    private val userAgreementClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            appViewModel.appInfo.value?.marketjson?.xieyitanchuang_url_zhuxiao?.let {
                CommonActivityBrowser.start(appContext, it)
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityLogout::class.java)
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

        val spannableString = SpannableString(
            HtmlCompat.fromHtml(contentText.replace("\n", "<br>"), HtmlCompat.FROM_HTML_MODE_LEGACY)
        )
        val startIndex = contentText.indexOf("《注销协议》")
        val endIndex = startIndex + "《注销协议》".length
        if (startIndex >= 0) {
            spannableString.setSpan(userAgreementClickableSpan, startIndex, endIndex, 0)
            spannableString.setSpan(ForegroundColorSpan(linkTextColor), startIndex, endIndex, 0)
        }
        mDataBinding.tvAgree.text = spannableString
        mDataBinding.tvAgree.movementMethod = LinkMovementMethod.getInstance()
        mDataBinding.tvAgree.highlightColor = Color.TRANSPARENT


    }

    override fun createObserver() {

//        ModManager.provider.logout()
//        XPopup.Builder(this@ModActivityLogout)
//            .isDestroyOnDismiss(false)
//            .hasStatusBar(true)
//            .animationDuration(5)
//            .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
//            .isLightStatusBar(true)
//            .hasNavigationBar(true)
//            .asConfirm(
//                "注销成功", "实名信息、手机信息、相关第三方授权已释放删除，即时起你将不可再登录现有账号，再次使用手机号登录将会创建一个全新账号。\n*原注销账号数据将在7日内完全删除",
//                "", "确定",
//                {
//                    MMKVConfig.userInfo = null
//                    appViewModel.modUserInfo.postValue(null)
//                    eventViewModel.isLogin.value = false
//                    ModActivityMain.start(this@ModActivityLogout)
//                    finish()
//                }, null, true, R.layout.xpopup_confirm_mod
//            ).show()

    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun logout() {
            if (!mDataBinding.agreementButton.isChecked) {
                Toaster.show("请先阅读同意《注销协议》")
                return
            }
            XPopup.Builder(this@ModActivityLogout)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .animationDuration(5)
                .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                .isLightStatusBar(true)
                .hasNavigationBar(true)
                .asConfirm(
                    "注销提示", "账户可能存在可用财产，建议使用完毕再注销账号，若仍要注销将视为你自愿放弃且无法继续使用！" + AppUtils.getAppName(),
                    "暂不注销", "确定注销",
                    {
                        mViewModel.zhuxiaoShowView.set(1)
                    }, null, false, R.layout.xpopup_confirm_mod
                ).show()

        }

        fun logout2() {
            if (mViewModel.password.get().length < 6) {
                Toaster.show("密码长度应不少于6位数")
                return
            }
            if (mViewModel.password.get().length > 18) {
                Toaster.show("密码长度应不大于18位数")
                return
            }
            mViewModel.logoutCheckRequest()
        }


        var agreementButtonChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            mDataBinding.agreementButton.isChecked = isChecked
            mViewModel.agreementChecked.set(isChecked)
        }
    }



    class Model : BaseViewModel(title = "账号注销") {
        var zhuxiaoShowView = IntObservableField(0)
        var zhuxiaoText = StringObservableField()
        var agreementChecked = BooleanObservableField(false)
        var mobileNum = StringObservableField("")
        var password = StringObservableField("")



        fun logoutCheckRequest() {
            val user = MMKVConfig.userInfo
            Logs.e("USER:${GsonUtils.toJson(user)}")
            if (user != null) {

            }
        }


        fun logoutRequest() {
            val user = MMKVConfig.userInfo
            Logs.e("USER:${GsonUtils.toJson(user)}")
            if (user != null) {

            }
        }



    }


}