package com.chaoji.mod.ui.activity.login

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.base.callback.databind.IntObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.modRequestWithMsg
import com.chaoji.base.state.ModResultStateWithMsg
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs

class ModActivityLogoutModel : BaseViewModel(title = "账号注销") {
    var zhuxiaoShowView = IntObservableField(0)
    var zhuxiaoText = StringObservableField()
    var agreementChecked = BooleanObservableField(false)
    var mobileNum = StringObservableField("")
    var password = StringObservableField("")

    var postLogoutResultWithMsg = MutableLiveData<ModResultStateWithMsg<AppletsInfo>>()
    var postLogoutCheckResultWithMsg = MutableLiveData<ModResultStateWithMsg<AppletsInfo>>()


    fun logoutCheckRequest() {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            modRequestWithMsg({
                val map = mutableMapOf<String, String>()
                map["api"] = "user_cancel_check"
                map["uid"] = user.uid
                map["token"] = user.token
                apiService.postInfoAppApi(NetworkApi.INSTANCE.createModPostData(map)!!)
            }, postLogoutCheckResultWithMsg)
        }
    }


    fun logoutRequest() {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            modRequestWithMsg({
                val map = mutableMapOf<String, String>()
                map["api"] = "user_cancel"
                map["password"] = password.get()
                map["uid"] = user.uid
                map["token"] = user.token
                apiService.postInfoAppApi(NetworkApi.INSTANCE.createModPostData(map)!!)
            }, postLogoutResultWithMsg)
        }
    }



}