package com.box.mod.ui.activity.modmain.mod5

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.ext.request
import com.box.base.state.ResultState
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.data.model.AppletsData

class Mod5Fragment4Model: BaseViewModel(title = "") {
    var postDataAppApi268Result = MutableLiveData<ResultState<AppletsData>>()

    fun postDataAppApi268() {
        request({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = "268"
            apiService.postDataAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApi268Result)
    }
}