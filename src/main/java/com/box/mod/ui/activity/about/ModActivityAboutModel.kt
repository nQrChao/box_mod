package com.box.mod.ui.activity.about

import com.box.base.base.viewmodel.BaseViewModel
import com.box.other.blankj.utilcode.util.AppUtils

class ModActivityAboutModel : BaseViewModel() {
    var version = "VER:" + AppUtils.getAppVersionName()
}