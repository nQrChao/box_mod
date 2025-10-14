package com.chaoji.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.ext.modRequest
import com.chaoji.base.ext.request
import com.chaoji.base.ext.requestFlow
import com.chaoji.base.state.ModResultState
import com.chaoji.base.state.ModResultStateWithMsg
import com.chaoji.base.state.ResultState
import com.chaoji.im.data.model.ModUserInfoBean
import com.chaoji.im.data.model.ModUserRealName
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.sdk.eventViewModel
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class ModActivityMainModel : BaseViewModel(titleLine = false) {
    var loginOutResult = MutableLiveData<Boolean>()
    var registerResult = MutableLiveData<ResultState<Any?>>()

    var postModAuthLoginResult = MutableLiveData<ModResultStateWithMsg<ModUserInfoBean>>()
    val userInfoBeanResult = MutableLiveData<ModResultStateWithMsg<ModUserInfoBean>>()
    val modUserRealName = MutableLiveData<ModResultStateWithMsg<ModUserRealName>>()

    fun modAuthLogin() {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            requestFlow {
                val login = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "auto_login"
                        map["uid"] = user.uid
                        map["auth"] = user.auth
                        apiService.postAuthLogin("auto_login", NetworkApi.INSTANCE.createModPostData(map)!!)
                    },
                    resultState = postModAuthLoginResult,
                )

                val modUser = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "get_userinfo"
                        map["get_super_user"] = "y"
                        map["uid"] = login.uid
                        map["token"] = login.token
                        apiService.postUserInfo(NetworkApi.INSTANCE.createModPostData(map)!!)
                    },
                    resultState = userInfoBeanResult
                )

                modUser.token = login.token
                MMKVUtil.saveModUser(modUser)
                appViewModel.modUserInfo.postValue(modUser)
                eventViewModel.isLogin.value = true
                val realName = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "market_tradeusercert"
                        map["uid"] = login.uid
                        map["token"] = login.token
                        apiService.postModUserRealName(NetworkApi.INSTANCE.createModPostData(map)!!)
                    },
                    resultState = modUserRealName
                )

            }

        }

    }


    fun loginOut() {

    }


    fun marketInit() {
        request({
            val map = mutableMapOf<String, Any>()
            map["device"] = 21
            val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), Gson().toJson(map))
            apiService.register(body)
        }, registerResult)
    }

}