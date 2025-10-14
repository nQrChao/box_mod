package com.chaoji.mod.ui.activity.jiaoyi

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.IntObservableField
import com.chaoji.base.ext.modRequestWithMsg
import com.chaoji.base.state.ModResultStateWithMsg
import com.chaoji.im.data.model.ModPay
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs

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