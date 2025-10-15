package com.box.mod.ui.activity.jiaoyi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import com.box.base.base.action.ClickAction
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.network.NetState
import com.box.common.STORAGEPermission
import com.box.common.countClick
import com.box.common.data.model.UpdateList
import com.box.common.generateTotpNumber
import com.box.common.network.ApiService.Companion.XY_1_URL
import com.box.common.network.ApiService.Companion.XY_2_URL
import com.box.common.ui.activity.CommonActivityBrowser
import com.box.common.ui.widget.XCollapsingToolbarLayout
import com.box.common.ui.xpop.XXXPopupCenter
import com.box.common.utils.MMKVUtil
import com.box.mod.R
import com.box.mod.databinding.ModActivityJiaoyiTipBinding
import com.box.mod.ui.xpop.ModXPopupCenterUpdate
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.FileIOUtils
import com.box.other.blankj.utilcode.util.FileUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.blankj.utilcode.util.PathUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.kongzue.baseokhttp.HttpRequest
import com.box.other.kongzue.baseokhttp.listener.OnDownloadListener
import com.box.other.xpopup.XPopup
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.hjq.permissions.XXPermissions
import java.io.File
import com.box.com.R as RC

class ModActivityJiaoYiTip : BaseVmDbActivity<ModActivityJiaoYiXuZhiModel, ModActivityJiaoyiTipBinding>(), XCollapsingToolbarLayout.OnScrimsListener, ClickAction {
    private var isTabClickScrolling = false
    private lateinit var anchorViews: List<View>
    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_tip

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityJiaoYiTip::class.java)
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
            titleBar(mDataBinding.tbTitle)
            statusBarDarkFont(false)
            navigationBarColor(RC.color.white)
            init()
        }
        mDataBinding.ctlBar.setOnScrimsListener(this)

        anchorViews = listOf(
            mDataBinding.anchorPurchaseFlow,
            mDataBinding.anchorSellFlow,
            mDataBinding.anchorSellFaq,
            mDataBinding.anchorPurchaseFaq
        )

        setupScrollListener()

        mDataBinding.appBar.addOnOffsetChangedListener(object : OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                Logs.e("verticalOffset:", verticalOffset)
            }
        })

    }

    private fun setupScrollListener() {
        mDataBinding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (isTabClickScrolling) {
                return@OnScrollChangeListener
            }
            val stickyHeaderHeight = mDataBinding.tabLayout.height
            for (i in anchorViews.indices.reversed()) {
                val view = anchorViews[i]
                if (scrollY >= view.top - stickyHeaderHeight) {
                    // 如果计算出的Tab索引和当前已选中的不一致，则更新
                    if (mViewModel.selectedTab.get() != i) {
                        mViewModel.selectedTab.set(i)
                    }
                    // 找到后即可退出循环
                    return@OnScrollChangeListener
                }
            }
        })
    }

    private fun scrollToAnchor(index: Int) {
        if (index < 0 || index >= anchorViews.size) return
        val listener = object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (verticalOffset == 0) {
                    appBarLayout?.removeOnOffsetChangedListener(this)
                    val targetView = anchorViews[index]
                    val y = targetView.top
                    isTabClickScrolling = true
                    mDataBinding.nestedScrollView.smoothScrollTo(0, y, 500)
                    mDataBinding.root.postDelayed({
                        isTabClickScrolling = false
                    }, 550) // 延迟时间略长于滚动时间
                }
            }
        }
        mDataBinding.appBar.addOnOffsetChangedListener(listener)
        mDataBinding.appBar.setExpanded(true, true)
    }


    override fun onScrimsStateChange(layout: XCollapsingToolbarLayout?, shown: Boolean) {
        //getStatusBarConfig().statusBarDarkFont(shown).init()
        immersionBar { statusBarDarkFont(shown).init() }
        mViewModel.shown.set(shown)
        //mDataBinding.tvTitle.visibility
        //mDataBinding.tvBack.setImageResource(if (shown) com.chaoji.common.R.drawable.arrows_left_b_ic else com.chaoji.common.R.drawable.arrows_left_b_w_ic)
        //mDataBinding.tvTitle.setTextColor(ContextCompat.getColor(this, if (shown) com.chaoji.common.R.color.black80 else com.chaoji.common.R.color.white))
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
            scrollToAnchor(index)
        }

        fun finish() {
            this@ModActivityJiaoYiTip.finish()
        }

        fun aboutIcon() {
            countClick {
                XPopup.Builder(this@ModActivityJiaoYiTip)
                    .dismissOnTouchOutside(false)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(true)
                    .isLightStatusBar(true)
                    .animationDuration(10)
                    .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                    .hasNavigationBar(true)
                    .asCustom(XXXPopupCenter(this@ModActivityJiaoYiTip))
                    .show()
            }
        }

        fun aboutVersion() {
            MMKVUtil.saveGroupAtRead(null)
            MMKVUtil.saveGroupAnnRead(null)
            MMKVUtil.saveRemindList(null)
            countClick {
                generateTotpNumber("IM")
            }
        }

        fun gotoWeb1() {
            CommonActivityBrowser.start(this@ModActivityJiaoYiTip, XY_1_URL)
        }

        fun gotoWeb2() {
            CommonActivityBrowser.start(this@ModActivityJiaoYiTip, XY_2_URL)
        }

        fun checkVer() {
            XXPermissions.with(this@ModActivityJiaoYiTip).permission(STORAGEPermission).request { _, all ->
                if (all) {
                    HttpRequest.DOWNLOAD(
                        this@ModActivityJiaoYiTip,
                        XY_1_URL,
                        FileUtils.getFileByPath(PathUtils.getInternalAppFilesPath() + File.separator + "app.json"),
                        object : OnDownloadListener {
                            override fun onDownloadSuccess(file: File) {
                                val text = FileIOUtils.readFile2String(file)
                                val update = GsonUtils.fromJson(text, UpdateList::class.java)
                                if (AppUtils.getAppVersionName() != update.android?.version) {

                                    XPopup.Builder(this@ModActivityJiaoYiTip)
                                        .dismissOnTouchOutside(false)
                                        .dismissOnBackPressed(false)
                                        .isDestroyOnDismiss(true)
                                        .hasStatusBar(true)
                                        .isLightStatusBar(true)
                                        .animationDuration(10)
                                        .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                                        .hasNavigationBar(true)
                                        .asCustom(
                                            ModXPopupCenterUpdate(this@ModActivityJiaoYiTip, update, {}) {

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