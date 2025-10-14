package com.box.mod.ui.activity

import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField

class ModActivityMessageModel : BaseViewModel(title = "消息中心") {
    var searchKey = StringObservableField()


}