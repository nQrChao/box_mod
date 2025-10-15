package com.box.mod.ui.activity.modmain.mod2

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.ext.parseState
import com.box.base.network.NetState
import com.box.common.appContext
import com.box.common.data.model.AppletsInfo
import com.box.common.sdk.ImSDK.Companion.eventViewModelInstance

import com.box.mod.ui.activity.ModActivityPicDown
import com.box.common.ui.adapter.SpacingItemDecorator
import com.box.mod.R
import com.box.com.R as RC
import com.box.mod.databinding.Mod2Fragment2Binding
import com.box.mod.ui.activity.ModActivityPicList
import com.box.mod.ui.adapter.AppletsAdapter
import com.box.mod.ui.adapter.AppletsInfoDiffCallback
import com.box.mod.ui.adapter.AppletPicAdapter
import com.box.other.blankj.utilcode.util.GsonUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar

class Mod2Fragment2 : BaseTitleBarFragment<Mod2Fragment2Model, Mod2Fragment2Binding>() {
    var tabOrientation = LinearLayout.HORIZONTAL
    var appList: MutableList<AppletsInfo> = mutableListOf()
    var appListAdapter = AppletsAdapter(appList)

    var appPicList: MutableList<AppletsInfo> = mutableListOf()
    var appPicAdapter = AppletPicAdapter(appPicList)

    override fun layoutId(): Int = R.layout.mod2_fragment_2

    companion object {
        var isSecondFloor = false
        fun newInstance(): Mod2Fragment2 {
            return Mod2Fragment2()
        }
    }


    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }
        appPicAdapter.setDiffCallback(AppletsInfoDiffCallback())
        appListAdapter.setDiffCallback(AppletsInfoDiffCallback())

        mDataBinding.recyclerView.run {
            layoutManager = object : GridLayoutManager(context, 4) {
                override fun isLayoutRTL(): Boolean {
                    return false
                }
            }.apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position < 2 * 4) 1 else spanCount
                    }
                }
            }
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 5).toInt()))
            adapter = appListAdapter
        }


        mDataBinding.recyclerView2.run {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 2).toInt()))
            adapter = appPicAdapter
        }

        appListAdapter.setOnItemClickListener { adapter, view, position ->
            val appletsInfo = adapter.data[position] as AppletsInfo
            ModActivityPicList.start(appContext, GsonUtils.toJson(appletsInfo))
        }

        appPicAdapter.setOnItemClickListener { adapter, view, position ->
            val appletsInfo = adapter.data[position] as AppletsInfo
            ModActivityPicDown.start(appContext, appletsInfo.pic2)
        }


        mDataBinding.tab.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            mViewModel.isSelect.set(toIndex)
            mViewModel.postDataAppApiByGameIdResul(mDataBinding.tab[toIndex].tag.toString())
        }

    }

    override fun initData() {

    }

    override fun createObserver() {
        mViewModel.postDataAppApi128Result.observe(this) { it ->
            parseState(it, {
                it?.let {
                    appListAdapter.setDiffNewData(it.marketjson.list_data)
                }

            }, {
                Toaster.show(it.errorMsg)
            })
        }

//        appViewModel.appLetsList.observe(this) {
//            mDataBinding.tab.apply {
//                postDelayed({
//                    removeAllViews()
//                    it.forEachIndexed { index, appletsClass ->
//                        @StyleRes val styleResId = R.style._Dsl_TabCommonItemStyle
//                        val textView = TextView(context, null, 0, styleResId)
//                        textView.text = appletsClass.title
//                        textView.tag = index
//                        textView.ellipsize = TextUtils.TruncateAt.END
//                        textView.gravity = Gravity.CENTER
//                        textView.setPadding(20, 30, 20, 30)
//                        addView(textView)
//                    }
//                }, 600)
//            }
//        }
        eventViewModelInstance.setNavigation2Info.observe(this) {
            mViewModel.postDataAppApiByGameIdResul(it.id)
        }
        eventViewModelInstance.setNavigation2InfoIndex.observe(this) {
        }

        eventViewModelInstance.setDefaultGameId.observe(this) {
            mViewModel.postDataAppApiByGameIdResul(it.toString())

        }

        mViewModel.postDataAppApiByGameIdResult.observe(this) { it ->
            parseState(it, {
                it?.let {
                    appPicAdapter.setDiffNewData(it.marketjson.list_data)
                }

            }, {
                Toaster.show(it.errorMsg)
            })
        }


    }

    override fun lazyLoadData() {
        mViewModel.updateConversation()
        mViewModel.postDataAppApi128()
    }

    override fun onNetworkStateChanged(it: NetState) {
    }


    /**********************************************Click**************************************************/
    inner class ProxyClick {
        fun search() {
        }

        fun searchApplets() {
            Toaster.show("搜索小程序")
        }

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun update(text: CharSequence?) {
            itemView.findViewById<TextView>(RC.id.text_view)?.text = text
        }
    }

}


