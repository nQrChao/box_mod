package com.chaoji.mod.ui.activity.modmain.mod5

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.chaoji.base.base.fragment.BaseTitleBarFragment
import com.chaoji.base.ext.parseState
import com.chaoji.base.network.NetState
import com.chaoji.im.appContext
import com.chaoji.im.sdk.ImSDK.Companion.eventViewModelInstance

import com.chaoji.im.sdk.appViewModel
import com.chaoji.im.ui.adapter.SpacingItemDecorator
import com.chaoji.im.data.model.AppletsInfo
import com.chaoji.other.blankj.utilcode.util.KeyboardUtils
import com.chaoji.other.blankj.utilcode.util.ScreenUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.immersionbar.BarHide
import com.chaoji.other.immersionbar.immersionBar
import com.chaoji.other.scwang.smartrefresh.layout.api.RefreshHeader
import com.chaoji.other.scwang.smartrefresh.layout.api.RefreshLayout
import com.chaoji.other.scwang.smartrefresh.layout.constant.RefreshState
import com.chaoji.other.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import com.chaoji.mod.R
import com.chaoji.mod.databinding.Mod5Fragment2Binding
import com.chaoji.mod.ui.activity.ModActivityPicDown
import com.chaoji.mod.ui.adapter.AppletPicAdapter
import com.chaoji.common.R as RC

class Mod5Fragment2 : BaseTitleBarFragment<Mod5Fragment2Model, Mod5Fragment2Binding>() {
    var tabOrientation = LinearLayout.HORIZONTAL

    var appPicList: MutableList<AppletsInfo> = mutableListOf()
    var appPicAdapter = AppletPicAdapter(appPicList)

    override fun layoutId(): Int = R.layout.mod5_fragment_2

    companion object {
        var isSecondFloor = false
        fun newInstance(): Mod5Fragment2 {
            return Mod5Fragment2()
        }
    }


    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()

        immersionBar {
            statusBarDarkFont(true)
            init()
        }

        mDataBinding.refreshLayout.setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
            override fun onHeaderMoving(
                header: RefreshHeader?,
                isDragging: Boolean,
                percent: Float,
                offset: Int,
                headerHeight: Int,
                maxDragHeight: Int
            ) {
                val screenHeight = ScreenUtils.getScreenHeight()
                val dragRatio = offset.toFloat() / screenHeight
                val alpha = 1.2f - dragRatio
                mDataBinding.centerView.alpha = alpha
                if (activity?.let { KeyboardUtils.isSoftInputVisible(it) } == true) {
                    KeyboardUtils.hideSoftInput(activity)
                }

                if (offset < 5) {
                    immersionBar {
                        hideBar(BarHide.FLAG_SHOW_BAR)
                        statusBarDarkFont(true)
                        init()
                    }
                }

            }

            override fun onStateChanged(
                refreshLayout: RefreshLayout,
                oldState: RefreshState,
                newState: RefreshState
            ) {

            }
        })

        mDataBinding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(SpacingItemDecorator((resources.displayMetrics.density * 2).toInt()))
            adapter = appPicAdapter
        }

        appPicAdapter.setOnItemClickListener { adapter, view, position ->
            val appletsInfo = adapter.data[position] as AppletsInfo
            ModActivityPicDown.start(appContext, appletsInfo.pic2)
        }
        mDataBinding.tab.configTabLayoutConfig {
            tabEnableTextBold = false
            tabEnableIcoGradientColor = false
            tabEnableGradientScale = false
        }
        mDataBinding.tab.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            mViewModel.postDataAppApiByGameIdResul(appViewModel.appLetsList.value?.get(toIndex)!!.id)
        }

    }

    override fun initData() {

    }

    override fun createObserver() {
        appViewModel.appLetsList.observe(this) {
            mDataBinding.tab.apply {
                postDelayed({
                    removeAllViews()
                    it.forEachIndexed { index, appletsClass ->
                        @StyleRes val styleResId = RC.style._Dsl_TabCommonItemStyle
                        val textView = TextView(context, null, 0, styleResId)
                        textView.text = appletsClass.title
                        textView.tag = index
                        textView.ellipsize = TextUtils.TruncateAt.END
                        textView.gravity = Gravity.CENTER
                        textView.setPadding(20, 30, 20, 30)
                        addView(textView)
                    }
                }, 600)
            }
        }
        eventViewModelInstance.setNavigation2Info.observe(this) {
            //Toaster.show(it.title)
            mViewModel.postDataAppApiByGameIdResul(it.id)
        }
        eventViewModelInstance.setNavigation2InfoIndex.observe(this) {
            //Toaster.show(it.title)
            mDataBinding.tab.setCurrentItem(it)
        }
        eventViewModelInstance.setDefaultGameId.observe(this) {
            mViewModel.postDataAppApiByGameIdResul(it.toString())
        }

        mViewModel.postDataAppApiByGameIdResult.observe(this) { it ->
            parseState(it, {
                it?.let {
                    appPicAdapter.setList(it.marketjson.list_data)
                    appPicAdapter.notifyDataSetChanged()
                }

            }, {
                Toaster.show(it.errorMsg)
            })
        }


    }

    override fun lazyLoadData() {
        mViewModel.updateConversation()
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


