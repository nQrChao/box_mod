package com.chaoji.mod.ui.activity.modmain.mod2

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.IntObservableField
import com.chaoji.base.ext.request
import com.chaoji.base.state.ResultState
import com.chaoji.im.data.model.AppletsData
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.sdk.ImSDK.Companion.eventViewModelInstance

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