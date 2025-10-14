package com.chaoji.mod.ui.activity.login

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.modRequestChain
import com.chaoji.base.ext.requestFlow
import com.chaoji.base.state.ModResultState
import com.chaoji.base.state.ModResultStateWithMsg
import com.chaoji.im.data.model.ModLoginBean
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.data.model.ModUserInfoBean
import com.chaoji.im.data.model.ModUserRealName
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.sdk.eventViewModel
import com.chaoji.im.utils.MMKVUtil

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