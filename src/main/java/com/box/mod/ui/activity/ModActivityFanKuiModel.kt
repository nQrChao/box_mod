package com.box.mod.ui.activity

import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField
import com.box.other.blankj.utilcode.util.AppUtils

class ModActivityFanKuiModel : BaseViewModel(title = "意见反馈") {
    var version = "VER:" + AppUtils.getAppVersionName()

    var questionText = StringObservableField("")
    var qqText = StringObservableField("")


}