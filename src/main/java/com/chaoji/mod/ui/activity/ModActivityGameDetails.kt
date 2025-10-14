package com.chaoji.mod.ui.activity

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
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModStateWithMsg
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.data.model.ModGameLabelBean
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.im.glide.GlideApp
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.toBrowser
import com.chaoji.im.ui.adapter.HorizontalSpaceItemDecoration
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ModActivityGameDetailsBinding
import com.chaoji.mod.databinding.ModItemGameDetailPicBinding
import com.chaoji.mod.ui.activity.image.ModActivityPreviewImageVideo
import com.chaoji.mod.ui.activity.jiaoyi.ModActivityGouMai
import com.chaoji.mod.ui.activity.jiaoyi.ModActivityJiaoYiShiMing
import com.chaoji.mod.ui.activity.jiaoyi.ModXPopupJiaoyiBottomHuanJia
import com.chaoji.mod.ui.activity.login.ModActivityXDLogin
import com.chaoji.mod.ui.adapter.ModGameLabelAdapter
import com.chaoji.mod.ui.xpop.XPopupBottomYouHui
import com.chaoji.other.blankj.utilcode.util.ActivityUtils
import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.other.hjq.titlebar.TitleBar
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.other.xpopup.XPopup
import com.youth.banner.BannerConfig
import com.youth.banner.Transformer
import com.youth.banner.listener.OnBannerListener
import com.youth.banner.loader.ImageLoader
import com.chaoji.common.R as RC

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
                            .navigationBarColor(ColorUtils.getColor(com.chaoji.common.R.color.xpop_shadow_color))
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
                .navigationBarColor(ColorUtils.getColor(com.chaoji.common.R.color.white))
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
                                .navigationBarColor(ColorUtils.getColor(com.chaoji.common.R.color.xpop_shadow_color))
                                .isLightStatusBar(true)
                                .hasNavigationBar(true)
                                .asConfirm(
                                    "提示", it,
                                    "取消", "确定",
                                    {
                                    }, null, true, com.chaoji.common.R.layout.xpopup_confirm
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
                .shadowBgColor(ColorUtils.getColor(com.chaoji.common.R.color.black50))
                .navigationBarColor(ColorUtils.getColor(com.chaoji.common.R.color.white))
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