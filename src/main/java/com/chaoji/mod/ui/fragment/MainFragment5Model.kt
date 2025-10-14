package com.chaoji.mod.ui.fragment

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.request
import com.chaoji.base.state.ResultState
import com.chaoji.im.data.model.UserUseCountResult
import com.chaoji.im.network.apiService

class MainFragment5Model : BaseViewModel() {
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