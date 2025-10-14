package com.box.mod.ui.fragment

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chad.library.adapter.base.BaseProviderMultiAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.box.common.appContext
import com.box.common.data.model.AppletsLunTan
import com.box.common.data.model.ModTradeGoodDetailBean
import com.box.common.glide.GlideApp
import com.box.common.ui.adapter.HorizontalSpaceItemDecoration
import com.box.mod.R
import com.box.mod.databinding.*
import com.youth.banner.BannerConfig
import com.youth.banner.Transformer
import com.youth.banner.listener.OnBannerListener
import com.youth.banner.loader.ImageLoader


private const val ITEM_VIEW_TYPE_zixun01 = 0
private const val ITEM_VIEW_TYPE_zixun02 = 1
private const val ITEM_VIEW_TYPE_zixun03 = 2
private const val ITEM_VIEW_TYPE_zixun04 = 3
private const val ITEM_VIEW_TYPE_zixun05 = 4
private const val ITEM_VIEW_TYPE_zixun06 = 5
private const val ITEM_VIEW_TYPE_TITLE = 6
private const val ITEM_VIEW_TYPE_TOP_GOODS = 7
private const val ITEM_VIEW_TYPE_TITLE2 = 8
private const val ITEM_VIEW_TYPE_GOODS = 9

class Navigation11Adapter : BaseProviderMultiAdapter<ModItemList>() {
    private var clickHandler: Nav1ClickHandler? = null

    init {
        // 注册所有类型的ItemProvider
        addItemProvider(HeaderImageProvider())
        addItemProvider(TitleProvider())
        addItemProvider(TopGoodsProvider())
        addItemProvider(Title2Provider())
        addItemProvider(GoodsProvider())

        // 设置Diff回调
        setDiffCallback(Navigation11DiffCallback())
    }

    // 修复：实现 getItemType 方法
    override fun getItemType(data: List<ModItemList>, position: Int): Int {
        val item = data[position]
        return when (item) {
            is ModItemList.M -> ITEM_VIEW_TYPE_zixun01
            is ModItemList.TitleItem -> ITEM_VIEW_TYPE_TITLE
            is ModItemList.TopGoodsItem -> ITEM_VIEW_TYPE_TOP_GOODS
            is ModItemList.Title2Item -> ITEM_VIEW_TYPE_TITLE2
            is ModItemList.GoodsItem -> ITEM_VIEW_TYPE_GOODS
        }
    }

    fun setClickHandler(handler: Nav1ClickHandler) {
        this.clickHandler = handler
    }

    // HeaderImage类型
    inner class HeaderImageProvider : BaseItemProvider<ModItemList>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_zixun01
        override val layoutId: Int = R.layout.mod_item_jiaoyi_f1_top_header

        override fun convert(holder: BaseViewHolder, item: ModItemList) {
            val binding = ModItemJiaoyiF1TopHeaderBinding.bind(holder.itemView)
            val headerItem = item as ModItemList.M
            binding.clickHandler = clickHandler
            binding.list = headerItem.list

            binding.banner.apply {
                setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
                setImageLoader(object : ImageLoader() {
                    override fun displayImage(context: Context, path: Any, imageView: ImageView) {
                        GlideApp.with(appContext)
                            .load(path as Int)
                            .into(imageView)
                    }
                })
                setImages(headerItem.list)
                setBannerAnimation(Transformer.Default)
                setDelayTime(5000)
                isAutoPlay(true)
                setIndicatorGravity(BannerConfig.CIRCLE_INDICATOR_TITLE)
                setOnBannerListener(OnBannerListener { position -> })
                start()
            }
        }
    }

    // Title类型
    inner class TitleProvider : BaseItemProvider<ModItemList>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_TITLE
        override val layoutId: Int = R.layout.mod_item_section_title
        override fun convert(holder: BaseViewHolder, item: ModItemList) {
            val binding = ModItemSectionTitleBinding.bind(holder.itemView)
            val titleItem = item as ModItemList.TitleItem

            binding.clickHandler = clickHandler
            binding.title = titleItem.title
            binding.showIcon = titleItem.showIcon
            binding.showMore = titleItem.showMore
        }
    }

    // TopGoods类型
    inner class TopGoodsProvider : BaseItemProvider<ModItemList>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_TOP_GOODS
        override val layoutId: Int = R.layout.mod_item_nested_recycler_top

        override fun convert(holder: BaseViewHolder, item: ModItemList) {
            val binding = ModItemNestedRecyclerTopBinding.bind(holder.itemView)
            val topGoodsItem = item as ModItemList.TopGoodsItem

            // 确保只初始化一次
            if (binding.innerRecyclerView.adapter == null) {
                binding.innerRecyclerView.apply {
                    adapter = TopGoodsInnerAdapter().apply {
                        setOnItemClickListener { _, _, position ->
                            clickHandler?.onTopGoodsItemClick(getItem(position))
                        }
                        setOnItemChildClickListener { _, view, position ->
                            clickHandler?.onTopGoodsItemChildClick(getItem(position), view)
                        }
                    }
                    layoutManager = LinearLayoutManager(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    addItemDecoration(HorizontalSpaceItemDecoration(10))
                }
            }

            // 更新数据
            (binding.innerRecyclerView.adapter as TopGoodsInnerAdapter).setList(topGoodsItem.goodsList)
        }
    }

    // Title2类型
    inner class Title2Provider : BaseItemProvider<ModItemList>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_TITLE2
        override val layoutId: Int = R.layout.mod_item_section_title

        override fun convert(holder: BaseViewHolder, item: ModItemList) {
            val binding = ModItemSectionTitleBinding.bind(holder.itemView)
            val title2Item = item as ModItemList.Title2Item

            binding.clickHandler = clickHandler
            binding.title = title2Item.title
            binding.showIcon = title2Item.showIcon
            binding.showMore = title2Item.showMore
        }
    }

    // Goods类型
    inner class GoodsProvider : BaseItemProvider<ModItemList>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_GOODS
        override val layoutId: Int = R.layout.mod_item_jiaoyi_price_pro

        override fun convert(holder: BaseViewHolder, item: ModItemList) {
            val binding = ModItemJiaoyiPriceProBinding.bind(holder.itemView)
            val goodsItem = item as ModItemList.GoodsItem

            binding.goodDetailBean = goodsItem.goodDetail
            holder.itemView.setOnClickListener {
                clickHandler?.onMainGoodsItemClick(goodsItem.goodDetail)
            }
            binding.pic1.setOnClickListener {
                clickHandler?.onMainGoodsItemChildClick(goodsItem.goodDetail, it)
            }
            binding.goumaiLayout.setOnClickListener {
                clickHandler?.onMainGoodsItemChildClick(goodsItem.goodDetail, it)
            }
        }
    }
}

// 修复嵌套适配器
class TopGoodsInnerAdapter : BaseQuickAdapter<ModTradeGoodDetailBean, BaseViewHolder>(R.layout.mod_item_jiaoyi_price) {
    override fun convert(holder: BaseViewHolder, item: ModTradeGoodDetailBean) {
        // 使用数据绑定
        val binding = ModItemJiaoyiPriceBinding.bind(holder.itemView)
        binding.goodDetailBean = item
        binding.executePendingBindings()
    }
}

class MainGoodsInnerAdapter : BaseQuickAdapter<ModTradeGoodDetailBean, BaseViewHolder>(R.layout.mod_item_jiaoyi_price_pro) {
    override fun convert(holder: BaseViewHolder, item: ModTradeGoodDetailBean) {
        // 使用数据绑定
        val binding = ModItemJiaoyiPriceProBinding.bind(holder.itemView)
        binding.goodDetailBean = item
        binding.executePendingBindings()
    }
}

class Navigation11DiffCallback : DiffUtil.ItemCallback<ModItemList>() {
    override fun areItemsTheSame(oldItem: ModItemList, newItem: ModItemList): Boolean {
        if (oldItem is ModItemList.GoodsItem && newItem is ModItemList.GoodsItem) {
            return oldItem.goodDetail.gameid == newItem.goodDetail.gameid
        }
        return oldItem::class == newItem::class
    }

    override fun areContentsTheSame(oldItem: ModItemList, newItem: ModItemList): Boolean {
        return oldItem == newItem
    }
}

sealed class ModItemList {
    data class Modzixun01(val ziXunList: List<AppletsLunTan>) : ModItemList()
    data class TitleItem(val title: String, val showIcon: Boolean, val showMore: Boolean) : ModItemList()
    data class TopGoodsItem(val goodsList: List<ModTradeGoodDetailBean>) : ModItemList()
    data class Title2Item(val title: String, val showIcon: Boolean, val showMore: Boolean) : ModItemList()
    data class GoodsItem(val goodDetail: ModTradeGoodDetailBean) : ModItemList()
}


interface Nav1ClickHandler {
    fun onTitleImageClick(goodDetail: ModTradeGoodDetailBean)
    fun onTopGoodClick(goodDetail: ModTradeGoodDetailBean)
    fun onSearchTitleClick(view: View)
    fun onSearchClick()
    fun onJiaoYiClick()
    fun onLeYuanClick()
    fun onReLiaoClick()
    fun onTopGoodsItemClick(goodDetail: ModTradeGoodDetailBean)
    fun onTopGoodsItemChildClick(goodDetail: ModTradeGoodDetailBean, view: View)
    fun onMainGoodsItemClick(goodDetail: ModTradeGoodDetailBean)
    fun onMainGoodsItemChildClick(goodDetail: ModTradeGoodDetailBean, view: View)
}