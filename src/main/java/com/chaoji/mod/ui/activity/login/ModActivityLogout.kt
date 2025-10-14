package com.chaoji.mod.ui.activity.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.CompoundButton
import androidx.core.text.HtmlCompat
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModStateWithMsg
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.sdk.eventViewModel
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityLoginLogoutBinding
import com.chaoji.mod.game.ModManager
import com.chaoji.mod.ui.activity.ModActivityMain
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.AppUtils
import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.other.xpopup.XPopup
import com.chaoji.common.R as RC

class ModActivityLogout : BaseVmDbActivity<ModActivityLogoutModel, ModActivityLoginLogoutBinding>() {
    private var contentText = "我已阅读并同意《注销协议》"
    private val linkTextColor = Color.parseColor("#007BFF")
    override fun layoutId(): Int = R.layout.mod_activity_login_logout

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
        mDataBinding.tvAgree.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        mDataBinding.tvAgree.highlightColor = Color.TRANSPARENT


    }

    override fun createObserver() {
        mViewModel.postLogoutCheckResultWithMsg.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    mViewModel.logoutRequest()
                },
                onError = {
                    Toaster.show(it.msg)
                    Logs.e("postLogoutResultWithMsg", "ERROR", it.msg)
                    XPopup.Builder(this@ModActivityLogout)
                        .isDestroyOnDismiss(true)
                        .hasStatusBar(true)
                        .animationDuration(5)
                        .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                        .isLightStatusBar(true)
                        .hasNavigationBar(true)
                        .asConfirm(
                            "无法注销", "账户未处于安全状态，请7天内不要进行绑定手机、换绑手机、找回密码、修改密码等操作后再尝试！",
                            "", "确定",
                            {
                                finish()
                            }, null, true, R.layout.xpopup_confirm_mod
                        ).show()

                }
            )
        }
        mViewModel.postLogoutResultWithMsg.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    ModManager.provider.logout()
                    XPopup.Builder(this@ModActivityLogout)
                        .isDestroyOnDismiss(false)
                        .hasStatusBar(true)
                        .animationDuration(5)
                        .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                        .isLightStatusBar(true)
                        .hasNavigationBar(true)
                        .asConfirm(
                            "注销成功", "实名信息、手机信息、相关第三方授权已释放删除，即时起你将不可再登录现有账号，再次使用手机号登录将会创建一个全新账号。\n*原注销账号数据将在7日内完全删除",
                            "", "确定",
                            {
                                MMKVUtil.saveModUser(null)
                                appViewModel.modUserInfo.postValue(null)
                                eventViewModel.isLogin.postValue(false)
                                ModActivityMain.start(this@ModActivityLogout)
                                finish()
                            }, null, true, R.layout.xpopup_confirm_mod
                        ).show()
                },
                onError = {
                    Logs.e("postLogoutResultWithMsg", "ERROR", it.msg)
                    Toaster.show(it.msg)
                }
            )
        }

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


}