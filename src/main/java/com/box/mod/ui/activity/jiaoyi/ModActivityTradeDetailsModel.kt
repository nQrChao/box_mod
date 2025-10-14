package com.box.mod.ui.activity.jiaoyi

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.request
import com.box.mod.modnetwork.ModAppException
import com.box.mod.modnetwork.ModExceptionHandle
import com.box.base.state.ModResultState
import com.box.base.state.ModResultStateWithMsg
import com.box.base.state.ResultState
import com.box.common.data.model.AppletsData
import com.box.common.data.model.ModCollectionGood
import com.box.common.data.model.ModTradeGoodDetailBean
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.utils.MMKVUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ModActivityTradeDetailsModel : BaseViewModel(title = "商品详情") {
    var shoucang = BooleanObservableField()
    var showShouCang = BooleanObservableField(false)
    var searchKey = StringObservableField()
    val photoList: ArrayList<Any> = ArrayList()
    var postDataAppApiByGameIdResult = MutableLiveData<ResultState<AppletsData>>()
    var postShouCangResult = MutableLiveData<ModResultStateWithMsg<ModCollectionGood>>()
    var postShouCangCancelResult = MutableLiveData<ModResultStateWithMsg<ModCollectionGood>>()

    val photoBannerList: ArrayList<String> = ArrayList()
    var tradeGoodDetail = MutableLiveData<ModTradeGoodDetailBean?>()
    var tradeGoodsListResult = MutableLiveData<ModResultState<MutableList<ModTradeGoodDetailBean>>>()

    fun postTradeGoodDetailAll(gid: String, type: String) {
        viewModelScope.launch {
            try {
                val detailMap = mutableMapOf<String, String>()
                detailMap["gid"] = gid
                detailMap["type"] = type
                val detailResponse = apiService.tradeGoodDetail(NetworkApi.INSTANCE.createPostData(detailMap)!!)
                if (detailResponse.isSucceed()) {
                    // 使用 var 声明，方便后续可能被更新
                    var tradeGood = detailResponse.getResponseData()
                    if (tradeGood != null) {
                        // --- 【新增逻辑】: 为主商品详情对象获取并更新 gameicon ---
                        try {
                            val iconMap = mutableMapOf<String, String>()
                            iconMap["api"] = "market_tradegame"
                            iconMap["gameid"] = tradeGood.gameid
                            val iconResponse = apiService.postModTradeGameIcon(NetworkApi.INSTANCE.createPostData(iconMap)!!)
                            if (iconResponse.isSucceed() && iconResponse.getResponseData() != null) {
                                val newIconUrl = iconResponse.getResponseData()!!.tradegameicon
                                if (newIconUrl.isNotEmpty()) {
                                    // 将 tradeGood 变量指向一个带有新 gameicon 的副本
                                    tradeGood = tradeGood.copy(gameicon = newIconUrl)
                                }
                            }
                        } catch (e: Exception) {
                            // 单独的图标获取失败不应中断整个流程，仅作记录
                            // Log.e("IconFetch", "Failed to fetch icon for detail object", e)
                        }

                        // --- 使用被更新过的 tradeGood 对象继续后续操作 ---
                        if (tradeGood != null) {
                            setPhotoList(tradeGood)
                            showShouCang.set(true)
                            tradeGoodDetail.value = tradeGood // 此处赋值的是更新后的对象
                            shoucang.set(MMKVUtil.isShouCangCollected(tradeGood.gid))
                        }
                        // 获取相关商品列表 (此部分逻辑不变)
                        val listMap = mutableMapOf<String, String>()
                        listMap["scene"] = "normal"
                        if (tradeGood != null) {
                            listMap["gameid"] = tradeGood.gameid
                            listMap["rm_gid"] = tradeGood.gid
                            listMap["goods_type"] = tradeGood.goods_type
                            listMap["pic"] = "multiple"
                            listMap["page"] = "1"
                            listMap["pagecount"] = "3"
                        }
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
                    } else {
                        tradeGoodsListResult.value = ModResultState.onAppError(ModAppException("-1", "", "商品详情为空"))
                    }
                } else {
                    tradeGoodsListResult.value = ModResultState.onAppError(
                        ModAppException(detailResponse.getResponseCode().toString(), "", detailResponse.getResponseMsg())
                    )
                }
            } catch (e: Throwable) {
                tradeGoodsListResult.value = ModResultState.onAppError(ModExceptionHandle.handleException(e))
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
            photoBannerList.add(pic.pic_path)
        }
    }

    fun postDataAppApiByGameIdResul(gameId: String) {
        request({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = gameId
            apiService.postDataAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApiByGameIdResult)
    }

    fun postShouCangByGameIdResul(gameId: String) {
        modRequestWithMsg({
            val map = mutableMapOf<String, String>()
            map["gameid"] = gameId
            apiService.collectionGood(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postShouCangResult)
    }
}