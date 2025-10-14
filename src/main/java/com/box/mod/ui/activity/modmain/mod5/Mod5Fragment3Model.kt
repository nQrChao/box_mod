package com.box.mod.ui.activity.modmain.mod5

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.ext.request
import com.box.base.state.ResultState
import com.box.common.network.NetworkApi
import com.box.common.network.apiService
import com.box.common.data.model.AppletsData
import com.box.common.data.model.AppletsInfo
import com.box.common.data.model.ModLocalAppletsInfo

class Mod5Fragment3Model : BaseViewModel(title = "", isStatusBarEnabled = false, titleLine = false) {
    var localAppletsList = MutableLiveData<MutableList<ModLocalAppletsInfo>>()
    var postDataAppApi259Result = MutableLiveData<ResultState<AppletsData>>()
    fun postDataAppApi259() {
        request({
            val map = mutableMapOf<String, String>()
            map["api"] = "market_data_appapi"
            map["market_data_id"] = "259"
            apiService.postDataAppApi(NetworkApi.INSTANCE.createPostData(map)!!)
        }, postDataAppApi259Result)
    }

    fun setLocalAppletsList(appletsList: MutableList<AppletsInfo>) {
        val applets: MutableList<ModLocalAppletsInfo> = mutableListOf()
        appletsList.forEach { applet ->
            val localApplet = ModLocalAppletsInfo()
            localApplet.id = applet.id
            localApplet.icon = applet.pic
            localApplet.title = applet.marketjson.app_title
            localApplet.title2 = applet.marketjson.app_title2

            applet.marketjson.list_data.forEachIndexed { index, applet2 ->
                when (index) {
                    0 -> {
                        localApplet.pic1 = applet2.pic
                        localApplet.redirect1 = applet2.redirect
                    }

                    1 -> {
                        localApplet.pic2 = applet2.pic
                        localApplet.redirect2 = applet2.redirect
                    }

                    2 -> {
                        localApplet.pic3 = applet2.pic
                        localApplet.redirect3 = applet2.redirect
                        localApplet.desc3_1 = applet2.marketjson.app_title
                        localApplet.desc3_2 = applet2.marketjson.app_title2

                    }

                    3 -> {
                        localApplet.pic4 = applet2.pic
                        localApplet.redirect4 = applet2.redirect
                        localApplet.desc4_1 = applet2.marketjson.app_title
                        localApplet.desc4_2 = applet2.marketjson.app_title2
                    }
                }
            }
            applets.add(localApplet)
        }

        localAppletsList.value = applets
    }


}
