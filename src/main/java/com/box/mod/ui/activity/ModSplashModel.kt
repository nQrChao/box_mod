package com.box.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.ext.request
import com.box.base.state.ResultState
import com.box.common.data.model.ProtocolInit
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.other.immersionbar.BarHide

class ModSplashModel : BaseViewModel(barHid = BarHide.FLAG_HIDE_BAR, isStatusBarEnabled = true) {

    var marketInitResult = MutableLiveData<ResultState<ProtocolInit>>()

    fun xyInit() {
        request({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_typeid"] = "2"
            apiService.postDataAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, marketInitResult)
    }

    fun pollingUrls(){

    }

}