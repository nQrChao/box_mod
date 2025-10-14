package com.box.mod.ui.activity.login

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.requestFlow
import com.box.base.state.ModResultStateWithMsg
import com.box.common.data.model.ModLoginBean
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.data.model.ModUserInfoBean
import com.box.common.data.model.ModUserRealName
import com.box.common.sdk.appViewModel
import com.box.common.sdk.eventViewModel
import com.box.common.utils.MMKVUtil

class ModActivityXDRegisterModel : BaseViewModel(title = "账号注册") {
    var name = StringObservableField("")
    var password1 = StringObservableField("")
    var password2 = StringObservableField("")

    var loginBean = MutableLiveData<ModLoginBean>()
    val loginBeanResult = MutableLiveData<ModResultStateWithMsg<ModLoginBean>>()
    val userInfoBeanResult = MutableLiveData<ModResultStateWithMsg<ModUserInfoBean>>()
    val modUserRealName= MutableLiveData<ModResultStateWithMsg<ModUserRealName>>()

    fun postRegisterByUserName() {
        requestFlow {
            val login = step(
                block = {
                    val map = mutableMapOf<String, String>()
                    map["api"] = "account_register"
                    map["username"] = name.get()
                    map["password"] = password1.get()
                    apiService.postLogin(NetworkApi.INSTANCE.createModPostData(map)!!)
                },
                resultState = loginBeanResult,
            )
            val modUser = step(
                block = {
                    val map = mutableMapOf<String, String>()
                    map["api"] = "get_userinfo"
                    map["get_super_user"] = "y"
                    map["uid"] = login.uid.toString()
                    map["token"] = login.token
                    apiService.postUserInfo(NetworkApi.INSTANCE.createModPostData(map)!!)
                },
                resultState = userInfoBeanResult
            )

            modUser.token = login.token
            MMKVUtil.saveModUser(modUser)
            appViewModel.modUserInfo.postValue(modUser)
            eventViewModel.isLogin.postValue(true)

            val realName = step(
                block = {
                    val map = mutableMapOf<String, String>()
                    map["api"] = "market_tradeusercert"
                    map["uid"] = login.uid.toString()
                    map["token"] = login.token
                    apiService.postModUserRealName(NetworkApi.INSTANCE.createModPostData(map)!!)
                },
                resultState = modUserRealName
            )

        }
    }


}