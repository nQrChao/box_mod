package com.box.mod.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.network.NetState
import com.box.common.STORAGEPermission
import com.box.common.glide.GlideApp
import com.box.common.utils.MMKVUtil
import com.box.com.R as RC
import com.box.mod.R
import com.box.mod.databinding.ModActivityPicDownBinding
import com.box.mod.view.xpop.ModXPopupCenterPermissions
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.ImageUtils
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import com.hjq.permissions.XXPermissions

class ModActivityPicDown : BaseVmDbActivity<ModActivityPicDownModel, ModActivityPicDownBinding>() {
    var picUrl: String = ""
    override fun layoutId(): Int = R.layout.mod_activity_pic_down

    companion object {
        const val INTENT_KEY_IN_URL: String = "url"
        fun start(context: Context, url: String) {
            if (TextUtils.isEmpty(url)) {
                return
            }
            val intent = Intent(context, ModActivityPicDown::class.java)
            intent.putExtra(INTENT_KEY_IN_URL, url)
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
        picUrl = intent.getStringExtra(INTENT_KEY_IN_URL) ?: ""
        GlideApp.with(this@ModActivityPicDown)
            .load(picUrl)
            .transform(RoundedCorners(15))
            .error(RC.drawable.status_error_ic)
            .into(mDataBinding.appletPic)

        mDataBinding.appletPic.setOnLongClickListener {
            showSaveImageDialog()
            true
        }
    }

    override fun createObserver() {

    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {
        fun savePic() {
            showSaveImageDialog()
        }
    }

    private fun showSaveImageDialog() {
        AlertDialog.Builder(this@ModActivityPicDown)
            .setTitle("保存图片")
            .setMessage("是否要保存此图片到本地？")
            .setPositiveButton(
                "保存"
            ) { dialog: DialogInterface?, which: Int -> savePic() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun savePic() {
        if(StringUtils.isEmpty(MMKVUtil.getSTORAGE())){
            XPopup.Builder(this)
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .isLightStatusBar(true)
                .animationDuration(5)
                .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                .hasNavigationBar(true)
                .asCustom(
                    ModXPopupCenterPermissions(this, "存储", "用于实现图片存储功能", {
                        XXPermissions.with(this@ModActivityPicDown).permission(STORAGEPermission).request { _, all ->
                            if (all) {
                                MMKVUtil.saveSTORAGE("saveSTORAGE")
                                if (!StringUtils.isEmpty(picUrl)) {
                                    ImageUtils.save2Album(
                                        mDataBinding.appletPic.drawable.toBitmap(),
                                        Bitmap.CompressFormat.PNG,
                                        true
                                    )
                                    Toaster.show("保存成功")
                                }
                            } else {
                                Toaster.show("授权失败，请手动授权")
                            }
                        }
                    }) {

                    })
                .show()
        }else{
            ImageUtils.save2Album(
                mDataBinding.appletPic.drawable.toBitmap(),
                Bitmap.CompressFormat.PNG,
                true
            )
            Toaster.show("保存成功")
        }

    }

}