package com.box.mod.ui.activity.jiaoyi

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.state.ModResultStateWithMsg
import com.box.common.data.model.ModPay
import com.box.common.data.model.ModUserRealName
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.utils.MMKVUtil
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs

class ModActivityJiaoYiShiMingModel : BaseViewModel(title = "实名认证") {
    var searchKey = StringObservableField()
    var rName = StringObservableField()
    var cardId = StringObservableField()

    var shiMingResult = MutableLiveData<ModResultStateWithMsg<ModPay>>()
    val modUserRealName = MutableLiveData<ModResultStateWithMsg<ModUserRealName>>()
    fun postShiMing() {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            modRequestWithMsg({
                val map = mutableMapOf<String, String>()
                map["api"] = "cert_add_v2"
                map["real_name"] = rName.get()
                map["idcard"] = cardId.get()
                map["type"] = "1"
                map["is_check"] = "1"
                map["uid"] = user.uid
                map["token"] = user.token
                apiService.postModShiMing(NetworkApi.INSTANCE.createModPostData(map)!!)
            }, shiMingResult)
        }
    }

    fun postRealName() {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            modRequestWithMsg({
                val map = mutableMapOf<String, String>()
                map["api"] = "market_tradeusercert"
                map["uid"] = user.uid
                map["token"] = user.token
                apiService.postModUserRealName(NetworkApi.INSTANCE.createModPostData(map)!!)
            }, modUserRealName)
        }
    }



}