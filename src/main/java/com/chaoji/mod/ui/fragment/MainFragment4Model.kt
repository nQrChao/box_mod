package com.chaoji.mod.ui.fragment

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.im.data.model.ModUserInfoBean

class MainFragment4Model: BaseViewModel(title = "个人中心") {
    var id = StringObservableField("")
    var shown = ObservableBoolean(false)
    val tuiSong = MutableLiveData<Boolean>()
    var modUser = MutableLiveData<ModUserInfoBean>()
    var isLogin = BooleanObservableField(false)
    fun loginOut() {
    }


}