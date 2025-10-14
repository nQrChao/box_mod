package com.chaoji.mod.ui.activity.modmain.mod5

import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.IntObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.request
import com.chaoji.base.state.ResultState
import com.chaoji.im.data.model.AIChat
import com.chaoji.im.data.model.AiMessage
import com.chaoji.im.data.model.AppletsData
import com.chaoji.im.data.model.RefreshAiTokenInfo
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.utils.MMKVUtil
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class Mod5Fragment1Model : BaseViewModel() {
    var getChatId = ""
    var configResult = MutableLiveData<ResultState<String>>()
    var aiChatListResult = MutableLiveData<ResultState<MutableList<AIChat>>>()
    var createChatResult = MutableLiveData<ResultState<AIChat>>()
    var refreshAiTokenInfo = MutableLiveData<ResultState<RefreshAiTokenInfo>>()
    var messageListResult = MutableLiveData<ResultState<List<AiMessage>>>()
    var aiText = StringObservableField()
    var aiConfigText = ""
    var curChat = MutableLiveData<AIChat>()
    var questionText = MutableLiveData<String>()
    var delChatResult = MutableLiveData<ResultState<Any?>>()

    var postDataAppApi248Result = MutableLiveData<ResultState<AppletsData>>()
    var postDataAppApi249Result = MutableLiveData<ResultState<AppletsData>>()

    fun postDataAppApi248() {
        request({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = "248"
            apiService.postDataAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApi248Result)
    }
    fun postDataAppApi249() {
        request({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = "249"
            apiService.postDataAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApi249Result)
    }

    //是否显示
    var isBtnVisibility = IntObservableField(View.VISIBLE)

    //设置就显示/反之
    var isShowView = object : ObservableInt(isBtnVisibility) {
        override fun get(): Int {
            return if (isBtnVisibility.get() == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
    fun aiResponseToken() {
        request({
            val map = mutableMapOf<String, Any>()
            map["refresh"] = MMKVUtil.getAiRefreshToken()!!
            map["device"] = 21
            val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), Gson().toJson(map))
            apiService.refreshAiToken(body)
        }, refreshAiTokenInfo)
    }

    fun aiConfig() {
        request({
            apiService.aiConfig()
        }, configResult)
    }

    fun chatList() {
        request({
            apiService.aiChatList()
        }, aiChatListResult)
    }

    fun createChat(title: String, id: Int? = null) {
        val map = mutableMapOf<String, Any>()
        map["tittle"] = title
        if (StringUtils.isEmpty(getChatId)) {
            id?.let {
                map["chatmodelid"] = id
            }
        } else {
            map["chatmodelid"] = getChatId
        }
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), Gson().toJson(map))
        request({ apiService.createChat(body) }, createChatResult)
    }

    fun getChatMessage(id:String) {
        request({ apiService.chatMessage(id) }, messageListResult)
    }

    fun delChatMessage(id:String) {
        request({ apiService.delChatMessage(id) }, delChatResult)
    }

}