package com.chaoji.mod.ui.fragment

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.modRequest
import com.chaoji.base.state.ModResultState
import com.chaoji.im.data.model.AppletsLunTan
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.sdk.ImSDK

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
