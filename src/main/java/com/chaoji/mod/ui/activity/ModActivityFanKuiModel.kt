package com.chaoji.mod.ui.activity

import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.other.blankj.utilcode.util.AppUtils

class ModActivityFanKuiModel : BaseViewModel(title = "意见反馈") {
    var version = "VER:" + AppUtils.getAppVersionName()

    var questionText = StringObservableField("")
    var qqText = StringObservableField("")


}