package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.databinding.ObservableField
import androidx.fragment.app.viewModels
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.callback.databind.IntObservableField
import com.box.base.callback.databind.StringObservableField
import com.box.base.network.NetState
import com.box.mod.R
import com.box.mod.databinding.ModFragment2Binding
import com.box.other.immersionbar.immersionBar


class ModFragment2 : BaseTitleBarFragment<ModFragment2.Model, ModFragment2Binding>() {
    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_2

    companion object {
        fun newInstance(): ModFragment2 {
            return ModFragment2()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            statusBarDarkFont(true)
            init()
        }

    }


    override fun createObserver() {

    }

    override fun lazyLoadData() {

    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun confirm() {

        }

    }


    /**********************************************Model**************************************************/

    class Model : BaseViewModel(title = "商品发布") {
        var pic = IntObservableField(0)
        var pic1Uri  = ObservableField<Uri>()
        var pic2Uri  = ObservableField<Uri>()
        var pic3Uri  = ObservableField<Uri>()

        var gameName  = StringObservableField()
        var platformName  = StringObservableField()
        var clientType  = StringObservableField("Android")
        var productType  = StringObservableField("账号")
        var accountName  = StringObservableField()
        var titleName  = StringObservableField()
        var productIntro  = StringObservableField()
        var price  = StringObservableField()


        fun clear() {
            gameName.set("")
            platformName.set("")
            accountName.set("")
            titleName.set("")
            productIntro.set("")
            price.set("")
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
                Pair( { gameName.get().isNullOrEmpty() }, "请填写游戏名称" ),
                Pair( { platformName.get().isNullOrEmpty() }, "请填写所属平台" ),
                Pair( { accountName.get().isNullOrEmpty() }, "请填写游戏账号" ),
                Pair( { titleName.get().isNullOrEmpty() }, "请填写商品标题" ),
                Pair( { productIntro.get().isNullOrEmpty() }, "请填写商品介绍" ),
                Pair( { price.get().isNullOrEmpty() }, "请填写商品价格" ),
                Pair( { pic1Uri.get() == null }, "请选择商品截图1" ),
                Pair( { pic2Uri.get() == null }, "请选择商品截图2" ),
                Pair( { pic3Uri.get() == null }, "请选择商品截图3" )
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


}


