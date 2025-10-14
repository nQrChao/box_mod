package com.box.mod.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.BooleanObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.ext.request
import com.box.base.ext.requestFlow
import com.box.mod.modnetwork.ModAppException
import com.box.mod.modnetwork.ModExceptionHandle
import com.box.base.state.ModResultState
import com.box.base.state.ModResultStateWithMsg
import com.box.base.state.ResultState
import com.box.common.appContext
import com.box.common.data.model.AppletsData
import com.box.common.data.model.ModCollectionGood
import com.box.common.data.model.ModGameFuLiInfo
import com.box.common.data.model.ModGameInfo
import com.box.common.data.model.ModMeYouHuiQuanBean
import com.box.common.data.model.ModTradeGoodDetailBean
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.utils.MMKVUtil
import com.box.mod.ui.activity.login.ModActivityXDLogin
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.blankj.utilcode.util.Logs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Random

class ModActivityGameDetailsModel : BaseViewModel(title = "游戏详情") {
    var lingQuType = BooleanObservableField(false)
    var modGameInfo = MutableLiveData<ModGameInfo>()
    var modFuLiInfo = MutableLiveData<ModGameFuLiInfo>()
    var myGameYouHui = MutableLiveData<MutableList<ModMeYouHuiQuanBean>>()

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

    var gameInfoResult = MutableLiveData<ModResultStateWithMsg<ModGameInfo>>()
    var gameFuLiInfoResult = MutableLiveData<ModResultStateWithMsg<ModGameFuLiInfo>>()

    var postYouHuiQuanLingQuResult = MutableLiveData<ModResultStateWithMsg<Any>>()
    var postGameYouHuiResult = MutableLiveData<ModResultStateWithMsg<MutableList<ModMeYouHuiQuanBean>>>()

    // 私有的、可变的 LiveData，仅在 ViewModel 内部使用
    private val _score = MutableLiveData<Float>()
    // 公开的、不可变的 LiveData，供 UI (XML) 订阅
    val score: LiveData<Float> = _score
    /**
     * 生成一个随机分数并更新 LiveData
     */
    fun generateRandomScore() {
        val possibleScores = floatArrayOf(2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f)
        val randomIndex = Random().nextInt(possibleScores.size)
        // 使用 .value 来更新 LiveData 的值
        _score.value = possibleScores[randomIndex]
    }

    // 随机数添加 LiveData
    private val _downloadCount = MutableLiveData<Int>()
    val downloadCount: LiveData<Int> = _downloadCount

    /**
     * 生成 1000 到 10000 之间的随机整数
     */
    fun generateRandomDownloadCount() {
        // 使用 Kotlin 的区间随机函数，它包含起始值和结束值
        val randomCount = (1000..10000).random()
        _downloadCount.value = randomCount
    }

    fun postGetGameInfo(gameId: String) {
        modRequestWithMsg({
            val map = mutableMapOf<String, String>()
            map["api"] = "gameinfo_part_base"
            map["gameid"] = gameId
            apiService.postModGameInfo(NetworkApi.INSTANCE.createPostData(map)!!)
        }, gameInfoResult)
    }

    fun postGetGameFuLiInfo(gameId: String) {
        modRequestWithMsg({
            val map = mutableMapOf<String, String>()
            map["api"] = "gameinfo_part_fl"
            map["gameid"] = gameId
            apiService.postModGameFuLiInfo(NetworkApi.INSTANCE.createPostData(map)!!)
        }, gameFuLiInfoResult)
    }

    fun postYouHuiQuanLingQuApi(coupon_id: String) {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            requestFlow {
                val lingqu = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "get_coupon"
                        map["coupon_id"] = coupon_id
                        map["from_web"] = "1"
                        map["uid"] = user.uid
                        map["token"] = user.token
                        apiService.postYouHuiQuanLingQuApi(NetworkApi.INSTANCE.createPostData(map)!!)
                    },
                    resultState = postYouHuiQuanLingQuResult,
                )
            }
        } else {
            ModActivityXDLogin.start(appContext)
        }
    }


    /**
     * 比较 myGameYouHui 列表中是否有 gameid 与 modFuLiInfo.coupon_list 的第一个元素的 gameid 相同。
     * @return Boolean - 如果找到匹配项则返回 true，否则返回 false。
     */
    fun doesMyCouponListContainFirstFuLiGameId(): Boolean {
        val targetGameId = modFuLiInfo.value?.coupon_list?.firstOrNull()?.gameid
        val myCouponList = myGameYouHui.value
        if (targetGameId.isNullOrBlank() || myCouponList.isNullOrEmpty()) {
            println("比较失败：福利信息或我的优惠券列表为空。")
            return false
        }
        // 使用 Kotlin 的 `any` 函数来检查列表中是否存在任何一个元素的 gameid 与 targetGameId 匹配。
        // `any` 函数非常高效，一旦找到匹配项就会立即停止遍历并返回 true。
        val isMatchFound = myCouponList.any { it.gameid == targetGameId }
        // 打印日志方便调试
        println("福利列表的第一个gameid是: $targetGameId")
        println("我的优惠券列表中是否包含此gameid: $isMatchFound")
        return isMatchFound
    }
    fun postYouHuiQuan() {
        val user = MMKVUtil.getModUser()
        Logs.e("USER:${GsonUtils.toJson(user)}")
        if (user != null) {
            requestFlow {
                val youhuiquan = step(
                    block = {
                        val map = mutableMapOf<String, String>()
                        map["api"] = "coupon_record"
                        map["list_type"] = "game"
                        map["need_gameinfo"] = "1"
                        map["uid"] = user.uid
                        map["token"] = user.token
                        apiService.postMeYouHuiQuanAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
                    },
                    resultState = postGameYouHuiResult,
                )
            }
        }else {
            ModActivityXDLogin.start(appContext)
        }
    }


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