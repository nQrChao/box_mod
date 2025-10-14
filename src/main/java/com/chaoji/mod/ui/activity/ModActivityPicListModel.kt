package com.chaoji.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.request
import com.chaoji.base.state.ResultState
import com.chaoji.im.data.model.AppletsData
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService

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