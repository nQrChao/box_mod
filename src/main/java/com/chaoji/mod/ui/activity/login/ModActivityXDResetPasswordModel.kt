package com.chaoji.mod.ui.activity.login

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.base.callback.databind.IntObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.modRequestChain
import com.chaoji.base.ext.modRequestWithMsg
import com.chaoji.base.state.ModResultState
import com.chaoji.base.state.ModResultStateWithMsg
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.data.model.ModUserInfoBean

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