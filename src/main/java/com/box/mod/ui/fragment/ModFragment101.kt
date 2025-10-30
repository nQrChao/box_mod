package com.box.mod.ui.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.base.fragment.BaseVmDbFragment
import com.box.base.base.viewmodel.BaseViewModel
import com.box.base.network.NetState
import com.box.common.ui.adapter.ViewPager2FragmentAdapter
import com.box.mod.R
import com.box.mod.databinding.ModFragment101Binding
import com.box.mod.ui.data.ModTabTextBean
import com.box.other.immersionbar.immersionBar


class ModFragment101 : BaseTitleBarFragment<ModFragment101.Model, ModFragment101Binding>() {
    //val titles = listOf("热门游戏赛事", "游戏账号估值", "角色名生成器", "小游戏排行榜")
    private var pagerAdapter: ViewPager2FragmentAdapter<BaseVmDbFragment<*, *>>? = null

    override val mViewModel: Model by viewModels()

    override fun layoutId(): Int = R.layout.mod_fragment_101

    companion object {
        fun newInstance(): ModFragment101 {
            return ModFragment101()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.vm = mViewModel
        mDataBinding.click = ProxyClick()
        immersionBar {
            titleBar(mDataBinding.tab)
            statusBarDarkFont(true)
            init()
        }

        pagerAdapter = ViewPager2FragmentAdapter(this)
        pagerAdapter?.run {
            addFragment(ModFragment1001.newInstance())
            addFragment(ModFragment1002.newInstance())
            addFragment(ModFragment1003.newInstance())
            addFragment(ModFragment1004.newInstance())
        }

        mDataBinding.vpPager.run {
            adapter = pagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    mDataBinding.tab.setCurrentItem(position)
                }
            })
        }
        // 在 ModFragment1.kt 中
        mDataBinding.tab.observeIndexChange { fromIndex, toIndex, reselect, fromUser ->
            mViewModel.titlesBean.value?.run {
                mapIndexed { index, bean ->
                    get(index).select.set(index == toIndex)
                }
            }
            try {
                mDataBinding.tab.getChildAt(toIndex)?.run {
                    setBackgroundResource(R.drawable.bg_item_tab_text_select)
                }

                if (fromIndex != toIndex) {
                    mDataBinding.tab.getChildAt(fromIndex)?.run {
                        setBackgroundResource(R.drawable.bg_item_tab_text_default)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mDataBinding.vpPager.currentItem = toIndex
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
    class Model : BaseViewModel(title = "") {
        val titlesBean: LiveData<List<ModTabTextBean>> = MutableLiveData(
            listOf(
                ModTabTextBean(name = "热门游戏赛事", select = ObservableBoolean(true)),
                ModTabTextBean(name = "游戏账号估值", select = ObservableBoolean(false)),
                ModTabTextBean(name = "角色名生成器", select = ObservableBoolean(false)),
                ModTabTextBean(name = "小游戏排行榜", select = ObservableBoolean(false))
            )
        )
    }
}


