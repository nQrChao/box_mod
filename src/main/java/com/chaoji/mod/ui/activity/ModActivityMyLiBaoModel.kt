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
import com.chaoji.im.data.model.ModMyLiBaoBean
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.mod.ui.activity.login.ModActivityXDLogin
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs

class ModActivityMyLiBaoModel : BaseViewModel(title = "我的礼包") {
    var isSelect = IntObservableField(0)
    var hasData = BooleanObservableField(false)
    var searchKey = StringObservableField()
    var appletsInfo = MutableLiveData<MutableList<ModMyLiBaoBean>>()
    var postDataAppApiByGameIdResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModMyLiBaoBean>>>()

    fun postMyLiBao() {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            requestFlow {
                val youhuiquan = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "get_user_card"
                        map["uid"] = user.uid
                        map["token"] = user.token
                        apiService.postMyLiBaoList(NetworkApi.INSTANCE.createPostData(map)!!)
                    },
                    resultState = postDataAppApiByGameIdResult,
                )
            }
        }

    }
}