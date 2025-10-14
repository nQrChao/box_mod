package com.chaoji.mod.ui.activity.jiaoyi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.base.base.activity.BaseVmDbActivity
import com.chaoji.base.ext.parseModState
import com.chaoji.base.ext.parseModStateWithMsg
import com.chaoji.base.network.NetState
import com.chaoji.mod.BR
import com.chaoji.im.appContext
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.common.R as RC
import com.chaoji.mod.R
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.im.glide.GlideApp
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.toBrowser
import com.chaoji.im.ui.activity.CommonActivityBrowser
import com.chaoji.mod.BuildConfig
import com.chaoji.mod.databinding.ModActivityJiaoyiTradeDetailsBinding
import com.chaoji.mod.databinding.ModItemJiaoyiDetailsMoreBinding
import com.chaoji.mod.databinding.ModItemJiaoyiPriceProBinding
import com.chaoji.mod.ui.activity.image.ModActivityPreviewImageVideo
import com.chaoji.mod.ui.activity.login.ModActivityXDLogin
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

class ModActivityTradeDetails : BaseVmDbActivity<ModActivityTradeDetailsModel, ModActivityJiaoyiTradeDetailsBinding>() {
    var goodId = ""
    var goodDetailList: MutableList<ModTradeGoodDetailBean> = mutableListOf()
    var appDetailAdapter = ModTradeGoodDetailAdapter(goodDetailList)


    override fun layoutId(): Int = R.layout.mod_activity_jiaoyi_trade_details

    companion object {
        const val INTENT_KEY_GOOD_ID: String = "goodId"
        fun start(context: Context, goodId: String) {
            if (TextUtils.isEmpty(goodId)) {
                return
            }
            val intent = Intent(context, ModActivityTradeDetails::class.java)
            intent.putExtra(INTENT_KEY_GOOD_ID, goodId)
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
        appDetailAdapter.setDiffCallback(ModTradeGoodDetailDiffCallback())
        goodId = intent.getStringExtra(INTENT_KEY_GOOD_ID) ?: ""
        if (!StringUtils.isEmpty(goodId)) {
            //mViewModel.postTradeGoodDetail(goodId, "")
            mViewModel.postTradeGoodDetailAll(goodId, "")
        }

        mDataBinding.innerRecyclerView.run {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 1).toInt()))
            adapter = appDetailAdapter
        }

        appDetailAdapter.setOnItemClickListener { adapter, view, position ->
            val clickedItem = adapter.getItem(position) as ModTradeGoodDetailBean
            start(this@ModActivityTradeDetails, clickedItem.gid)
        }


    }

    fun setBanner(){
        mDataBinding.banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
        //设置图片加载器
        mDataBinding.banner.setImageLoader(object : ImageLoader() {
            override fun displayImage(context: Context, path: Any, imageView: ImageView) {
                val imageRes = path as String
                //val thumbUrl = "https://images.cqxiayou.com/imagethumb.php?thumb=w150h150&imgurl=$imageRes"
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
        mViewModel.tradeGoodsListResult.observe(this) { it ->
            parseModState(it, {
                it?.let {
                    if (it.size < 1) {
                        mDataBinding.xiaohaoList.visibility = View.GONE
                    }
                    setBanner()
                    appDetailAdapter.setList(it)
                    Logs.e("goodDetailList", GsonUtils.toJson(it))
                }
            }, {
            })
        }

        mViewModel.postShouCangResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    mViewModel.shoucang.set((msg?.contains("成功")))
                    Logs.e("postShouCangResult-onSuccess:$msg")
                },
                onError = {
                    Logs.e("postShouCangResult-onError:$it.msg")
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
        fun goWeb(){
            if (BuildConfig.APP_UPDATE_ID == "27"){
                CommonActivityBrowser.start(appContext, "https://mobile.xiaodianyouxi.com/index.php/Index/market_view/?id=566")
            }else{
                CommonActivityBrowser.start(appContext, "https://mobile.xiaodianyouxi.com/index.php/Index/market_view/?id=588")
            }
        }
        fun shoucang() {
            if (isLogin()) {
                //mViewModel.postShouCangByGameIdResul(goodId)
                mViewModel.tradeGoodDetail.value?.let {
                    val isNowCollected = MMKVUtil.toggleShouCang(it)
                    Toaster.show(if (isNowCollected) "收藏成功" else "已取消收藏")
                    mViewModel.shoucang.set(MMKVUtil.isShouCangCollected(goodId))
                }
            } else {
                Toaster.show("请先登录")
                ModActivityXDLogin.start(this@ModActivityTradeDetails)
            }
        }

        fun kefu() {
            appViewModel.appInfo.value.let {
                if (it != null) {
                    toBrowser(it.marketjson.wechat_url)
                }
            }
        }

        fun huanjia() {
            XPopup.Builder(this@ModActivityTradeDetails)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .isLightStatusBar(true)
                .autoFocusEditText(false)
                .autoOpenSoftInput(false)
                .navigationBarColor(ColorUtils.getColor(com.chaoji.common.R.color.white))
                .hasNavigationBar(true)
                .asCustom(
                    ModXPopupJiaoyiBottomHuanJia(this@ModActivityTradeDetails, {
                        //取消
                    }, {
                        if (isLogin()) {
                            XPopup.Builder(this@ModActivityTradeDetails)
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
                            ModActivityXDLogin.start(this@ModActivityTradeDetails)
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
                ModActivityXDLogin.start(this@ModActivityTradeDetails)
            }
        }

        fun pic1() {
            ModActivityPreviewImageVideo.start(this@ModActivityTradeDetails, mViewModel.photoList, 0)
        }

        fun pic2() {
            ModActivityPreviewImageVideo.start(this@ModActivityTradeDetails, mViewModel.photoList, 1)
        }

        fun pic3() {
            ModActivityPreviewImageVideo.start(this@ModActivityTradeDetails, mViewModel.photoList, 2)
        }


    }


    class ModTradeGoodDetailAdapter constructor(list: MutableList<ModTradeGoodDetailBean>) : BaseQuickAdapter<ModTradeGoodDetailBean, BaseDataBindingHolder<ModItemJiaoyiPriceProBinding>>(
        R.layout.mod_item_jiaoyi_price_pro, list
    ) {
        override fun convert(holder: BaseDataBindingHolder<ModItemJiaoyiPriceProBinding>, item: ModTradeGoodDetailBean) {
            holder.dataBinding?.setVariable(BR.goodDetailBean, item)
        }

        override fun onBindViewHolder(holder: BaseDataBindingHolder<ModItemJiaoyiPriceProBinding>, position: Int, payloads: MutableList<Any>) {
            super.onBindViewHolder(holder, position, payloads)
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
                return
            }

            val binding = holder.dataBinding
            val item = getItem(position)

            // 遍历所有的 payload
            for (payload in payloads) {
                if (payload == "LIKE_UPDATE") {
                    binding?.setVariable(BR.goodDetailBean, item)
                }
            }
        }

    }

    class ModTradeGoodDetailDiffCallback : DiffUtil.ItemCallback<ModTradeGoodDetailBean>() {
        override fun areItemsTheSame(oldItem: ModTradeGoodDetailBean, newItem: ModTradeGoodDetailBean): Boolean {
            return oldItem.gid == newItem.gid
        }

        override fun areContentsTheSame(oldItem: ModTradeGoodDetailBean, newItem: ModTradeGoodDetailBean): Boolean {
            return oldItem == newItem
        }
    }


}