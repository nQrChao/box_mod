package com.chaoji.mod.ui.activity.modmain.mod2

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.request
import com.chaoji.base.state.ResultState
import com.chaoji.im.data.model.UserUseCountResult
import com.chaoji.im.network.apiService

class Mod2Fragment4Model: BaseViewModel(title = "个人中心") {
    var id = StringObservableField("")
    var userUseCountResult =  MutableLiveData<ResultState<UserUseCountResult>>()
    val tuiSong = MutableLiveData<Boolean>()
    fun loginOut() {

    }

    fun getCount() {
        request({
            apiService.getUserUseCount()
        },userUseCountResult)
    }

    fun delChatMessage() {

    }
}