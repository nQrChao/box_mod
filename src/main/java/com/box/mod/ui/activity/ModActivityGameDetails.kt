package com.box.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.data.model.ModGameLabelBean
import com.box.common.glide.GlideApp
import com.box.common.sdk.appViewModel
import com.box.common.toBrowser
import com.box.common.ui.adapter.HorizontalSpaceItemDecoration
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.mod.BR
import com.box.mod.R
import com.box.mod.databinding.ModActivityGameDetailsBinding
import com.box.mod.databinding.ModItemGameDetailPicBinding
import com.box.mod.ui.activity.image.ModActivityPreviewImageVideo
import com.box.mod.ui.activity.jiaoyi.ModActivityGouMai
import com.box.mod.ui.activity.jiaoyi.ModActivityJiaoYiShiMing
import com.box.mod.ui.activity.jiaoyi.ModXPopupJiaoyiBottomHuanJia
import com.box.mod.ui.activity.login.ModActivityXDLogin
import com.box.mod.ui.adapter.ModGameLabelAdapter
import com.box.mod.ui.xpop.XPopupBottomYouHui
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.hjq.titlebar.TitleBar
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.youth.banner.BannerConfig
import com.youth.banner.Transformer
import com.youth.banner.listener.OnBannerListener
import com.youth.banner.loader.ImageLoader
import com.box.com.R as RC

class ModActivityGameDetails : BaseVmDbActivity<ModActivityGameDetailsModel, ModActivityGameDetailsBinding>() {
    var gameId = ""
    var gameTipsList: MutableList<ModGameLabelBean> = mutableListOf()
    var gameTipsAdapter = ModGameLabelAdapter(gameTipsList)

    var gamePicList: MutableList<String> = mutableListOf()
    var gamePicAdapter = ModGameDetailPicAdapter(gamePicList)

    override fun layoutId(): Int = R.layout.mod_activity_game_details

    companion object {
        const val INTENT_KEY_GAME_ID: String = "gameID"
        fun start(context: Context, goodId: String) {
            if (TextUtils.isEmpty(goodId)) {
                return
            }
            val intent = Intent(context, ModActivityGameDetails::class.java)
            intent.putExtra(INTENT_KEY_GAME_ID, goodId)
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
        gameId = intent.getStringExtra(INTENT_KEY_GAME_ID) ?: ""
        if (!StringUtils.isEmpty(gameId)) {
            mViewModel.postGetGameInfo(gameId)
        }

        mDataBinding.recyclerViewTips.run {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(HorizontalSpaceItemDecoration(2))
            adapter = gameTipsAdapter
        }

        mDataBinding.recyclerViewPic.run {
            layoutManager = GridLayoutManager(context, 3)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 1).toInt()))
            adapter = gamePicAdapter
        }

        gamePicAdapter.setOnItemClickListener { adapter, view, position ->
            //val clickedItem = adapter.getItem(position) as ModTradeGoodDetailBean
            //start(this@ModActivityGameDetails, clickedItem.gid)
        }


    }

    fun setBanner() {
        mDataBinding.banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
        mDataBinding.banner.setImageLoader(object : ImageLoader() {
            override fun displayImage(context: Context, path: Any, imageView: ImageView) {
                val imageRes = path as String
                GlideApp.with(appContext)
                    .load(imageRes)
                    .into(imageView)
            }
        })
        mDataBinding.banner.setImages(mViewModel.photoBannerList)
        mDataBinding.banner.setBannerAnimation(Transformer.Default)
        mDataBinding.banner.setDelayTime(5000)
        mDataBinding.banner.isAutoPlay(true)
        mDataBinding.banner.setIndicatorGravity(BannerConfig.CENTER)
        mDataBinding.banner.setOnBannerListener(OnBannerListener { position: Int ->
            //ModActivityPreviewImageVideo.start(this@ModActivityTradeDetails, mViewModel.photoList, position)
        })
        mDataBinding.banner.start()
        mDataBinding.executePendingBindings()
    }

    override fun createObserver() {
        mViewModel.gameInfoResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    mViewModel.postGetGameFuLiInfo(gameId)
                    mViewModel.generateRandomScore()
                    mViewModel.generateRandomDownloadCount()
                    mViewModel.modGameInfo.value = data
                    if (data != null) {
                        gameTipsAdapter.setList(data.game_labels)
                        gamePicAdapter.setList(data.screenshot.take(3))
                        mViewModel.photoBannerList.add(data.bg_pic)
                        setBanner()
                    }

                },
                onError = {

                }
            )
        }

        mViewModel.gameFuLiInfoResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    Logs.e("FULIINFO:${data}")
                    mViewModel.modFuLiInfo.value = data
                    mViewModel.postYouHuiQuan()
                },
                onError = {

                }
            )
        }

        mViewModel.postYouHuiQuanLingQuResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    if (msg == "ok") {
                        Toaster.show("领取成功")
                    } else {
                        Toaster.show(msg)
                    }

                },
                onError = {
                    if (it.msg.contains("您还未实名")) {
                        XPopup.Builder(this@ModActivityGameDetails)
                            .isDestroyOnDismiss(true)
                            .hasStatusBar(true)
                            .animationDuration(10)
                            .navigationBarColor(ColorUtils.getColor(com.box.com.R.color.xpop_shadow_color))
                            .isLightStatusBar(true)
                            .hasNavigationBar(true)
                            .asConfirm(
                                "实名", it.msg + "\n是否前往实名认证？",
                                "取消", "确定",
                                {
                                    ActivityUtils.startActivity(ModActivityJiaoYiShiMing::class.java)
                                }, null, false, R.layout.xpopup_confirm_mod
                            ).show()
                    } else {
                        Toaster.show(it.msg)
                    }

                }
            )
        }

        mViewModel.postGameYouHuiResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    if (data != null) {
                        mViewModel.myGameYouHui.value = data
                        if (mViewModel.doesMyCouponListContainFirstFuLiGameId()) {
                            mViewModel.lingQuType.set(true)
                        }
                    }
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }


    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    override fun onRightClick(view: TitleBar) {
        super.onRightClick(view)
        //ModActivityJiaoYiTip.start(appContext)
    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {

        fun kefu() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    toBrowser(it.marketjson.wechat_url)
                }
            }
        }

        fun huanjia() {
            XPopup.Builder(this@ModActivityGameDetails)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .isLightStatusBar(true)
                .autoFocusEditText(false)
                .autoOpenSoftInput(false)
                .navigationBarColor(ColorUtils.getColor(com.box.com.R.color.white))
                .hasNavigationBar(true)
                .asCustom(
                    ModXPopupJiaoyiBottomHuanJia(this@ModActivityGameDetails, {
                        //取消
                    }, {
                        if (isLogin()) {
                            XPopup.Builder(this@ModActivityGameDetails)
                                .isDestroyOnDismiss(true)
                                .hasStatusBar(true)
                                .animationDuration(5)
                                .navigationBarColor(ColorUtils.getColor(com.box.com.R.color.xpop_shadow_color))
                                .isLightStatusBar(true)
                                .hasNavigationBar(true)
                                .asConfirm(
                                    "提示", it,
                                    "取消", "确定",
                                    {
                                    }, null, true, com.box.com.R.layout.xpopup_confirm
                                ).show()
                        } else {
                            Toaster.show("请先登录")
                            ModActivityXDLogin.start(this@ModActivityGameDetails)
                        }
                    })
                )
                .show()
        }

        fun goumai() {
            if (isLogin()) {
                ModActivityGouMai.start(appContext, GsonUtils.toJson(mViewModel.tradeGoodDetail.value))
            } else {
                Toaster.show("请先登录")
                ModActivityXDLogin.start(this@ModActivityGameDetails)
            }
        }

        fun checkFuLi() {
            if (mViewModel.modFuLiInfo.value?.coupon_count == "0") {
                Toaster.show("该游戏暂无福利")
                return
            }
            XPopup.Builder(this@ModActivityGameDetails)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .isLightStatusBar(true)
                .autoFocusEditText(false)
                .autoOpenSoftInput(false)
                .shadowBgColor(ColorUtils.getColor(com.box.com.R.color.black50))
                .navigationBarColor(ColorUtils.getColor(com.box.com.R.color.white))
                .hasNavigationBar(true)
                .asCustom(
                    mViewModel.modFuLiInfo.value?.let {
                        XPopupBottomYouHui(this@ModActivityGameDetails, mViewModel.lingQuType.get(), it, {

                        }, {
                            it.let { mViewModel.postYouHuiQuanLingQuApi(it.coupon_list[0].id) }
                        })
                    }
                )
                .show()
        }

        fun pic1() {
            ModActivityPreviewImageVideo.start(this@ModActivityGameDetails, mViewModel.photoList, 0)
        }

        fun pic2() {
            ModActivityPreviewImageVideo.start(this@ModActivityGameDetails, mViewModel.photoList, 1)
        }

        fun pic3() {
            ModActivityPreviewImageVideo.start(this@ModActivityGameDetails, mViewModel.photoList, 2)
        }


    }


    class ModGameDetailPicAdapter constructor(list: MutableList<String>) : BaseQuickAdapter<String, BaseDataBindingHolder<ModItemGameDetailPicBinding>>(
        R.layout.mod_item_game_detail_pic, list
    ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemGameDetailPicBinding>, item: String) {
            holder.dataBinding?.setVariable(BR.picPath, item)
        }
    }


}