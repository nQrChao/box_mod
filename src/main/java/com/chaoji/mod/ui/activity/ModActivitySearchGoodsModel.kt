package com.chaoji.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.ext.modRequest
import com.chaoji.base.state.ModResultState
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.data.model.ModTradeGoodDetailBean

class ModActivitySearchGoodsModel : BaseViewModel() {
    var tradeAllGoodsListResult = MutableLiveData<ModResultState<MutableList<ModTradeGoodDetailBean>>>()

    fun postAllTradeGoodsList() {
        modRequest({
            val map = mutableMapOf<String, String>()
            map["scene"] = "normal"
            map["goods_type"] = "0"
            apiService.tradeGoodsList(NetworkApi.INSTANCE.createPostData(map)!!)
        }, tradeAllGoodsListResult)
    }

}