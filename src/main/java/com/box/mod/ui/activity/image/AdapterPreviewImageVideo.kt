package com.box.mod.ui.activity.image

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.box.common.data.PictureElem
import com.box.common.loadPicture
import com.box.mod.R
import com.box.mod.databinding.ItemImageVideoPreviewBinding
import com.box.common.data.model.ModTradeGoodDetailBean

class AdapterPreviewImageVideo constructor(list: MutableList<Any>) : BaseQuickAdapter<Any, BaseDataBindingHolder<ItemImageVideoPreviewBinding>>(
    R.layout.item_image_video_preview, list
) {
    override fun convert(holder: BaseDataBindingHolder<ItemImageVideoPreviewBinding>, item: Any) {
        when (item) {
            is String -> {
                holder.dataBinding?.ivPhotoView?.visibility = View.VISIBLE
                loadPicture(item).into(holder.dataBinding?.ivPhotoView!!)
                holder.dataBinding?.ivPhotoView?.minimumScale = 1f
                holder.dataBinding?.ivPhotoView?.scale = 1f
            }

            is ModTradeGoodDetailBean.PicList -> {
                holder.dataBinding?.ivPhotoView?.visibility = View.VISIBLE
                loadPicture(item).into(holder.dataBinding?.ivPhotoView!!)
                holder.dataBinding?.ivPhotoView?.minimumScale = 1f
                holder.dataBinding?.ivPhotoView?.scale = 1f
            }

            is PictureElem -> {
                holder.dataBinding?.ivPhotoView?.visibility = View.VISIBLE
                loadPicture(item).into(holder.dataBinding?.ivPhotoView!!)
                holder.dataBinding?.ivPhotoView?.minimumScale = 1f
                holder.dataBinding?.ivPhotoView?.scale = 1f
            }


        }


    }


}