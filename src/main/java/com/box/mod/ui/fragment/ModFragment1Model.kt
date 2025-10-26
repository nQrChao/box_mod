package com.box.mod.ui.fragment

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.mod.ui.data.ModTabTextBean


class ModFragment1Model : BaseViewModel(title = "") {
    val titlesBean: LiveData<List<ModTabTextBean>> = MutableLiveData(
        listOf(
            ModTabTextBean(name = "热门游戏赛事", select = ObservableBoolean(true)),
            ModTabTextBean(name = "游戏账号估值", select = ObservableBoolean(false)),
            ModTabTextBean(name = "角色名生成器", select = ObservableBoolean(false)),
            ModTabTextBean(name = "小游戏排行榜", select = ObservableBoolean(false))
        )
    )
}