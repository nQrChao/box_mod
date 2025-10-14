package com.box.mod.ui.fragment

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequest
import com.box.base.state.ModResultState
import com.box.common.data.model.AppletsLunTan
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.sdk.ImSDK

class MainFragment3Model : BaseViewModel(title = "攻略") {
    var sendKey = StringObservableField()
    var postDataAppApiByDataIdResult = MutableLiveData<ModResultState<AppletsLunTan>>()
    fun updateConversation() {
        ImSDK.eventViewModelInstance.messageLoadingState.value = true
    }

    fun postDataAppApiByDataId(dataId: String) {
        modRequest({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = dataId
            apiService.postInfoLunTanAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApiByDataIdResult)
    }
}
