package com.chaoji.mod.ui.fragment.fragment1

import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chaoji.base.base.viewmodel.BaseViewModel
import com.chaoji.base.callback.databind.IntObservableField
import com.chaoji.base.callback.databind.StringObservableField
import com.chaoji.base.ext.modRequestWithMsg
import com.chaoji.base.ext.request
import com.chaoji.base.modnetwork.ModAppException
import com.chaoji.base.modnetwork.ModExceptionHandle
import com.chaoji.base.state.ModResultState
import com.chaoji.base.state.ModResultStateWithMsg
import com.chaoji.base.state.ResultState
import com.chaoji.im.data.model.AIChat
import com.chaoji.im.data.model.AiMessage
import com.chaoji.im.data.model.ModGameIcon
import com.chaoji.im.data.model.ModGameInfo
import com.chaoji.im.data.model.ModTradeGoodDetailBean
import com.chaoji.im.data.model.RefreshAiTokenInfo
import com.chaoji.im.network.ModApiResponse
import com.chaoji.im.network.NetworkApi
import com.chaoji.im.network.apiService
import com.chaoji.im.utils.MMKVUtil
import com.chaoji.other.blankj.utilcode.util.Logs
import com.chaoji.other.blankj.utilcode.util.StringUtils
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class Navigation1Model : BaseViewModel(title = "首页") {

    var getChatId = ""
    var configResult = MutableLiveData<ResultState<String>>()
    var aiChatListResult = MutableLiveData<ResultState<MutableList<AIChat>>>()
    var createChatResult = MutableLiveData<ResultState<AIChat>>()
    var refreshAiTokenInfo = MutableLiveData<ResultState<RefreshAiTokenInfo>>()
    var messageListResult = MutableLiveData<ResultState<List<AiMessage>>>()
    var aiText = StringObservableField()
    var aiConfigText = ""
    var curChat = MutableLiveData<AIChat>()
    var questionText = MutableLiveData<String>()
    var delChatResult = MutableLiveData<ResultState<Any?>>()

    val photoList: ArrayList<Any> = ArrayList()

    // Fragment 正在观察这些 LiveData，我们将直接向它们提供最终数据
    var tradeDiyGoodsListResult = MutableLiveData<ModResultState<MutableList<ModTradeGoodDetailBean>>>()
    var tradeAllGoodsListResult = MutableLiveData<ModResultState<MutableList<ModTradeGoodDetailBean>>>()

    // 根据您的要求，同时将处理好的数据赋值给 goodsList 和 goodsDiyList
    var goodsList = MutableLiveData<MutableList<ModTradeGoodDetailBean>>()
    var goodsDiyList = MutableLiveData<MutableList<ModTradeGoodDetailBean>>()


    var topGoodsList = MutableLiveData<MutableList<ModTradeGoodDetailBean>>()


    var topGoods = MutableLiveData<ModTradeGoodDetailBean>()
    var topGameInfo = MutableLiveData<ModResultStateWithMsg<ModGameInfo>>()
    var gameIcon = MutableLiveData<ModResultStateWithMsg<ModGameIcon>>()

    fun postGetGameIcon(gameId: String) {
        modRequestWithMsg({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_tradegame"
            map["gameid"] = gameId
            apiService.postModTradeGameIcon(NetworkApi.INSTANCE.createPostData(map)!!)
        }, gameIcon)
    }

    fun postGetGameInfo(gameId: String) {
        modRequestWithMsg({
            val map = mutableMapOf<String, String>()
            map["api"] = "gameinfo_part_base"
            map["gameid"] = gameId
            apiService.postModGameInfo(NetworkApi.INSTANCE.createPostData(map)!!)
        }, topGameInfo)
    }

    /**
     * 获取所有商品列表。
     * 此方法已重构：获取列表后，会接着获取每个商品的图标，并替换原有图片数据，最后才更新LiveData。
     */
    fun postAllTradeGoodsList() {
        fetchAndProcessGoods(
            goodsListFetcher = {
                val map = mutableMapOf<String, String>()
                map["scene"] = "normal"
                map["goods_type"] = "0"
                apiService.tradeGoodsList(NetworkApi.INSTANCE.createPostData(map)!!)
            },
            liveDataResult = tradeAllGoodsListResult,
            liveDataList = goodsList
        )
    }

    /**
     * 获取自定义排序/分页的商品列表。
     * 此方法已重构：获取列表后，会接着获取每个商品的图标，并替换原有图片数据，最后才更新LiveData。
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
                map["pagecount"] = pageCount
                map["r_time"] = ""
                apiService.tradeGoodsList(NetworkApi.INSTANCE.createPostData(map)!!)
            },
            liveDataResult = tradeDiyGoodsListResult,
            liveDataList = goodsDiyList
        )
    }

    /**
     * 最终修正版：将获取到的图片地址赋值给 gameicon 字段。
     */
    private fun fetchAndProcessGoods(
        goodsListFetcher: suspend () -> ModApiResponse<MutableList<ModTradeGoodDetailBean>>,
        liveDataResult: MutableLiveData<ModResultState<MutableList<ModTradeGoodDetailBean>>>,
        liveDataList: MutableLiveData<MutableList<ModTradeGoodDetailBean>>
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
                                        Logs.d("IconFetch", "Requesting icon for gameId: ${good.gameid}")
                                        val iconResponse = apiService.postModTradeGameIcon(NetworkApi.INSTANCE.createPostData(iconMap)!!)
                                        if (iconResponse.isSucceed() && iconResponse.getResponseData() != null) {
                                            val newIconUrl = iconResponse.getResponseData()!!.tradegameicon
                                            Logs.d("IconFetch", "SUCCESS for gameId: ${good.gameid}. URL: '$newIconUrl'")
                                            if (newIconUrl.isNotEmpty()) {
                                                // 【关键修改】使用 copy() 创建一个新对象，并只更新 gameicon 字段
                                                Logs.d("IconFetch", "Assigning new URL to gameicon for gameId: ${good.gameid}. Old: '${good.gameicon}', New: '$newIconUrl'")
                                                good.copy(gameicon = newIconUrl)
                                            } else {
                                                Logs.w("IconFetch", "Icon URL is empty for gameId: ${good.gameid}")
                                                good // URL为空，返回原对象
                                            }
                                        } else {
                                            Logs.e("IconFetch", "API call failed for gameId: ${good.gameid}. Msg: ${iconResponse.getResponseMsg()}")
                                            good // 接口失败，返回原对象
                                        }
                                    } catch (e: Exception) {
                                        Logs.e("IconFetch", "Exception for gameId: ${good.gameid}", e)
                                        good // 出现异常，返回原对象
                                    }
                                }
                            }.awaitAll()
                        }

                        val finalList = updatedList.toMutableList()
                        liveDataResult.value = ModResultState.onAppSuccess(finalList)
                        liveDataList.value = finalList

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


    //是否显示
    var isBtnVisibility = IntObservableField(View.VISIBLE)
    //设置就显示/反之
    var isShowView = object : ObservableInt(isBtnVisibility) {
        override fun get(): Int {
            return if (isBtnVisibility.get() == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    fun aiResponseToken() {
        request({
            val map = mutableMapOf<String, Any>()
            map["refresh"] = MMKVUtil.getAiRefreshToken()!!
            map["device"] = 21
            val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), Gson().toJson(map))
            apiService.refreshAiToken(body)
        }, refreshAiTokenInfo)
    }

    fun aiConfig() {
        request({
            apiService.aiConfig()
        }, configResult)
    }

    fun chatList() {
        request({
            apiService.aiChatList()
        }, aiChatListResult)
    }

    fun createChat(title: String, id: Int? = null) {
        val map = mutableMapOf<String, Any>()
        map["tittle"] = title
        if (StringUtils.isEmpty(getChatId)) {
            id?.let {
                map["chatmodelid"] = id
            }
        } else {
            map["chatmodelid"] = getChatId
        }
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), Gson().toJson(map))
        request({ apiService.createChat(body) }, createChatResult)
    }

    fun getChatMessage(id: String) {
        request({ apiService.chatMessage(id) }, messageListResult)
    }

    fun delChatMessage(id: String) {
        request({ apiService.delChatMessage(id) }, delChatResult)
    }

}