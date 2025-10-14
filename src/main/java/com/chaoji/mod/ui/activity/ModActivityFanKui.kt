package com.chaoji.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.chaoji.other.blankj.utilcode.util.AppUtils
import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.other.blankj.utilcode.util.FileIOUtils
import com.chaoji.other.blankj.utilcode.util.FileUtils
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.blankj.utilcode.util.PathUtils
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.network.NetState
import com.chaoji.im.data.model.UpdateList
import com.chaoji.im.network.ApiService.Companion.XY_1_URL
import com.chaoji.im.ui.xpop.XXXPopupCenter
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.im.STORAGEPermission
import com.chaoji.im.appContext
import com.chaoji.im.countClick
import com.chaoji.mod.R
import com.chaoji.common.R as RC
import com.chaoji.mod.databinding.ModActivityFankuiBinding
import com.chaoji.mod.ui.xpop.ModXPopupCenterUpdate
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.other.kongzue.baseokhttp.HttpRequest
import com.chaoji.other.kongzue.baseokhttp.listener.OnDownloadListener
import com.hjq.permissions.XXPermissions
import com.chaoji.other.xpopup.XPopup
import java.io.File

class ModActivityFanKui : BaseVmDbActivity<ModActivityFanKuiModel, ModActivityFankuiBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_fankui

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityFanKui::class.java)
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
        fun hidePop() {
            countClick {
                XPopup.Builder(this@ModActivityFanKui)
                    .dismissOnTouchOutside(false)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(true)
                    .isLightStatusBar(true)
                    .animationDuration(10)
                    .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                    .hasNavigationBar(true)
                    .asCustom(XXXPopupCenter(this@ModActivityFanKui))
                    .show()
            }
        }

        fun aboutVersion() {
            MMKVUtil.saveGroupAtRead(null)
            MMKVUtil.saveGroupAnnRead(null)
            MMKVUtil.saveRemindList(null)
            countClick {
            }
        }

        fun send() {
            if (mViewModel.questionText.get().length < 15) {
                mDataBinding.modQuestionEdit.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                Toaster.show("亲，能否说的再详细一点呢(大于15个字)")
            } else {
                if (StringUtils.isEmpty(mViewModel.qqText.get())) {
                    mDataBinding.qqText.startAnimation(AnimationUtils.loadAnimation(appContext, RC.anim.shake_anim))
                    Toaster.show("请输入您的QQ号码")
                } else {
                    mViewModel.qqText.set("")
                    mViewModel.questionText.set("")
                    Toaster.show("提交成功，感谢您的建议")
                    finish()
                }
            }
        }


        fun checkVer() {
            XXPermissions.with(this@ModActivityFanKui).permission(STORAGEPermission)
                .request { _, all ->
                    if (all) {
                        HttpRequest.DOWNLOAD(
                            this@ModActivityFanKui,
                            XY_1_URL,
                            FileUtils.getFileByPath(PathUtils.getInternalAppFilesPath() + File.separator + "app.json"),
                            object : OnDownloadListener {
                                override fun onDownloadSuccess(file: File) {
                                    val text = FileIOUtils.readFile2String(file)
                                    val update = GsonUtils.fromJson(text, UpdateList::class.java)
                                    if (AppUtils.getAppVersionName() != update.android?.version) {

                                        XPopup.Builder(this@ModActivityFanKui)
                                            .dismissOnTouchOutside(false)
                                            .dismissOnBackPressed(false)
                                            .isDestroyOnDismiss(true)
                                            .hasStatusBar(true)
                                            .isLightStatusBar(true)
                                            .animationDuration(10)
                                            .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                                            .hasNavigationBar(true)
                                            .asCustom(
                                                ModXPopupCenterUpdate(
                                                    this@ModActivityFanKui,
                                                    update,
                                                    {}) {

                                                })
                                            .show()
                                    } else {
                                        Toaster.show("已是最新版本")
                                    }
                                }

                                override fun onDownloading(progress: Int) {

                                }

                                override fun onDownloadFailed(e: Exception) {
                                    Logs.e("下载失败", e.printStackTrace())
                                }
                            }
                        )
                    } else {
                        Toaster.show("授权失败，请手动授权")
                    }
                }

        }
    }


}


