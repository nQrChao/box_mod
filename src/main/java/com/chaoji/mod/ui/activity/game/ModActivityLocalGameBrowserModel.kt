package com.chaoji.mod.ui.activity.game

import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.other.immersionbar.BarHide

class ModActivityLocalGameBrowserModel : BaseViewModel(barHid = BarHide.FLAG_HIDE_BAR) {
    var showGame = BooleanObservableField()
    var hideLoading = BooleanObservableField()


}