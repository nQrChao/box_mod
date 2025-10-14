package com.box.mod.ui.fragment.fragment1

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.ext.modRequest
import com.box.base.state.ModResultState
import com.box.common.data.model.AppletsLeYuan
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.sdk.ImSDK.Companion.eventViewModelInstance

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