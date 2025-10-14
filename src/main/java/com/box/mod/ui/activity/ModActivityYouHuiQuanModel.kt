package com.box.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.requestFlow
import com.box.base.state.ModResultStateWithMsg
import com.box.common.appContext
import com.box.common.data.model.ModMeYouHuiQuanBean
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.utils.MMKVUtil
import com.box.mod.ui.activity.login.ModActivityXDLogin
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs

class ModActivityYouHuiQuanModel : BaseViewModel(title = "我的优惠券") {
    var isSelect = IntObservableField(0)
    var hasData = BooleanObservableField(false)
    var searchKey = StringObservableField()
    var appletsInfo = MutableLiveData<MutableList<ModMeYouHuiQuanBean>>()
    var postDataAppApiByGameIdResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModMeYouHuiQuanBean>>>()

    fun postYouHuiQuan() {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            requestFlow {
                val youhuiquan = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "coupon_record"
                        map["list_type"] = "game"
                        map["need_gameinfo"] = "1"
                        map["uid"] = user.uid
                        map["token"] = user.token
                        apiService.postMeYouHuiQuanAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
                    },
                    resultState = postDataAppApiByGameIdResult,
                )
            }
        }else {
            ModActivityXDLogin.start(appContext)
        }
    }
}