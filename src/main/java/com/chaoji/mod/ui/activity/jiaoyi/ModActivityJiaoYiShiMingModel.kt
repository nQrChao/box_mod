package com.chaoji.mod.ui.activity.jiaoyi

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.modRequestWithMsg
import com.chaoji.base.ext.requestFlow
import com.chaoji.base.state.ModResultStateWithMsg
import com.chaoji.im.data.model.ModPay
import com.chaoji.im.data.model.ModUserRealName
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.sdk.eventViewModel
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.hjq.toast.Toaster

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