package com.chaoji.mod.ui.activity.game

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.base.ext.modRequest
import com.chaoji.base.ext.modRequestWithMsg
import com.chaoji.base.state.ModResultState
import com.chaoji.base.state.ModResultStateWithMsg
import com.chaoji.im.data.model.ModCocosExchange
import com.chaoji.im.data.model.ModGameListInfo
import com.chaoji.im.data.model.ModGameListInfoDialog
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.other.immersionbar.BarHide

class ModActivityGameBrowserModel : BaseViewModel(barHid = BarHide.FLAG_HIDE_BAR) {
    var showGame= BooleanObservableField()
    var hideLoading = BooleanObservableField()

    var postInfoAppApi325Result = MutableLiveData<ModResultState<ModGameListInfo>>()
    var postInfoAppApi326Result = MutableLiveData<ModResultState<ModGameListInfo>>()
    var postInfoAppApi327Result = MutableLiveData<ModResultState<ModGameListInfoDialog>>()
    var postCocosExchangeResultWithMsg = MutableLiveData<ModResultStateWithMsg<ModCocosExchange>>()
    fun postInfoAppApi325() {
        modRequest({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = "325"
            apiService.postGameInfoAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postInfoAppApi325Result)
    }

    fun postInfoAppApi326() {
        modRequest({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = "326"
            apiService.postGameInfoAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postInfoAppApi326Result)
    }

    fun postInfoAppApi327() {
        modRequest({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = "327"
            apiService.postGameInfoDialogAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postInfoAppApi327Result)
    }

    fun postCocosExchangeWithMsg(type:String,uid:String?,token:String?) {
        var cocosExchangeType = ""
        when (type) {
            "1" -> cocosExchangeType = "lsdnx_1"
            "2" -> cocosExchangeType = "lsdnx_2"
            "3" -> cocosExchangeType = "lsdnx_3"
        }

        modRequestWithMsg({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_cocos_exchangeflb"
            map["cocos_exchange"] = cocosExchangeType
            map["uid"] = uid?:""
            map["token"] = token?:""
            map["client_type"] = "1"
            apiService.postCocosExchangeApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postCocosExchangeResultWithMsg)
    }


}