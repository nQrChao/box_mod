package com.chaoji.mod.ui.activity.image

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chaoji.im.data.PictureElem
import com.chaoji.im.loadPicture
import com.chaoji.mod.R
import com.chaoji.mod.databinding.ItemImageVideoPreviewBinding
import com.chaoji.im.data.model.ModTradeGoodDetailBean

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