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
import com.chaoji.im.data.model.ModLiBaoLingQu
import com.chaoji.im.data.model.ModLiBaoListBean
import com.chaoji.im.data.model.ModMeYouHuiQuanBean
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.mod.ui.activity.login.ModActivityXDLogin
import com.chaoji.other.blankj.utilcode.util.GsonUtils
import com.chaoji.other.blankj.utilcode.util.Logs

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