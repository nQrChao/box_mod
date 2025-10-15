package com.box.mod.ui.activity.about

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.FileIOUtils
import com.box.other.blankj.utilcode.util.FileUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.blankj.utilcode.util.PathUtils
import com.box.other.blankj.utilcode.util.ResourceUtils
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.network.NetState
import com.box.common.data.model.UpdateList
import com.box.common.network.ApiService.Companion.XY_1_URL
import com.box.common.network.ApiService.Companion.XY_2_URL
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.mod.ui.xpop.ModXPopupCenterUpdate
import com.box.common.ui.xpop.XXXPopupCenter
import com.box.common.utils.MMKVUtil
import com.box.common.STORAGEPermission
import com.box.common.countClick
import com.box.common.generateTotpNumber
import com.box.mod.R
import com.box.com.R as RC
import com.box.mod.databinding.ModActivityAboutBinding
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.kongzue.baseokhttp.HttpRequest
import com.box.other.kongzue.baseokhttp.listener.OnDownloadListener
import com.hjq.permissions.XXPermissions
import com.box.other.xpopup.XPopup
import java.io.File

class ModActivityAbout : BaseVmDbActivity<ModActivityAboutModel, ModActivityAboutBinding>() {
    override fun layoutId(): Int = R.layout.mod_activity_about
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityAbout::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()

        mDataBinding.aboutIcon.setImageDrawable(AppUtils.getAppIcon()?: ResourceUtils.getDrawable(RC.drawable.audio_circle))
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
        fun aboutIcon() {
            countClick {
                XPopup.Builder(this@ModActivityAbout)
                    .dismissOnTouchOutside(false)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(true)
                    .isLightStatusBar(true)
                    .animationDuration(10)
                    .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                    .hasNavigationBar(true)
                    .asCustom(XXXPopupCenter(this@ModActivityAbout))
                    .show()
            }
        }

        fun aboutVersion() {
            MMKVUtil.saveGroupAtRead(null)
            MMKVUtil.saveGroupAnnRead(null)
            MMKVUtil.saveRemindList(null)
            countClick{
                generateTotpNumber("IM")
            }
        }

        fun gotoWeb1() {
            CommonActivityBrowser.start(this@ModActivityAbout, XY_1_URL)
        }

        fun gotoWeb2() {
            CommonActivityBrowser.start(this@ModActivityAbout, XY_2_URL)
        }

        fun checkVer() {
            XXPermissions.with(this@ModActivityAbout).permission(STORAGEPermission).request { _, all ->
                if (all) {
                    HttpRequest.DOWNLOAD(
                        this@ModActivityAbout,
                        XY_1_URL,
                        FileUtils.getFileByPath(PathUtils.getInternalAppFilesPath() + File.separator + "app.json"),
                        object : OnDownloadListener {
                            override fun onDownloadSuccess(file: File) {
                                val text = FileIOUtils.readFile2String(file)
                                val update = GsonUtils.fromJson(text, UpdateList::class.java)
                                if (AppUtils.getAppVersionName() != update.android?.version) {

                                    XPopup.Builder(this@ModActivityAbout)
                                        .dismissOnTouchOutside(false)
                                        .dismissOnBackPressed(false)
                                        .isDestroyOnDismiss(true)
                                        .hasStatusBar(true)
                                        .isLightStatusBar(true)
                                        .animationDuration(10)
                                        .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                                        .hasNavigationBar(true)
                                        .asCustom(
                                            ModXPopupCenterUpdate(this@ModActivityAbout, update, {}) {

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