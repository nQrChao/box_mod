package com.box.mod.ui.activity

import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField

class ModActivityPicDownModel : BaseViewModel(title = "壁纸详情") {
    var searchKey = StringObservableField()
}