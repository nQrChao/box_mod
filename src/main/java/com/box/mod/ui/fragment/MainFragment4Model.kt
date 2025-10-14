package com.box.mod.ui.fragment

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.common.data.model.ModUserInfoBean

class MainFragment4Model: BaseViewModel(title = "个人中心") {
    var id = StringObservableField("")
    var shown = ObservableBoolean(false)
    val tuiSong = MutableLiveData<Boolean>()
    var modUser = MutableLiveData<ModUserInfoBean>()
    var isLogin = BooleanObservableField(false)
    fun loginOut() {
    }


}