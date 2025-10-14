package com.box.mod.ui.xpop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.box.other.blankj.utilcode.util.FileUtils
import com.box.other.blankj.utilcode.util.IntentUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.blankj.utilcode.util.PathUtils
import com.box.base.base.action.ClickAction
import com.box.base.base.action.KeyboardAction
import com.box.common.data.model.UpdateList
import com.box.mod.R
import com.box.other.hjq.toast.Toaster
import com.box.other.kongzue.baseokhttp.HttpRequest
import com.box.other.kongzue.baseokhttp.listener.OnDownloadListener
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.box.other.xpopup.core.CenterPopupView
import java.io.File

@SuppressLint("ViewConstructor")
class ModXPopupCenterUpdate(context: Context, var update: UpdateList, private var cancel: (() -> Unit)?, private var sure: (() -> Unit)?) :
    CenterPopupView(context), ClickAction, KeyboardAction {
    override fun getImplLayoutId(): Int = R.layout.mod_xpopup_update

    private var progressText: TextView? = null
    private var titleView: TextView? = null
    private var cancelView: TextView? = null
    private var contentTextView: TextView? = null
    private var versionView: TextView? = null
    private var confirmView: TextView? = null
    private var progressBar: ProgressBar? = null
    private var downLL: LinearLayout? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        titleView = findViewById<TextView>(R.id.tv_title)
        cancelView = findViewById<TextView>(R.id.tv_cancel)
        versionView = findViewById<TextView>(R.id.tv_version)
        contentTextView = findViewById<TextView>(R.id.tv_content)
        confirmView = findViewById<TextView>(R.id.tv_confirm)
        progressText = findViewById<TextView>(R.id.progress_text)
        progressBar = findViewById<ProgressBar>(R.id.progress)
        downLL = findViewById<LinearLayout>(R.id.down_ll)
        versionView?.text = "V " + update.android?.version

        val builder: StringBuilder = StringBuilder()
        for (i in update.android?.versionContent!!) {
            builder.append(i).append("\n")
        }
        if (update.android?.force == true) {
            cancelView?.visibility = View.INVISIBLE
        }else{
            cancelView?.visibility = View.VISIBLE
        }
        contentTextView?.text = builder
        setOnClickListener(R.id.tv_cancel, R.id.tv_confirm)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_confirm -> {
                sure?.invoke()
                //dismiss()
                XXPermissions.with(context).permission(Permission.REQUEST_INSTALL_PACKAGES).request(object : OnPermissionCallback {
                    override fun onGranted(permissions: List<String>, all: Boolean) {
                        cancelView?.visibility = View.INVISIBLE
                        confirmView?.visibility = View.GONE
                        downLL?.visibility = View.VISIBLE

                        HttpRequest.DOWNLOAD(
                            context,
                            update.android?.fileUrl,
                            FileUtils.getFileByPath(PathUtils.getInternalAppFilesPath() + File.separator + "im.apk"),
                            object : OnDownloadListener {
                                override fun onDownloadSuccess(file: File) {
                                    progressText?.text = "下载完成"
                                    Logs.e("update.android?.fileUrl:" + update.android?.fileUrl)
                                    installApk(file)
                                    dismiss()
                                }

                                @SuppressLint("SetTextI18n")
                                override fun onDownloading(progress: Int) {
                                    progressText?.text = "正在下载 $progress%"
                                    progressBar?.progress = progress
                                }

                                override fun onDownloadFailed(e: Exception) {
                                    Logs.e("下载失败", e.printStackTrace())
                                }
                            }
                        )
                    }

                    override fun onDenied(permissions: List<String>, never: Boolean) {
                        if (never) {
                            Toaster.show("被永久拒绝授权，请手动授予权限")
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(context, permissions)
                        } else {
                            Toaster.show("获取权限失败，请手动授予权限")
                        }
                    }
                })

            }

            R.id.tv_cancel -> {
                cancel?.invoke()
                dismiss()
            }
        }
    }


    /**
     * 安装 Apk
     */
    private fun installApk(file: File) {
        XXPermissions.with(context).permission(Permission.REQUEST_INSTALL_PACKAGES).request(object : OnPermissionCallback {
            override fun onGranted(permissions: List<String>, all: Boolean) {
                context.startActivity(IntentUtils.getInstallAppIntent(file))
            }

            override fun onDenied(permissions: List<String>, never: Boolean) {
                if (never) {
                    Toaster.show("被永久拒绝授权，请手动授予权限")
                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                    XXPermissions.startPermissionActivity(context, permissions)
                } else {
                    Toaster.show("获取权限失败，请手动授予权限")
                }
            }
        })
    }

    override fun dismiss() {
        super.dismiss()
        hideKeyboard(this)
    }


}