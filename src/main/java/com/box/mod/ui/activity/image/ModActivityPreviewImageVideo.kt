package com.box.mod.ui.activity.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.box.base.base.activity.BaseModVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.network.NetState
import com.box.common.INTENT_KEY_INT
import com.box.common.INTENT_KEY_OUT_IMAGE_ARRAYLIST
import com.box.common.INTENT_KEY_STRING
import com.box.common.MMKVConfig
import com.box.common.STORAGEPermission
import com.box.common.data.PictureElem
import com.box.common.glide.EasyGlideEngine
import com.box.mod.R
import com.box.com.R as RC
import com.box.mod.databinding.ModActivityPreviewImageVideoBinding
import com.box.mod.view.xpop.ModXPopupCenterPermissions
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.ImageUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.chrisbanes.photoview.PhotoView
import com.box.other.hjq.toast.Toaster
import com.box.other.huantansheng.easyphotos.ui.PreviewFragment
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.hjq.permissions.XXPermissions


class ModActivityPreviewImageVideo : BaseModVmDbActivity<ModActivityPreviewImageVideo.Model, ModActivityPreviewImageVideoBinding>(), View.OnClickListener,
    PreviewFragment.OnPreviewFragmentClickListener {
    override fun layoutId(): Int = R.layout.mod_activity_preview_image_video

    var index = 0
    var lastPosition = 0 //记录recyclerView最后一次角标位置，用于判断是否转换了item
    val snapHelper: PagerSnapHelper = PagerSnapHelper()
    var lm: LinearLayoutManager = LinearLayoutManager(this@ModActivityPreviewImageVideo, RecyclerView.HORIZONTAL, false)
    var previewFragment: PreviewFragment? = null
    var dataList = ArrayList<Any>()
    private var previewPhotosAdapter: AdapterPreviewImageVideo = AdapterPreviewImageVideo(dataList)

    companion object {
        fun start(context: Context, picture: String) {
            val intent = Intent(context, ModActivityPreviewImageVideo::class.java)
            intent.putExtra(INTENT_KEY_STRING, picture)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }

        fun start(context: Context, photos: ArrayList<Any>, index: Int) {
            val intent = Intent(context, ModActivityPreviewImageVideo::class.java)
            intent.putExtra(INTENT_KEY_OUT_IMAGE_ARRAYLIST, photos)
            intent.putExtra(INTENT_KEY_INT, index)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }


    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.viewmodel = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            statusBarColor(RC.color.easy_photos_bar_primary)
            navigationBarColor(RC.color.easy_photos_bar_primary)
            statusBarDarkFont(false)
            init()
        }
        EasyGlideEngine.getInstance()
        if (intent.getSerializableExtra(INTENT_KEY_OUT_IMAGE_ARRAYLIST) != null) {
            dataList = intent.getSerializableExtra(INTENT_KEY_OUT_IMAGE_ARRAYLIST) as ArrayList<Any>
            index = intent.getIntExtra(INTENT_KEY_INT, 0)
            initAdapter()
        }

        if (intent.getStringExtra(INTENT_KEY_STRING) != null) {
            val picture = GsonUtils.fromJson(intent.getStringExtra(INTENT_KEY_STRING), PictureElem::class.java)
            dataList.add(picture)
            initAdapter()
        }
    }

    override fun createObserver() {

    }

    private fun initAdapter() {
        lastPosition = index
        previewPhotosAdapter.setList(dataList)
        mDataBinding.rvPhotos.run {
            layoutManager = lm
            adapter = previewPhotosAdapter
            isNestedScrollingEnabled = false

            scrollToPosition(index)
        }

        snapHelper.attachToRecyclerView(mDataBinding.rvPhotos)
        mDataBinding.rvPhotos.addOnScrollListener(object : OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val view: View = snapHelper.findSnapView(lm) ?: return
                val position: Int = lm.getPosition(view)
                if (lastPosition == position) {
                    return
                }
                lastPosition = position
                Logs.e("item.video.lastPosition", lastPosition)
                try {
                    val picView: PhotoView = previewPhotosAdapter.getViewByPosition(position, RC.id.iv_photo_view) as PhotoView
                    picView.scale = 1f
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                previewFragment = supportFragmentManager.findFragmentById(R.id.fragment_preview) as PreviewFragment?
                mDataBinding.tvNumber.text = getString(RC.string.preview_current_number_easy_photos, lastPosition + 1, dataList.size)


            }
        })
        mDataBinding.tvNumber.text = getString(
            RC.string.preview_current_number_easy_photos, index + 1,
            dataList.size
        )
        previewPhotosAdapter.addChildClickViewIds(RC.id.iv_play)
        previewPhotosAdapter.addChildClickViewIds(RC.id.iv_photo_view)
        previewPhotosAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == RC.id.iv_play) {
                //ActivityUtils.finishActivity(this@PreviewEasyPhotoActivity)
            }
        }
        previewPhotosAdapter.setOnItemClickListener { adapter, view, position ->

        }
    }

    override fun onNetworkStateChanged(it: NetState) {
    }

    override fun onClick(v: View?) {
    }

    override fun onPreviewPhotoClick(position: Int) {
    }

    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun finishActivity() {
            finish()
        }

        fun savePhoto() {
            if (MMKVConfig.EXTERNAL_STORAGE) {
                XPopup.Builder(this@ModActivityPreviewImageVideo)
                    .dismissOnTouchOutside(false)
                    .dismissOnBackPressed(false)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(true)
                    .isLightStatusBar(true)
                    .animationDuration(5)
                    .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                    .hasNavigationBar(true)
                    .asCustom(
                        ModXPopupCenterPermissions(this@ModActivityPreviewImageVideo, "存储", "用于实现图片存储功能", {
                            XXPermissions.with(this@ModActivityPreviewImageVideo).permission(STORAGEPermission).request { _, all ->
                                if (all) {
                                    MMKVConfig.EXTERNAL_STORAGE = true
                                    val item = previewPhotosAdapter.data[lastPosition]
                                    try {
                                        val picView: PhotoView = previewPhotosAdapter.getViewByPosition(lastPosition, RC.id.iv_photo_view) as PhotoView
                                        ImageUtils.save2Album(picView.drawable.toBitmap(), Bitmap.CompressFormat.PNG, true)
                                        Toaster.show("已保存到相册")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    Toaster.show("授权失败，请手动授权")
                                }
                            }
                        }) {

                        })
                    .show()
            } else {
                try {
                    val picView: PhotoView = previewPhotosAdapter.getViewByPosition(lastPosition, RC.id.iv_photo_view) as PhotoView
                    ImageUtils.save2Album(picView.drawable.toBitmap(), Bitmap.CompressFormat.PNG, true)
                    Toaster.show("已保存到相册")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class Model : BaseViewModel() {

    }
}