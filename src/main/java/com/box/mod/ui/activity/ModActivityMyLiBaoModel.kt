package com.box.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.requestFlow
import com.box.base.state.ModResultStateWithMsg
import com.box.common.data.model.ModMyLiBaoBean
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.utils.MMKVUtil
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs

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