package com.box.mod.ui.activity.jiaoyi

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.state.ModResultStateWithMsg
import com.box.common.data.model.ModPay
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.data.model.ModTradeGoodDetailBean
import com.box.common.utils.MMKVUtil
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs

class ModActivityGouMaiModel : BaseViewModel(title = "确认购买") {
    var payType = IntObservableField(2)

    var tradeGoodDetail = MutableLiveData<ModTradeGoodDetailBean>()

    var payResult = MutableLiveData<ModResultStateWithMsg<ModPay>>()


    fun postPayGetOrder(amount: String) {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            modRequestWithMsg({
                val map = mutableMapOf<String, String>()
                map["api"] = "pay_gold"
                map["amount"] = amount
                map["paytype"] = payType.get().toString()
                map["uid"] = user.uid
                map["token"] = user.token
                apiService.postModPay(NetworkApi.INSTANCE.createPostData(map)!!)
            }, payResult)
        }
    }



}