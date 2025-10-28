package com.box.mod.ui.fragment

import androidx.lifecycle.MutableLiveData
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.ext.modRequestWithMsg
import com.box.base.state.ModResultStateWithMsg
import com.box.common.data.model.ModDataBean
import com.box.common.network.apiService


class ModFragment11Model : BaseViewModel(title = "") {
    var pic = IntObservableField(0)
    var newsListResult = MutableLiveData<ModResultStateWithMsg< MutableList<ModDataBean>>>()
    var newsDetailResult = MutableLiveData<ModResultStateWithMsg< ModDataBean>>()

    fun getNewsListData(pageNum: Int,pageSize: Int) {
        modRequestWithMsg({
            apiService.getNewsList(pageNum,pageSize)
        }, newsListResult)
    }

    fun getNewsDetailData(id: Int) {
        modRequestWithMsg({
            apiService.getNewsDetailById(id)
        }, newsDetailResult)
    }

}