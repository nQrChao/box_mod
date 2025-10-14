package com.chaoji.mod.ui.activity.login

import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField

class ModActivitySafetyModel : BaseViewModel(title = "隐私权限安全") {
    var isLogin = BooleanObservableField(false)


}