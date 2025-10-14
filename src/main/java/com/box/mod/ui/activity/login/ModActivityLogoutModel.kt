package com.box.mod.ui.activity.login

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.state.ModResultStateWithMsg
import com.box.common.data.model.AppletsInfo
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.utils.MMKVUtil
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs

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