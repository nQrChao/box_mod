package com.box.mod.ui.fragment

import android.net.Uri
import androidx.databinding.ObservableField
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField


class ModFragment12Model : BaseViewModel(title = "游戏账号估值") {
    var pic = IntObservableField(0)
    var gameName  = StringObservableField()
    var gameNickName  = StringObservableField()
    var gameServerName  = StringObservableField()
    var gamePrice  = StringObservableField()
    var pic1Uri  = ObservableField<Uri>()
    var pic2Uri  = ObservableField<Uri>()
    var pic3Uri  = ObservableField<Uri>()

    fun clearData() {
        gameName.set("")
        gameNickName.set("")
        gameServerName.set("")
        gamePrice.set("")
        pic1Uri.set(null)
        pic2Uri.set(null)
        pic3Uri.set(null)
    }

    /**
     * 数据校验方法
     * @return 返回null表示校验通过，否则返回错误提示信息
     */
    fun getValidationError(): String? {
        // 使用一个“规则列表”来定义所有校验
        val validationRules = listOf(
            Pair( { gameName.get().isEmpty() }, "请填写游戏名" ),
            Pair( { gameNickName.get().isEmpty() }, "请填写角色名" ),
            Pair( { gameServerName.get().isEmpty() }, "请填写区服名" ),
            Pair( { gamePrice.get().isEmpty() }, "请填写实充金额" ),
            Pair( { pic1Uri.get() == null && pic2Uri.get() == null && pic3Uri.get() == null}, "请上传角色信息截图，至少上传1张截图" ),
        )
        // 遍历规则，找到第一个不满足的并返回错误信息
        for ((condition, message) in validationRules) {
            if (condition()) {
                return message
            }
        }
        // 所有规则都通过
        return null
    }

}