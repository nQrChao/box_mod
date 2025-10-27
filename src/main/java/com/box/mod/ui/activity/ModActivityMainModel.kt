package com.box.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.ext.requestFlow
import com.box.base.state.ModResultStateWithMsg
import com.box.base.state.ResultState
import com.box.common.MMKVConfig
import com.box.common.appViewModel
import com.box.common.data.model.ModUserInfo
import com.box.common.data.model.ModUserRealName
import com.box.common.eventViewModel
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs

class ModActivityMainModel : BaseViewModel(titleLine = false) {
    var loginOutResult = MutableLiveData<Boolean>()
    var registerResult = MutableLiveData<ResultState<Any?>>()

    var postModAuthLoginResult = MutableLiveData<ModResultStateWithMsg<ModUserInfo>>()
    val userInfoBeanResult = MutableLiveData<ModResultStateWithMsg<ModUserInfo>>()
    val modUserRealName = MutableLiveData<ModResultStateWithMsg<ModUserRealName>>()

    fun modAuthLogin() {
        val user = MMKVConfig.userInfo
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            requestFlow {
                val login = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "auto_login"
                        map["uid"] = user.userId
                        map["auth"] = user.userAuthLoginToken
                        apiService.postAuthLogin("auto_login", NetworkApi.INSTANCE.createVirtualUserPostData(map)!!)
                    },
                    resultState = postModAuthLoginResult,
                )

                val modUser = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "get_userinfo"
                        map["get_super_user"] = "y"
                        map["uid"] = login.userId
                        map["token"] = login.userToken
                        apiService.postUserInfo(NetworkApi.INSTANCE.createVirtualUserPostData(map)!!)
                    },
                    resultState = userInfoBeanResult
                )

                modUser.userToken = login.userToken
                MMKVConfig.userInfo = modUser

                appViewModel.modUserInfo.postValue(modUser)
                eventViewModel.isLogin.value = true
                val realName = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "market_tradeusercert"
                        map["uid"] = login.userId
                        map["token"] = login.userToken
                        apiService.postModUserRealName(NetworkApi.INSTANCE.createVirtualUserPostData(map)!!)
                    },
                    resultState = modUserRealName
                )

            }

        }

    }


    fun loginOut() {

    }



}