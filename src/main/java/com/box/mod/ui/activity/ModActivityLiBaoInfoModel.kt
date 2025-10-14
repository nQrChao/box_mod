package com.box.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.requestFlow
import com.box.base.state.ModResultStateWithMsg
import com.box.common.data.model.ModLiBaoLingQu
import com.box.common.data.model.ModLiBaoListBean
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.utils.MMKVUtil
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs

class ModActivityLiBaoInfoModel : BaseViewModel(title = "礼包") {
    var isSelect = IntObservableField(0)
    var hasData = BooleanObservableField(false)
    var searchKey = StringObservableField()
    var libaoInfo = MutableLiveData<ModLiBaoListBean>()
    var appletsInfo = MutableLiveData<MutableList<ModLiBaoListBean>>()
    var postDataAppApiByGameIdResult = MutableLiveData<ModResultStateWithMsg<ModLiBaoListBean>>()
    var postLiBaoLingQuResult = MutableLiveData<ModResultStateWithMsg<ModLiBaoLingQu>>()

    fun postLiBaoListBean(gameid: String) {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        val map = mutableMapOf<String, String>()
        map["api"] = "get_game_card"
        map["gameid"] = gameid
        map["need_gameinfo"] = "1"
        if (user != null) {
            map["uid"] = user.uid
            map["token"] = user.token
        }
        requestFlow {
            val libaoInfo = step(
                block = {
                    apiService.postLiBaoListBean(NetworkApi.INSTANCE.createPostData(map)!!)
                },
                resultState = postDataAppApiByGameIdResult,
            )
        }

    }

    fun postLiBaoLingQu(cardid: String) {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            requestFlow {
                val libaoLingQu = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "getcard"
                        map["cardid"] = cardid
                        map["is_vir"] = "2"
                        map["uid"] = user.uid
                        map["token"] = user.token
                        apiService.postLiBaoLingqu(NetworkApi.INSTANCE.createPostData(map)!!)
                    },
                    resultState = postLiBaoLingQuResult,
                )
            }
            //}else {
            //   ModActivityXDLogin.start(appContext)
            //}
        }
    }


}