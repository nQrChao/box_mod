package com.box.mod.ui.activity.modmain.mod2

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.ext.request
import com.box.base.state.ResultState
import com.box.common.data.model.AppletsData
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.sdk.ImSDK.Companion.eventViewModelInstance

class Mod2Fragment2Model : BaseViewModel(leftTitle = "热门壁纸") {
    var isSelect = IntObservableField(0)

    var postDataAppApi128Result = MutableLiveData<ResultState<AppletsData>>()
    var postDataAppApiByGameIdResult = MutableLiveData<ResultState<AppletsData>>()
    fun updateConversation() {
        eventViewModelInstance.messageLoadingState.value = true
    }
    fun postDataAppApi128() {
        request({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = "128"
            apiService.postDataAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApi128Result)
    }
    fun postDataAppApiByGameIdResul(gameId: String) {
        request({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = gameId
            apiService.postDataAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApiByGameIdResult)
    }

}