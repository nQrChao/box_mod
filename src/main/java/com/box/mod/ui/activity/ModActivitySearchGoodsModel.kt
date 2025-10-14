package com.box.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.ext.modRequest
import com.box.base.state.ModResultState
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.data.model.ModTradeGoodDetailBean

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