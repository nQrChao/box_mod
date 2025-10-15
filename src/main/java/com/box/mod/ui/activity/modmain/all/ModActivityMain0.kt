package com.box.mod.ui.activity.modmain.all

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.KeyboardUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.blankj.utilcode.util.TimeUtils
import com.box.base.base.activity.BaseVmDbActivity
import com.box.base.network.NetState
import com.box.common.sdk.ImSDK.Companion.appViewModelInstance
import com.box.common.sdk.ImSDK.Companion.eventViewModelInstance
import com.box.common.sdk.ImSDK.Companion.isCreateMainActivity
import com.box.mod.R
import com.box.com.R as RC
import com.box.common.sdk.appViewModel
import com.box.common.sdk.eventViewModel
import com.box.common.ui.widget.bottombar.BottomBarItem
import com.box.common.utils.MMKVUtil
import com.box.common.toBrowser
import com.box.common.ui.view.InfoView
import com.box.common.ui.widget.bottombar.BottomBarLayout
import com.box.common.utils.floattoast.XToast
import com.box.common.utils.floattoast.draggable.SpringHideTimeDraggable
import com.box.common.utils.logcat.LogcatDialog
import com.box.mod.databinding.ModActivityMain0Binding
import com.box.mod.ui.activity.modmain.mod5.Mod5Fragment1
import com.box.mod.ui.activity.modmain.mod5.Mod5Fragment2
import com.box.mod.ui.activity.modmain.mod5.Mod5Fragment3
import com.box.mod.ui.activity.modmain.mod5.Mod5Fragment4
import com.box.mod.ui.activity.modmain.mod5.Mod5Fragment5
import com.box.other.blankj.utilcode.util.SizeUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.immersionbar.immersionBar
import com.box.other.xpopup.XPopup
import kotlinx.coroutines.launch

class ModActivityMain0 : BaseVmDbActivity<ModActivityMain0Model, ModActivityMain0Binding>() {
    private lateinit var bottomBarLayout: BottomBarLayout
    private lateinit var bottomBarItem1: BottomBarItem
    private lateinit var bottomBarItem2: BottomBarItem
    private lateinit var bottomBarItem3: BottomBarItem
    private lateinit var bottomBarItem4: BottomBarItem
    private lateinit var bottomBarItem5: BottomBarItem

    override fun layoutId(): Int = R.layout.mod_activity_main0

    var exitTime = 0L
    private var aniLoading = false
    private val mHandler = Handler(Looper.getMainLooper())
    private var mRotateAnimation: RotateAnimation? = null

    private var floatToast: XToast<XToast<*>>? = null
    private var testToast: XToast<XToast<*>>? = null

    val fragments = arrayListOf(
        Mod5Fragment1.newInstance(),
        Mod5Fragment2.newInstance(),
        Mod5Fragment3.newInstance(),
        Mod5Fragment4.newInstance(),
        Mod5Fragment5.newInstance()
    )

    companion object {
        @SuppressLint("StaticFieldLeak")
        var activity: Activity? = null
        fun start(context: Context) {
            val intent = Intent(context, ModActivityMain0::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addBottomBarItems(indices: List<Int>) {
        for (i in 0 until bottomBarLayout.childCount) {
            val child = bottomBarLayout.getChildAt(i)
            if (child is BottomBarItem) {
                child.visibility = View.GONE
            }
        }

        // 2. 显示指定的 BottomBarItem
        for (index in indices) {
            if (index < bottomBarLayout.childCount) {
                val bottomBarItem = bottomBarLayout.getChildAt(index) as BottomBarItem
                bottomBarItem.visibility = View.VISIBLE
                // 设置文本或其他属性
                //bottomBarItem.textView.text = "Item ${index + 1}"
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        activity = this
        immersionBar {
            titleBar(mDataBinding.titleBar)
            statusBarDarkFont(true)
            init()
        }
        //initFloatView()
        // 初始化 BottomBarLayout 和 BottomBarItem
        bottomBarLayout = findViewById(R.id.bbl) // 确保你的 BottomBarLayout 的 id 是 bbl

        bottomBarItem1 = bottomBarLayout.getChildAt(0) as BottomBarItem
        bottomBarItem2 = bottomBarLayout.getChildAt(1) as BottomBarItem
        bottomBarItem3 = bottomBarLayout.getChildAt(2) as BottomBarItem
        bottomBarItem4 = bottomBarLayout.getChildAt(3) as BottomBarItem
        bottomBarItem5 = bottomBarLayout.getChildAt(4) as BottomBarItem
        val tabTitles = resources.getStringArray(R.array.navigation_btn_name)
        // 动态设置 BottomBarItem 的名称
        bottomBarItem1.textView.text = tabTitles[0]
        bottomBarItem2.textView.text = tabTitles[1]
        bottomBarItem3.textView.text = tabTitles[2]
        bottomBarItem4.textView.text = tabTitles[3]
        bottomBarItem5.textView.text = tabTitles[4]

        // 动态设置 BottomBarItem 的显示
        addBottomBarItems(listOf(0, 1, 2, 3, 4)) // 传递需要显示的 item 的索引列表

        //throw IllegalStateException("are you sb?")
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Logs.e(ActivityUtils.getTopActivity().localClassName)
                if (System.currentTimeMillis() - exitTime > 2000) {
                    Toaster.show("再按一次退出程序")
                    exitTime = System.currentTimeMillis()
                } else {
                    finish()
                }
            }
        })

        mDataBinding.vpContent.apply {
            offscreenPageLimit = 4
            registerOnPageChangeCallback(object : OnPageChangeCallback() {

            })
            adapter = object : FragmentStateAdapter(this@ModActivityMain0) {
                override fun getItemCount(): Int {
                    return fragments.size
                }

                override fun createFragment(position: Int): Fragment {
                    return fragments[position]
                }
            }
        }

        mDataBinding.vpContent.isUserInputEnabled = false

        mDataBinding.bbl.setOnItemSelectedListener { bottomBarItem, previousPosition, currentPosition ->
            if (currentPosition == 4) {
                if (previousPosition == currentPosition) {
                    if (mRotateAnimation != null && !mRotateAnimation?.hasEnded()!!) {
                        return@setOnItemSelectedListener
                    }
                    //bottomBarItem.setSelectedIcon(R.mipmap.tab_loading)

                    if (mRotateAnimation == null) {
                        mRotateAnimation = RotateAnimation(
                            0f, 360f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                            0.5f
                        )
                        mRotateAnimation?.duration = 800
                        mRotateAnimation?.repeatCount = -1
                    }
                    val bottomImageView = bottomBarItem.imageView
                    bottomImageView.animation = mRotateAnimation
                    bottomImageView.startAnimation(mRotateAnimation) //播放旋转动画
                    aniLoading = true
                    //模拟数据刷新完毕
                    mHandler.postDelayed(Runnable {
                        val tabNotChanged =
                            mDataBinding.bbl.getCurrentItem() === currentPosition //是否还停留在当前页签
                        bottomBarItem.setSelectedIcon(R.mipmap.mod_navigation_bg5) //更换成首页原来选中图标
                        cancelTabLoading(bottomBarItem)
                        aniLoading = false
                    }, 2000)

                    eventViewModel.getAiChatCount.postValue("count")
                    return@setOnItemSelectedListener
                }
            }

            //如果点击了其他条目
            val bottomItem: BottomBarItem = mDataBinding.bbl.getBottomItem(4)
            bottomItem.setSelectedIcon(R.mipmap.mod_navigation_bg5) //更换为原来的图标
            cancelTabLoading(bottomItem)
            KeyboardUtils.hideSoftInput(this@ModActivityMain0)
            mDataBinding.vpContent.setCurrentItem(currentPosition, false)
        }
        postDelayed({
            dismissLoading()
        }, 2000)

//        XPopup.Builder(this@ActivityModMain)
//            .dismissOnTouchOutside(false)
//            .dismissOnBackPressed(false)
//            .isDestroyOnDismiss(true)
//            .hasStatusBar(true)
//            .isLightStatusBar(true)
//            .animationDuration(5)
//            .navigationBarColor(ColorUtils.getColor(R.color.xpop_shadow_color))
//            .hasNavigationBar(true)
//            .asCustom(
//                XPopupCenterProtocol(this@ActivityModMain, R.layout.xpopup_update) {
//
//                })
//            .show()

    }

    //    private fun changeFragment(currentPosition: Int) {
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.fl_content, fragments[currentPosition])
//        transaction.commit()
//    }
    override fun createObserver() {

        eventViewModelInstance.showLogView.observe(this) {
            showLogView(it)
        }
        eventViewModelInstance.showInfoView.observe(this) {
            showInfoView(it)
        }


        eventViewModelInstance.setMainCurrentItem.observe(this) {
            mDataBinding.bbl.currentItem = it
        }
        eventViewModelInstance.showMainCurrentItem.observe(this) {
            if (it) {
                lifecycleScope.launch {
                    mDataBinding.bbl.animate().alpha(1f).setDuration(500).start()
                    //delay(500)
                    mDataBinding.bbl.visibility = View.VISIBLE
                }

            } else {
                lifecycleScope.launch {
                    mDataBinding.bbl.animate().alpha(0f).setDuration(500).start()
                    //delay(500)
                    mDataBinding.bbl.visibility = View.GONE
                }
            }
        }

        eventViewModelInstance.onKickedOffline.observe(this) {
            MMKVUtil.saveModUser(null)
            MMKVUtil.saveJwtToken(null)
            MMKVUtil.saveJwtRefreshToken(null)

            appViewModelInstance.isLogin = false
            appViewModelInstance.userInfo.value = null
            XPopup.Builder(this@ModActivityMain0)
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .isDestroyOnDismiss(true)
                .hasStatusBar(true)
                .animationDuration(10)
                .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                .isLightStatusBar(true)
                .hasNavigationBar(true)
                .asConfirm(
                    "提示",
                    "您的超级AIM账号于" + TimeUtils.getNowString() + "在另一台移动设备上登录。如果不是你的操作，你的密码或信息已泄露。",
                    null,
                    "确定",
                    {
                        AppUtils.relaunchApp(true)
                        ActivityUtils.finishAllActivities()
                    },
                    null,
                    true,
                    RC.layout.xpopup_confirm
                ).show()
        }

        eventViewModelInstance.onUserTokenExpired.observe(this) {
            mViewModel.loginOut()
        }

        mViewModel.loginOutResult.observe(this) {
            MMKVUtil.saveModUser(null)
            MMKVUtil.saveJwtToken(null)
            MMKVUtil.saveJwtRefreshToken(null)
            appViewModelInstance.isLogin = false
            appViewModelInstance.userInfo.value = null
            AppUtils.relaunchApp(true)
            ActivityUtils.finishAllActivities()
        }


        isCreateMainActivity = true
    }

    override fun onNetworkStateChanged(it: NetState) {

    }


    private fun cancelTabLoading(bottomItem: BottomBarItem) {
        if (aniLoading) {
            val animation = bottomItem.imageView.animation
            animation?.cancel()
        }
    }

    /**
     * 悬浮球，BOX初始化
     */
    private fun initFloatView() {
        floatToast = XToast<XToast<*>>(this).apply {
            setContentView(R.layout.item_floatview)
            setGravity(Gravity.START or Gravity.TOP)
            setAnimStyle(RC.style.IOSAnimStyle)
            setYOffset(SizeUtils.dp2px(80f))
            setDraggable(SpringHideTimeDraggable(0.6f, 4000L))
            setOnClickListener { _, _ ->
                appViewModel.appInfo.value.let {
                    if (it != null) {
                        toBrowser(it.marketjson.wechat_url)
                    }
                }
            }
        }
        floatToast?.show()
    }

    private var logToast: XToast<XToast<*>>? = null
    private fun showLogView(show: Boolean) {
        if (show) {
            logToast = XToast<XToast<*>>(this).apply {
                contentView = LogcatDialog(this@ModActivityMain0)
                setGravity(Gravity.END or Gravity.BOTTOM)
                setAnimStyle(RC.style.IOSAnimStyle)
                //setDraggable(MovingDraggable())
            }
            logToast?.show()
        } else {
            logToast?.cancel()
        }
    }

    private fun showInfoView(show: Boolean) {
        if (show) {
            testToast = XToast<XToast<*>>(this).apply {
                contentView = InfoView.getView(this@ModActivityMain0)
                setGravity(Gravity.END or Gravity.TOP)
                setAnimStyle(RC.style.IOSAnimStyle)
                //setDraggable(MovingDraggable())
            }
            testToast?.show()
        } else {
            testToast?.cancel()
        }
    }

}