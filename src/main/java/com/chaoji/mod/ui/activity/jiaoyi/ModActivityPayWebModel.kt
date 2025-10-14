package com.chaoji.mod.ui.activity.jiaoyi

import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.other.blankj.utilcode.util.AppUtils

class ModActivityPayWebModel : BaseViewModel(title = "支付中心") {
    var version = "VER:" + AppUtils.getAppVersionName()


}