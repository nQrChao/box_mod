package com.chaoji.mod.ui.activity

import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.StringObservableField

class ModActivityPicDownModel : BaseViewModel(title = "壁纸详情") {
    var searchKey = StringObservableField()
}