package com.box.mod.ui.activity.game

import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.other.immersionbar.BarHide

class ModActivityLocalGameBrowserModel : BaseViewModel(barHid = BarHide.FLAG_HIDE_BAR) {
    var showGame = BooleanObservableField()
    var hideLoading = BooleanObservableField()


}