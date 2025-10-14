package com.box.mod.ui.activity.modmain.mod5

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.ext.request
import com.box.base.state.ResultState
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.sdk.ImSDK.Companion.eventViewModelInstance
import com.box.common.data.model.AppletsData

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