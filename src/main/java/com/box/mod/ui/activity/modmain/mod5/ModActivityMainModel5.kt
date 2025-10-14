package com.box.mod.ui.activity.modmain.mod5
import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.ext.request
import com.box.base.state.ResultState
import com.box.common.network.apiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class ModActivityMainModel5: BaseViewModel(titleLine = false) {
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