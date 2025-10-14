package com.chaoji.mod.ui.activity.modmain.mod5

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.ext.request
import com.chaoji.base.state.ResultState
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.sdk.ImSDK.Companion.eventViewModelInstance
import com.chaoji.im.data.model.AppletsData

class Mod5Fragment2Model : BaseViewModel() {
    var postDataAppApiByGameIdResult = MutableLiveData<ResultState<AppletsData>>()
    fun updateConversation() {
        eventViewModelInstance.messageLoadingState.value = true
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