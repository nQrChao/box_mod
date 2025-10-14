package com.box.mod.ui.activity.login

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.state.ModResultStateWithMsg
import com.box.common.network.NetworkApi
import com.box.common.network.apiService

class ModActivityXDResetPasswordModel : BaseViewModel(title = "修改密码") {
    var mobileNum = StringObservableField("")
    var password = StringObservableField("")
    var verificationCode = StringObservableField("")

    var postResetPasswordResult = MutableLiveData<ModResultStateWithMsg<String>>()
    var postGetResetPasswordVerificationCodeResult = MutableLiveData<ModResultStateWithMsg<String>>()
    fun postGetResetPasswordVerificationCode() {
        modRequestWithMsg({
            val map = mutableMapOf<String, String>()
            map["api"] = "get_code"
            map["mobile"] = mobileNum.get()
            map["is_check"] ="2"
            apiService.postGetVerificationCode(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postGetResetPasswordVerificationCodeResult)
    }

    fun postResetPassword() {
        modRequestWithMsg({
            val map = mutableMapOf<String, String>()
            map["api"] = "get_pwd"
            map["mobile"] = mobileNum.get()
            map["code"] = verificationCode.get()
            map["password"] = password.get()
            apiService.postGetVerificationCode(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postResetPasswordResult)
    }


}