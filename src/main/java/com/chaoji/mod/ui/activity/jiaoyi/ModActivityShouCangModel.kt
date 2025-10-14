package com.chaoji.mod.ui.activity.jiaoyi

import androidx.lifecycle.MutableLiveData
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.im.data.model.ModTradeGoodDetailBean

class ModActivityShouCangModel : BaseViewModel(title = "我的收藏") {
    var hasTradeGoods = BooleanObservableField()
    var tradeGoodsList = MutableLiveData<MutableList<ModTradeGoodDetailBean>>()

}