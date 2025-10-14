package com.box.mod.ui.fragment.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.box.base.base.fragment.BaseTitleBarFragment
import com.box.base.network.NetState
import com.box.mod.R
import com.box.mod.databinding.ModFragmentMainBinding
import com.box.mod.ui.fragment.MainFragment3
import com.box.mod.ui.fragment.MainFragment4
import com.box.mod.ui.fragment.fragment1.FragmentNavigation1
import com.box.mod.ui.fragment.MainFragment2
import com.box.mod.ui.fragment.MainFragment5

class FragmentModMain : BaseTitleBarFragment<FragmentModMainModel, ModFragmentMainBinding>() {
    override fun layoutId(): Int = R.layout.mod_fragment_main

    private val titles = arrayListOf("1", "2","3", "4", "5")
    val fragments = arrayListOf(
        FragmentNavigation1.newInstance(),
        MainFragment2.newInstance(),
        MainFragment3.newInstance(),
        MainFragment4.newInstance(),
        MainFragment5.newInstance())

    companion object {
        fun newInstance(): FragmentModMain {
            return FragmentModMain()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        activity?.let {
            mDataBinding.mainViewpager.apply {
                adapter = object : FragmentStateAdapter(it) {
                    override fun getItemCount(): Int {
                        return fragments.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return fragments[position]
                    }
                }
            }
            mDataBinding.mainViewpager.isUserInputEnabled = false
        }


    }

    override fun createObserver() {
    }

    override fun lazyLoadData() {
    }

    override fun onNetworkStateChanged(it: NetState) {
    }

    //listOf(resources.getStringArray(R.array.navigation_btn_name).toString())


}