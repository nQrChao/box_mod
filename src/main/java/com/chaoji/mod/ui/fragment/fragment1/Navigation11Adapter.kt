package com.chaoji.mod.ui.fragment.fragment1

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chad.library.adapter.base.BaseProviderMultiAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chaoji.im.appContext
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.im.glide.GlideApp
import com.chaoji.im.ui.adapter.HorizontalSpaceItemDecoration
import com.chaoji.mod.BR
import com.chaoji.mod.R
import com.chaoji.mod.databinding.*
import com.youth.banner.BannerConfig
import com.youth.banner.Transformer
import com.youth.banner.listener.OnBannerListener
import com.youth.banner.loader.ImageLoader

class Navigation11Adapter : BaseProviderMultiAdapter<Navigation11ListItem>() {
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
    override fun getItemType(data: List<Navigation11ListItem>, position: Int): Int {
        val item = data[position]
        return when (item) {
            is Navigation11ListItem.HeaderImageItem -> ITEM_VIEW_TYPE_HEADER_IMAGE
            is Navigation11ListItem.TitleItem -> ITEM_VIEW_TYPE_TITLE
            is Navigation11ListItem.TopGoodsItem -> ITEM_VIEW_TYPE_TOP_GOODS
            is Navigation11ListItem.Title2Item -> ITEM_VIEW_TYPE_TITLE2
            is Navigation11ListItem.GoodsItem -> ITEM_VIEW_TYPE_GOODS
        }
    }

    fun setClickHandler(handler: Nav1ClickHandler) {
        this.clickHandler = handler
    }

    // HeaderImage类型
    inner class HeaderImageProvider : BaseItemProvider<Navigation11ListItem>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_HEADER_IMAGE
        override val layoutId: Int = R.layout.mod_item_jiaoyi_f1_top_header

        override fun convert(holder: BaseViewHolder, item: Navigation11ListItem) {
            val binding = ModItemJiaoyiF1TopHeaderBinding.bind(holder.itemView)
            val headerItem = item as Navigation11ListItem.HeaderImageItem
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
    inner class TitleProvider : BaseItemProvider<Navigation11ListItem>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_TITLE
        override val layoutId: Int = R.layout.mod_item_section_title
        override fun convert(holder: BaseViewHolder, item: Navigation11ListItem) {
            val binding = ModItemSectionTitleBinding.bind(holder.itemView)
            val titleItem = item as Navigation11ListItem.TitleItem

            binding.clickHandler = clickHandler
            binding.title = titleItem.title
            binding.showIcon = titleItem.showIcon
            binding.showMore = titleItem.showMore
        }
    }

    // TopGoods类型
    inner class TopGoodsProvider : BaseItemProvider<Navigation11ListItem>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_TOP_GOODS
        override val layoutId: Int = R.layout.mod_item_nested_recycler_top

        override fun convert(holder: BaseViewHolder, item: Navigation11ListItem) {
            val binding = ModItemNestedRecyclerTopBinding.bind(holder.itemView)
            val topGoodsItem = item as Navigation11ListItem.TopGoodsItem

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
    inner class Title2Provider : BaseItemProvider<Navigation11ListItem>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_TITLE2
        override val layoutId: Int = R.layout.mod_item_section_title

        override fun convert(holder: BaseViewHolder, item: Navigation11ListItem) {
            val binding = ModItemSectionTitleBinding.bind(holder.itemView)
            val title2Item = item as Navigation11ListItem.Title2Item

            binding.clickHandler = clickHandler
            binding.title = title2Item.title
            binding.showIcon = title2Item.showIcon
            binding.showMore = title2Item.showMore
        }
    }

    // Goods类型
    inner class GoodsProvider : BaseItemProvider<Navigation11ListItem>() {
        override val itemViewType: Int = ITEM_VIEW_TYPE_GOODS
        override val layoutId: Int = R.layout.mod_item_jiaoyi_price_pro

        override fun convert(holder: BaseViewHolder, item: Navigation11ListItem) {
            val binding = ModItemJiaoyiPriceProBinding.bind(holder.itemView)
            val goodsItem = item as Navigation11ListItem.GoodsItem

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

class Navigation11DiffCallback : DiffUtil.ItemCallback<Navigation11ListItem>() {
    override fun areItemsTheSame(oldItem: Navigation11ListItem, newItem: Navigation11ListItem): Boolean {
        if (oldItem is Navigation11ListItem.GoodsItem && newItem is Navigation11ListItem.GoodsItem) {
            return oldItem.goodDetail.gameid == newItem.goodDetail.gameid
        }
        return oldItem::class == newItem::class
    }

    override fun areContentsTheSame(oldItem: Navigation11ListItem, newItem: Navigation11ListItem): Boolean {
        return oldItem == newItem
    }
}

sealed class Navigation11ListItem {
    data class HeaderImageItem(val list: List<Int>) : Navigation11ListItem()
    data class TitleItem(val title: String, val showIcon: Boolean, val showMore: Boolean) : Navigation11ListItem()
    data class TopGoodsItem(val goodsList: List<ModTradeGoodDetailBean>) : Navigation11ListItem()
    data class Title2Item(val title: String, val showIcon: Boolean, val showMore: Boolean) : Navigation11ListItem()
    data class GoodsItem(val goodDetail: ModTradeGoodDetailBean) : Navigation11ListItem()
}

private const val ITEM_VIEW_TYPE_HEADER_IMAGE = 0
private const val ITEM_VIEW_TYPE_TITLE = 1
private const val ITEM_VIEW_TYPE_TOP_GOODS = 2
private const val ITEM_VIEW_TYPE_TITLE2 = 3
private const val ITEM_VIEW_TYPE_GOODS = 4

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