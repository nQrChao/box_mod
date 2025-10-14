package com.chaoji.mod.ui.fragment.fragment1

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.ext.modRequest
import com.chaoji.base.state.ModResultState
import com.chaoji.im.data.model.AppletsLeYuan
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.sdk.ImSDK.Companion.eventViewModelInstance

class Navigation13Model : BaseViewModel() {
    var postDataAppApiByDataIdResult = MutableLiveData<ModResultState<AppletsLeYuan>>()
    fun updateConversation() {
        eventViewModelInstance.messageLoadingState.value = true
    }
    fun postDataAppApiByDataId(dataId: String) {
        modRequest({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = dataId
            apiService.postInfoLeYuanAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApiByDataIdResult)
    }

}