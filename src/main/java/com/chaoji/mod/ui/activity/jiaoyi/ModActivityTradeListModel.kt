package com.chaoji.mod.ui.activity.jiaoyi

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.BooleanObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.modnetwork.ModAppException
import com.chaoji.base.state.ModResultState
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ModActivityTradeListModel : BaseViewModel(title = "　　　　　") {
    var gameId = ""
    var gameName = ""
    var gameIcon = ""
    var shoucang = BooleanObservableField()
    var showShouCang = BooleanObservableField(false)
    var searchKey = StringObservableField()
    val photoList: ArrayList<Any> = ArrayList()

    var tradeGoodDetail = MutableLiveData<ModTradeGoodDetailBean?>()
    var tradeGoodsListResult = MutableLiveData<ModResultState<MutableList<ModTradeGoodDetailBean>>>()

    fun postTradeGoodBeanList(gameId: String, page: Int, pageCount:String) {
        viewModelScope.launch {
            val listMap = mutableMapOf<String, String>()
            listMap["scene"] = "normal"
            listMap["gameid"] = gameId
            listMap["rm_gid"] = ""
            listMap["goods_type"] = ""
            listMap["pic"] = "multiple"
            listMap["page"] = page.toString()
            listMap["pagecount"] = pageCount
            val listResponse = apiService.tradeGoodsList(NetworkApi.INSTANCE.createPostData(listMap)!!)

            if (listResponse.isSucceed()) {
                val originalList = listResponse.getResponseData()
                if (!originalList.isNullOrEmpty()) {
                    val updatedList = processGoodsListWithIcons(originalList)
                    tradeGoodsListResult.value = ModResultState.onAppSuccess(updatedList.toMutableList())
                } else {
                    tradeGoodsListResult.value = ModResultState.onAppSuccess(originalList)
                }
            } else {
                tradeGoodsListResult.value = ModResultState.onAppError(
                    ModAppException(listResponse.getResponseCode().toString(), "", listResponse.getResponseMsg())
                )
            }

        }
    }

    private suspend fun processGoodsListWithIcons(list: List<ModTradeGoodDetailBean>): List<ModTradeGoodDetailBean> {
        return coroutineScope {
            list.map { good ->
                async {
                    try {
                        val iconMap = mutableMapOf<String, String>()
                        iconMap["api"] = "market_tradegame"
                        iconMap["gameid"] = good.gameid
                        val iconResponse = apiService.postModTradeGameIcon(NetworkApi.INSTANCE.createPostData(iconMap)!!)
                        if (iconResponse.isSucceed() && iconResponse.getResponseData() != null) {
                            val newIconUrl = iconResponse.getResponseData()!!.tradegameicon
                            if (newIconUrl.isNotEmpty()) {
                                good.copy(gameicon = newIconUrl)
                            } else {
                                good
                            }
                        } else {
                            good
                        }
                    } catch (e: Exception) {
                        good
                    }
                }
            }.awaitAll()
        }
    }

    private fun setPhotoList(bean: ModTradeGoodDetailBean) {
        photoList.clear()
        bean.pic_list.forEachIndexed { index, pic ->
            photoList.add(pic)
        }
    }

}