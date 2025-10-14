package com.box.mod.ui.fragment

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.request
import com.box.base.state.ResultState
import com.box.common.data.model.UserUseCountResult
import com.box.common.network.apiService

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