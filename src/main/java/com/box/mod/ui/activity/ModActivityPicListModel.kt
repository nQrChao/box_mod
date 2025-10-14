package com.box.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.request
import com.box.base.state.ResultState
import com.box.common.data.model.AppletsData
import com.box.common.data.model.AppletsInfo
import com.box.common.network.NetworkApi
import com.box.common.network.apiService

class ModActivityPicListModel : BaseViewModel(title = "　　　　　") {
    var searchKey = StringObservableField()
    var appletsInfo= MutableLiveData<AppletsInfo>()
    var postDataAppApiByGameIdResult = MutableLiveData<ResultState<AppletsData>>()


    fun postDataAppApiByGameIdResul(gameId: String) {
        request({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = gameId
            apiService.postDataAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApiByGameIdResult)
    }
}