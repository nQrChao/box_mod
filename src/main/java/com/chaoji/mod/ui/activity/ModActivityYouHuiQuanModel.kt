package com.chaoji.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.base.callback.databind.IntObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.modRequest
import com.chaoji.base.ext.request
import com.chaoji.base.ext.requestFlow
import com.chaoji.base.state.ModResultStateWithMsg
import com.chaoji.base.state.ResultState
import com.chaoji.im.appContext
import com.chaoji.im.data.model.AppletsData
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.im.data.model.ModMeYouHuiQuanBean
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.mod.ui.activity.login.ModActivityXDLogin
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs

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