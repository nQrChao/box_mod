package com.chaoji.mod.ui.activity.jiaoyi

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.callback.livedata.BooleanLiveData
import com.chaoji.other.blankj.utilcode.util.AppUtils


class ModActivityJiaoYiXuZhiModel : BaseViewModel() {
    var version = "VER:" + AppUtils.getAppVersionName()
    val selectedTab = ObservableInt(0)
    var shown = ObservableBoolean(false)


}