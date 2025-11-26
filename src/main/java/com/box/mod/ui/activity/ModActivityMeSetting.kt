package com.box.mod.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appContext
import com.box.common.appViewModel
import com.box.common.data.UpdateAccountRequest
import com.box.common.data.model.ModAvatarUriBean
import com.box.common.data.model.ModUserInfo
import com.box.common.data.model.UploadResponseString
import com.box.common.eventViewModel
import com.box.common.network.apiService
import com.box.common.ui.xpop.XPopupCenterCommonEditText
import com.box.common.utils.ext.logsE
import com.box.common.utils.mmkv.MMKVConfig
import com.box.mod.R
import com.box.mod.databinding.ModActivityMeSettingBinding
import com.box.mod.view.xpop.ModXPopupCenterPermissions
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.StringUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.box.com.R as RC

class ModActivityMeSetting :
    BaseVmDbActivity<ModActivityMeSetting.Model, ModActivityMeSettingBinding>() {
    var upAvatarList: MutableList<ModAvatarUriBean> = MutableList(8) {
        ModAvatarUriBean(null)
    }

    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                mViewModel.picUserIconUri.set(uri)
                upAvatarList.add(ModAvatarUriBean(uri))
            } else {
                Toaster.show("未选择任何图片")
            }
        }


    override fun layoutId(): Int = R.layout.mod_activity_me_setting


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ModActivityMeSetting::class.java)
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
            titleBar(mDataBinding.titleBar)
            navigationBarColor(RC.color.white)
            init()
        }

        MMKVConfig.userInfo?.let { savedUser ->
            eventViewModel.isLogin.value = true
            appViewModel.modUserInfo.value = savedUser
        }


    }

    override fun createObserver() {
        appViewModel.modUserInfo.observe(this) {
            mViewModel.modUserInfo.value = it
            mDataBinding.userAccountBar.setRightText(it.user_account)
        }

        mViewModel.uploadFileState.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    logsE("uploadFileState成功，原始数据: ${GsonUtils.toJson(data)}")
                    mViewModel.picUserIconUri.set(null)
                    MMKVConfig.userInfo?.let { user ->
                        user.avatar = data?.imgUrl.toString()
                        MMKVConfig.userInfo = user
                        appViewModel.modUserInfo.value = user
                    }
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }
        mViewModel.updateAccountResult.observe(this) { resultState ->
            parseModStateWithMsg(
                resultState,
                onSuccess = { data, msg ->
                    mViewModel.userAccountName.set("")
                    logsE("updateAccountResult成功，原始数据: ${GsonUtils.toJson(data)}")
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

    }

    override fun onNetworkStateChanged(it: NetState) {

    }

    /**********************************************Click**************************************************/

    inner class ProxyClick {

        fun confirm() {
            if (mViewModel.picUserIconUri.get() != null) {
                mViewModel.uploadCommitFiles(appContext, upAvatarList)
            }
            if (mViewModel.userAccountName.get().isNotEmpty()) {
                mViewModel.postUpdateAccountData(
                    MMKVConfig.userInfo?.password ?: "",
                    mViewModel.userAccountName.get(),
                    MMKVConfig.userInfo?.userName ?: ""
                )
            }

        }

        fun userName() {
            XPopup.Builder(this@ModActivityMeSetting)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .isLightStatusBar(true)
                .hasNavigationBar(true)
                .asCustom(
                    XPopupCenterCommonEditText(
                        this@ModActivityMeSetting,
                        18,
                        "修改昵称",
                        "",
                        "输入新昵称",
                        null
                    ) { it ->
                        if (!StringUtils.isEmpty(it)) {
                            mViewModel.userAccountName.set(it)
                            Toaster.show(mViewModel.userAccountName.get())
                            mDataBinding.userAccountBar.setRightText(it)
                        }
                    })
                .show()
        }

        fun userPwd() {
            ModActivityChangePassword.start(appContext)
        }

        fun userIcon() {
            selectPhoto()
        }

    }

    private fun selectPhoto() {
        if (!MMKVConfig.permissionsAlbum) {
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
                    ModXPopupCenterPermissions(this, "相册", "用于实现图片选择功能", {
                        MMKVConfig.permissionsAlbum = true
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {

                    })
                .show()
        } else {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

    }

    /**********************************************Adapter**************************************************/


    /**********************************************Model**************************************************/
    class Model : BaseViewModel(title = "") {
        var picUserIconUri = ObservableField<Uri>()
        var userAccountName = StringObservableField("")
        var hasData = BooleanObservableField(false)
        var isLogin = BooleanObservableField(false)
        val modUserInfo = MutableLiveData<ModUserInfo>()
        var updateAccountResult = MutableLiveData<ModResultStateWithMsg<Any>>()
        fun postUpdateAccountData(password: String, userAccount: String, userName: String) {
            val requestBody = UpdateAccountRequest(
                password = password,
                userAccount = userAccount,
                userName = userName

            )
            modRequestWithMsg(
                { apiService.postUpdateAccount(requestBody) },
                updateAccountResult,
                isShowDialog = true,
            )
        }


        var uploadFileState = MutableLiveData<ModResultStateWithMsg<UploadResponseString>>()

        /**
         * 执行文件上传操作，并返回上传后的文件URL列表
         * @param context Fragment/Activity 的 Context
         * @param picList 包含待上传文件的 ModImageUriBean 列表
         */
        fun uploadCommitFiles(context: Context, picList: MutableList<ModAvatarUriBean>) {
            // 1. 过滤并准备 MultipartBody.Part 列表（此部分代码与上一步相同）
            val fileParts = picList
                .filter { it.avatarFile != null }
                // 假设 prepareFilePart 是可用的辅助函数
                .mapNotNull {
                    prepareFilePart(context, it.avatarFile!!, "avatarFile")
                }

            if (fileParts.isEmpty()) {
                // 如果没有文件需要上传，直接发送成功状态（空列表）
                uploadFileState.value = ModResultStateWithMsg.onAppSuccess(
                    null,
                    message = "无需上传文件"
                )
                return
            }
            modRequestWithMsg({
                apiService.uploadAvatarFiles(fileParts)
            }, uploadFileState)
        }

        /**
         * 将 Uri 转换为 Retrofit 上传所需的 MultipartBody.Part
         * @param context Context 实例
         * @param uri 文件内容的 Uri
         * @param partName 表单字段名，应与 API 定义的字段名一致（此处为 "files"）
         */
        fun prepareFilePart(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/*"
            val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}")
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            val requestBody = tempFile.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
        }

    }


}