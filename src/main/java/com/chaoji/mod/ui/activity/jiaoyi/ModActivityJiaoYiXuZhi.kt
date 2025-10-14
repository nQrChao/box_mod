package com.chaoji.mod.ui.activity.jiaoyi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.network.NetState
import com.chaoji.im.STORAGEPermission
import com.chaoji.im.countClick
import com.chaoji.im.data.model.UpdateList
import com.chaoji.im.generateTotpNumber
import com.chaoji.im.network.ApiService.Companion.XY_1_URL
import com.chaoji.im.network.ApiService.Companion.XY_2_URL
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.im.ui.xpop.XXXPopupCenter
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityJiaoyiXuzhiBinding
import com.chaoji.mod.ui.xpop.ModXPopupCenterUpdate
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.AppUtils
import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.other.blankj.utilcode.util.FileIOUtils
import com.chaoji.other.blankj.utilcode.util.FileUtils
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.blankj.utilcode.util.PathUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.other.kongzue.baseokhttp.HttpRequest
import com.chaoji.other.kongzue.baseokhttp.listener.OnDownloadListener
import com.chaoji.other.xpopup.XPopup
import com.hjq.permissions.XXPermissions
import java.io.File
import com.chaoji.common.R as RC

class ModActivityJiaoYiXuZhi : BaseVmDbActivity<ModActivityJiaoYiXuZhiModel, ModActivityJiaoyiXuzhiBinding>() {
    // 用于防止点击Tab滚动时，滚动监听又反过来触发Tab切换
    private var isTabClickScrolling = false
    private lateinit var anchorViews: List<TextView>
    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_xuzhi
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityJiaoYiXuZhi::class.java)
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

        anchorViews = listOf(
            mDataBinding.anchorPurchaseFlow,
            mDataBinding.anchorSellFlow,
            mDataBinding.anchorSellFaq,
            mDataBinding.anchorPurchaseFaq
        )

        setupScrollListener()

    }
    private fun setupScrollListener() {
        mDataBinding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (isTabClickScrolling) {
                return@OnScrollChangeListener
            }
            for (i in anchorViews.indices.reversed()) {
                val view = anchorViews[i]
                if (scrollY >= view.top) {
                    if (mViewModel.selectedTab.get() != i) {
                        mViewModel.selectedTab.set(i)
                    }
                    return@OnScrollChangeListener
                }
            }
        })
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
        fun onTabClick(index: Int) {
            mViewModel.selectedTab.set(index)
        }
        fun aboutIcon() {
            countClick {
                XPopup.Builder(this@ModActivityJiaoYiXuZhi)
                    .dismissOnTouchOutside(false)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(true)
                    .isLightStatusBar(true)
                    .animationDuration(10)
                    .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                    .hasNavigationBar(true)
                    .asCustom(XXXPopupCenter(this@ModActivityJiaoYiXuZhi))
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
            CommonActivityBrowser.start(this@ModActivityJiaoYiXuZhi, XY_1_URL)
        }

        fun gotoWeb2() {
            CommonActivityBrowser.start(this@ModActivityJiaoYiXuZhi, XY_2_URL)
        }

        fun checkVer() {
            XXPermissions.with(this@ModActivityJiaoYiXuZhi).permission(STORAGEPermission).request { _, all ->
                if (all) {
                    HttpRequest.DOWNLOAD(
                        this@ModActivityJiaoYiXuZhi,
                        XY_1_URL,
                        FileUtils.getFileByPath(PathUtils.getInternalAppFilesPath() + File.separator + "app.json"),
                        object : OnDownloadListener {
                            override fun onDownloadSuccess(file: File) {
                                val text = FileIOUtils.readFile2String(file)
                                val update = GsonUtils.fromJson(text, UpdateList::class.java)
                                if (AppUtils.getAppVersionName() != update.android?.version) {

                                    XPopup.Builder(this@ModActivityJiaoYiXuZhi)
                                        .dismissOnTouchOutside(false)
                                        .dismissOnBackPressed(false)
                                        .isDestroyOnDismiss(true)
                                        .hasStatusBar(true)
                                        .isLightStatusBar(true)
                                        .animationDuration(10)
                                        .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                                        .hasNavigationBar(true)
                                        .asCustom(
                                            ModXPopupCenterUpdate(this@ModActivityJiaoYiXuZhi, update, {}) {

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