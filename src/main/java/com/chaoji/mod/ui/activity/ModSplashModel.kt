package com.chaoji.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.ext.request
import com.chaoji.base.state.ResultState
import com.chaoji.im.data.model.AppletsData
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.other.immersionbar.BarHide

class ModSplashModel : BaseViewModel(barHid = BarHide.FLAG_HIDE_BAR, isStatusBarEnabled = true) {

    var marketInitResult = MutableLiveData<ResultState<AppletsData>>()

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