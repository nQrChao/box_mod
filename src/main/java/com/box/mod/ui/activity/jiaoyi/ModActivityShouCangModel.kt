package com.box.mod.ui.activity.jiaoyi

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.common.data.model.ModTradeGoodDetailBean

class ModActivityShouCangModel : BaseViewModel(title = "我的收藏") {
    var hasTradeGoods = BooleanObservableField()
    var tradeGoodsList = MutableLiveData<MutableList<ModTradeGoodDetailBean>>()

}