package com.box.mod.ui.activity.login

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
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

class ModActivityXDLoginModel : BaseViewModel(rightTitle = "没有账号？立即注册 >") {
    var loginType = IntObservableField(1)
    var hasOneKeyLogin = BooleanObservableField(false)

    var mobileNum = StringObservableField("")
    var password = StringObservableField("")
    var verificationCode = StringObservableField("")


    val loginBeanResult = MutableLiveData<ModResultStateWithMsg<ModLoginBean>>()
    val userInfoBeanResult = MutableLiveData<ModResultStateWithMsg<ModUserInfoBean>>()
    val modUserRealName= MutableLiveData<ModResultStateWithMsg<ModUserRealName>>()

    var loginBean = MutableLiveData<ModLoginBean>()
    var postGetVerificationCodeResult = MutableLiveData<ModResultStateWithMsg<String>>()

    fun postGetVerificationCode() {
        modRequestWithMsg({
            val map = mutableMapOf<String, String>()
            map["api"] = "get_code"
            map["mobile"] = mobileNum.get()
            map["is_check"] = "3"
            apiService.postGetVerificationCode(NetworkApi.INSTANCE.createModPostData(map)!!)
        }, postGetVerificationCodeResult)
    }

    fun postNameLoginAndGetUserInfo() {
        requestFlow {
            val login = step(
                block = {
                    val map = mutableMapOf<String, String>()
                    map["api"] = "login"
                    map["username"] = mobileNum.get()
                    map["password"] = password.get()
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

    fun postPhoneLoginAndGetUserInfo() {
        requestFlow {
            val login = step(
                block = {
                    val map = mutableMapOf<String, String>()
                    map["api"] = "mobile_auto_login"
                    map["mobile"] = mobileNum.get()
                    map["code"] = verificationCode.get()
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