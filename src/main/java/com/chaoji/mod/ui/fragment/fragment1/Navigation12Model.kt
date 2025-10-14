package com.chaoji.mod.ui.fragment.fragment1

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.IntObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.modnetwork.ModAppException
import com.chaoji.base.modnetwork.ModExceptionHandle
import com.chaoji.base.state.ModResultState
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.im.network.ModApiResponse
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class Navigation12Model : BaseViewModel() {
    var isSelect = IntObservableField(0)
    var searchKey = StringObservableField()
    val photoList: ArrayList<Any> = ArrayList()

    var tradeDiyGoodsListResult = MutableLiveData<ModResultState<MutableList<ModTradeGoodDetailBean>>>()

    /**
     * 重构后的方法，现在会先获取列表，再获取正确的 gameicon 进行赋值。
     */
    fun postDiyTradeGoodsList(orderBy: String, page: String, pageCount: String) {
        fetchAndProcessGoods(
            goodsListFetcher = {
                val map = mutableMapOf<String, String>()
                map["scene"] = "normal"
                map["pic"] = "multiple"
                map["one_discount"] = "yes"
                map["orderby"] = orderBy
                map["page"] = page
                map["kw"] = searchKey.get() // 保留了原有的搜索关键字参数
                map["pagecount"] = pageCount
                map["r_time"] = ""
                apiService.tradeGoodsList(NetworkApi.INSTANCE.createPostData(map)!!)
            },
            liveDataResult = tradeDiyGoodsListResult
        )
    }

    /**
     * 核心处理逻辑：获取商品列表 -> 并发获取图标 -> 更新 gameicon 字段 -> 提交结果
     */
    private fun fetchAndProcessGoods(
        goodsListFetcher: suspend () -> ModApiResponse<MutableList<ModTradeGoodDetailBean>>,
        liveDataResult: MutableLiveData<ModResultState<MutableList<ModTradeGoodDetailBean>>>
    ) {
        viewModelScope.launch {
            try {
                val goodsResponse = goodsListFetcher()

                if (goodsResponse.isSucceed()) {
                    val originalList = goodsResponse.getResponseData()

                    if (!originalList.isNullOrEmpty()) {
                        val updatedList = coroutineScope {
                            originalList.map { good ->
                                async {
                                    try {
                                        val iconMap = mutableMapOf("api" to "market_tradegame", "gameid" to good.gameid)
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

                        val finalList = updatedList.toMutableList()
                        liveDataResult.value = ModResultState.onAppSuccess(finalList)
                    } else {
                        liveDataResult.value = ModResultState.onAppSuccess(originalList)
                    }
                } else {
                    liveDataResult.value = ModResultState.onAppError(
                        ModAppException(
                            goodsResponse.getResponseCode().toString(),
                            "",
                            goodsResponse.getResponseMsg()
                        )
                    )
                }
            } catch (e: Throwable) {
                liveDataResult.value = ModResultState.onAppError(ModExceptionHandle.handleException(e))
            }
        }
    }
}