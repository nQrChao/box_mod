package com.box.mod.ui.activity.login

import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField

class ModActivitySafetyModel : BaseViewModel(title = "隐私权限安全") {
    var isLogin = BooleanObservableField(false)


}