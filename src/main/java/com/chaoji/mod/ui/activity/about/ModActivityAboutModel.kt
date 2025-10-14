package com.chaoji.mod.ui.activity.about

import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.other.blankj.utilcode.util.AppUtils

class ModActivityAboutModel : BaseViewModel() {
    var version = "VER:" + AppUtils.getAppVersionName()
}