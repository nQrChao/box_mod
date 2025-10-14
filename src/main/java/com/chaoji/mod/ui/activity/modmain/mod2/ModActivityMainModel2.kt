package com.chaoji.mod.ui.activity.modmain.mod2

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.ext.request
import com.chaoji.base.state.ResultState
import com.chaoji.im.network.apiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class ModActivityMainModel2: BaseViewModel(titleLine = false) {
    var loginOutResult=MutableLiveData<Boolean>()
    var registerResult = MutableLiveData<ResultState<Any?>>()
    fun loginOut(){

    }

    fun marketInit() {
        request({
            val map = mutableMapOf<String, Any>()
            map["device"] = 21
            val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), Gson().toJson(map))
            apiService.register(body)
        }, registerResult)
    }

}