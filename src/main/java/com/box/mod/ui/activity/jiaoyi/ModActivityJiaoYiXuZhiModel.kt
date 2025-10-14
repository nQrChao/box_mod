package com.box.mod.ui.activity.jiaoyi

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import com.box.base.base.viewmodel.BaseViewModel
import com.box.other.blankj.utilcode.util.AppUtils


class ModActivityJiaoYiXuZhiModel : BaseViewModel() {
    var version = "VER:" + AppUtils.getAppVersionName()
    val selectedTab = ObservableInt(0)
    var shown = ObservableBoolean(false)


}