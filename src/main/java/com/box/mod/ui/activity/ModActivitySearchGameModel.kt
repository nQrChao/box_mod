package com.box.mod.ui.activity

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequest
import com.box.base.state.ModResultState
import com.box.common.data.model.ModGameHallList
import com.box.common.data.model.ModSearchHotGameBean
import com.box.common.network.NetworkApi
import com.box.common.network.apiService

class ModActivitySearchGameModel : BaseViewModel() {
    var searchKey = StringObservableField()
    var gameHallListResult = MutableLiveData<ModResultState<MutableList<ModGameHallList>>>()
    var searchHotGameBeanResult = MutableLiveData<ModResultState<ModSearchHotGameBean>>()

    fun postSearchHotGame() {
        modRequest({
            val map = mutableMapOf<String, String>()
            map["api"] = "game_s_best"
            apiService.postModSearchHotGameInfo(NetworkApi.INSTANCE.createPostData(map)!!)
        }, searchHotGameBeanResult)
    }

    fun postGameHallList(kw: String, page: String) {
        modRequest({
            val map = mutableMapOf<String, String>()
            map["api"] = "gamelist_page"
            map["kw"] = kw
            map["list_type"] = "search"
            map["page"] = page
            map["pagecount"] = "12"
            map["on_page"] = "search"
            map["more"] = "1"
            map["show_reserve"] = "yes"
            apiService.postModGameHallList(NetworkApi.INSTANCE.createPostData(map)!!)
        }, gameHallListResult)
    }


}