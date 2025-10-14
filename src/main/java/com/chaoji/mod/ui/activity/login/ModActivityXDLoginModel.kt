package com.chaoji.mod.ui.activity.login

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.base.callback.databind.IntObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.modRequestChain
import com.chaoji.base.ext.modRequestWithMsg
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
import com.chaoji.other.blankj.utilcode.util.Logs

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