package com.box.mod.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.box.base.base.action.HandlerAction
import com.box.base.base.activity.BaseModVmDbActivity
import com.box.base.ext.parseModStateWithMsg
import com.box.base.network.NetState
import com.box.common.INTENT_KEY_OUT_IMAGE_LIST
import com.box.common.RESULT_CODE_SELECT_PHOTO
import com.box.common.appViewModel
import com.box.common.data.model.ModMainTabConfig
import com.box.common.eventViewModel
import com.box.common.toBrowser
import com.box.common.ui.view.InfoView
import com.box.common.ui.widget.bottombar.BottomBarItem
import com.box.common.ui.widget.bottombar.BottomBarLayout
import com.box.common.utils.floattoast.XToast
import com.box.common.utils.floattoast.draggable.SpringHideTimeDraggable
import com.box.common.utils.logcat.LogcatDialog
import com.box.mod.R
import com.box.mod.databinding.ModActivityMainBinding
import com.box.mod.ui.fragment.ModFragment1
import com.box.other.blankj.utilcode.util.ActivityUtils
import com.box.other.blankj.utilcode.util.AppUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.blankj.utilcode.util.KeyboardUtils
import com.box.other.blankj.utilcode.util.Logs
import com.box.other.blankj.utilcode.util.ResourceUtils
import com.box.other.blankj.utilcode.util.SizeUtils
import com.box.other.blankj.utilcode.util.TimeUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.xpopup.XPopup
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import kotlin.getValue
import kotlin.jvm.java
import kotlin.system.exitProcess
import com.box.com.R as RC
import androidx.core.graphics.toColorInt


class ModActivityMain : BaseModVmDbActivity<ModActivityMainModel, ModActivityMainBinding>(),HandlerAction {
    override val mViewModel: ModActivityMainModel by viewModels()
    private val mainTabConfig = """
        [
            {
                "title": "首页",
                "normalIcon": ${R.mipmap.mod_navigation_bg1},
                "selectedIcon": ${R.mipmap.mod_navigation_bg1_1},
                "normalIconUrl": "",
                "selectedIconUrl": "",
                "titleNormalColor": "000000",
                "titleSelectedColor": "7C7C7C",
                "fragmentId": 1
            },
            {
                "title": "视频",
                "normalIcon": ${R.mipmap.mod_navigation_bg2},
                "selectedIcon": ${R.mipmap.mod_navigation_bg2_2},
                "normalIconUrl": "",
                "selectedIconUrl": "",
                "titleNormalColor": "000000",
                "titleSelectedColor": "7C7C7C",
                "fragmentId": 2
            },
            {
                "title": "排行榜",
                "normalIcon": ${R.mipmap.mod_navigation_bg3},
                "selectedIcon": ${R.mipmap.mod_navigation_bg3_3},
                "normalIconUrl": "",
                "selectedIconUrl": "",
                "titleNormalColor": "000000",
                "titleSelectedColor": "7C7C7C",
                "fragmentId": 3
            },
            {
                "title": "个人中心",
                "normalIcon": ${R.mipmap.mod_navigation_bg4},
                "selectedIcon": ${R.mipmap.mod_navigation_bg4_4},
                "normalIconUrl": "",
                "selectedIconUrl": "",
                "titleNormalColor": "000000",
                "titleSelectedColor": "7C7C7C",
                "fragmentId": 4
            }
        ]
    """.trimIndent()
    override fun layoutId(): Int = R.layout.mod_activity_main


    // 存储动态生成的 fragments 列表
    private var fragments = arrayListOf<Fragment>()
    private lateinit var bottomBarLayout: BottomBarLayout

    var exitTime = 0L
    private var aniLoading = false
    private var testToast: XToast<XToast<*>>? = null
    private var floatToast: XToast<XToast<*>>? = null
    private var mRotateAnimation: RotateAnimation? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var activity: Activity? = null
        var resultLauncher: ActivityResultLauncher<Intent>? = null
        fun start(context: Context) {
            val intent = Intent(context, ModActivityMain::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ActivityUtils.startActivity(intent)
        }


    }

    override fun initView(savedInstanceState: Bundle?) {
        activity = this

        bottomBarLayout = findViewById(R.id.bbl)
        loadDynamicBottomBar()

//      throw IllegalStateException("are you sb?")
//      initFloatView()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Logs.e(ActivityUtils.getTopActivity().localClassName)
                XPopup.Builder(this@ModActivityMain)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(true)
                    .animationDuration(10)
                    .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
                    .isLightStatusBar(true)
                    .hasNavigationBar(true)
                    .asConfirm(
                        "退出", "确认退出",
                        "取消", "确定",
                        {
                            ActivityUtils.finishAllActivities()
                            exitProcess(0)
                        }, null, false, R.layout.xpopup_confirm_mod
                    ).show()
//                if (System.currentTimeMillis() - exitTime > 2000) {
//                    Toaster.show("再按一次退出程序")
//                    exitTime = System.currentTimeMillis()
//                } else {
//                    finish()
//                }
            }
        })

        mDataBinding.vpContent.apply {
            offscreenPageLimit = 4
            registerOnPageChangeCallback(object : OnPageChangeCallback() {

            })
            adapter = object : FragmentStateAdapter(this@ModActivityMain) {
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
                    postDelayed(Runnable {
                        val tabNotChanged = mDataBinding.bbl.currentItem === currentPosition //是否还停留在当前页签
                        bottomBarItem.setSelectedIcon(R.mipmap.mod_navigation_bg4_4) //更换成首页原来选中图标
                        cancelTabLoading(bottomBarItem)
                        aniLoading = false
                    }, 2000)

                    eventViewModel.getAiChatCount.postValue("count")
                    return@setOnItemSelectedListener
                }
            }

            //如果点击了其他条目
//            val bottomItem: BottomBarItem = mDataBinding.bbl.getBottomItem(3)
//            bottomItem.setSelectedIcon(R.mipmap.mod_navigation_bg3) //更换为原来的图标
//            cancelTabLoading(bottomItem)
            KeyboardUtils.hideSoftInput(this@ModActivityMain)
            mDataBinding.vpContent.setCurrentItem(currentPosition, false)
        }
        postDelayed({
            dismissLoading()
        }, 2000)

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            //处理返回的结果
            val code = result.resultCode
            val data = result.data
            when (code) {
                RESULT_CODE_SELECT_PHOTO -> {
                    data?.extras?.getStringArrayList(INTENT_KEY_OUT_IMAGE_LIST)?.let {
                        try {
                            if (it.isEmpty()) {
                                Toaster.show("未选择")
                            }
                            val iterator: MutableIterator<String> = it.iterator()
                            while (iterator.hasNext()) {
                                if (!File(iterator.next()).isFile) {
                                    iterator.remove()
                                }
                            }
                            Toaster.show(it[0])
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }
        }

        mViewModel.modAuthLogin()
    }

    // 动态加载 BottomBarItem 和 Fragment 的函数
    private fun loadDynamicBottomBar() {
        // 使用 Gson 解析 JSON
        val tabConfigList = Gson().fromJson(mainTabConfig, Array<ModMainTabConfig>::class.java).toList()
        // 清除旧的视图和数据
        fragments.clear()
        bottomBarLayout.removeAllViews()

        for (config in tabConfigList) {
            // 根据配置的 fragmentId 创建对应的 Fragment 实例
            val fragment = when (config.fragmentId) {
                1 -> ModFragment1.newInstance()
                2 -> ModFragment1.newInstance()
                3 -> ModFragment1.newInstance()
                4 -> ModFragment1.newInstance()
                5 -> ModFragment1.newInstance()
                else -> throw IllegalArgumentException("Invalid fragmentId: ${config.fragmentId}")
            }
            fragments.add(fragment)




            // 使用 BottomBarItem.Builder 模式创建并配置 Builder 对象
            val builder = BottomBarItem.Builder(this)
                .normalIcon(ResourceUtils.getDrawable(config.normalIcon))
                .selectedIcon(ResourceUtils.getDrawable(config.selectedIcon))
                .title(config.title)
                .titleTextSize(SizeUtils.px2sp(resources.getDimension(RC.dimen.isp_13)))
                .setTitleNormalColor("#${config.titleNormalColor}".toColorInt())
                .setTitleSelectedColor("#${config.titleSelectedColor}".toColorInt())
                .marginTop(SizeUtils.dp2px(-5f))
                .openTouchBg(true)
                .touchDrawable(ContextCompat.getDrawable(this, RC.drawable.transparent_selector))
                .iconWidth(SizeUtils.dp2px(27f))
                .iconHeight(SizeUtils.dp2px(27f))
            // 创建一个 BottomBarItem 实例
            val item = BottomBarItem(this)
            // 将配置好的 Builder 对象传递给 BottomBarItem 实例的 create() 方法
            item.create(builder)
            bottomBarLayout.addItem(item)
        }

        // --- 遍历已创建的 Item，使用 Glide 异步加载并更新网络图标 ---
        for (i in 0 until bottomBarLayout.childCount) {
            // 获取配置和对应的 Item
            val config = tabConfigList[i]
            val item = bottomBarLayout.getChildAt(i) as? BottomBarItem ?: continue
            // 检查 normalIconUrl 是否有效
            if (config.normalIconUrl.isNotBlank()) {
                Glide.with(this)
                    .load(config.normalIconUrl)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            item.setNormalIcon(resource)
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            // 处理清理逻辑
                        }
                    })
            }
            // 检查 selectedIconUrl 是否有效
            if (config.selectedIconUrl.isNotBlank()) {
                Glide.with(this)
                    .load(config.selectedIconUrl)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            item.setSelectedIcon(resource)
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            // 处理清理逻辑
                        }
                    })
            }
        }


    }


    override fun createObserver() {
        eventViewModel.isLogin.observe(this) {
            

        }
        mViewModel.postModAuthLoginResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }
        mViewModel.userInfoBeanResult.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }

        mViewModel.modUserRealName.observe(this) { resultState ->
            parseModStateWithMsg(resultState,
                onSuccess = { data, msg ->
                    appViewModel.modUserRealName.postValue(data)
                },
                onError = {
                    Toaster.show(it.msg)
                }
            )
        }


        eventViewModel.showLogView.observe(this) {
            showLogView(it)
        }
        eventViewModel.showInfoView.observe(this) {
            showInfoView(it)
        }


        eventViewModel.setMainCurrentItem.observe(this) {
            mDataBinding.bbl.currentItem = it
        }
        eventViewModel.showMainCurrentItem.observe(this) {
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

        eventViewModel.onKickedOffline.observe(this) {

            eventViewModel.isLogin.value = false
            appViewModel.userInfo.value = null
            XPopup.Builder(this@ModActivityMain)
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
                    R.layout.xpopup_confirm_mod
                ).show()
        }

        eventViewModel.onUserTokenExpired.observe(this) {
            mViewModel.loginOut()
        }

        mViewModel.loginOutResult.observe(this) {
            eventViewModel.isLogin.value = false
            appViewModel.userInfo.value = null
            AppUtils.relaunchApp(true)
            ActivityUtils.finishAllActivities()
        }


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
            setContentView(R.layout.item_image_video_preview)
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
                contentView = LogcatDialog(this@ModActivityMain)
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
                contentView = InfoView.getView(this@ModActivityMain)
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