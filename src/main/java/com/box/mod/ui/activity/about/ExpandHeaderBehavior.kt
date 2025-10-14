package com.box.mod.ui.activity.about

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout

/**
 * 最终版自定义 Behavior，实现“下拉时优先展开 AppBarLayout”的效果。
 *
 * 工作原理:
 * 在 NestedScrollView/RecyclerView 接收到滚动事件时，在它自己处理之前 (onNestedPreScroll)，
 * 我们先检查手势是不是向下的。如果是，再检查 AppBarLayout 是不是正处于折叠状态。
 * 如果两个条件都满足，我们就“劫持”这个滚动事件，并强制让 AppBarLayout 的 Behavior 来消费它，
 * 从而实现 AppBarLayout 优先展开的效果。
 */
class ExpandHeaderBehavior(context: Context, attrs: AttributeSet) :
    AppBarLayout.ScrollingViewBehavior(context, attrs) {

    private var isAppBarExpanded = true
    private var isScrollingDown = false

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        // 监控 AppBarLayout 的状态
        val appBar = findAppBarLayout(coordinatorLayout.getDependencies(child))
        if (appBar != null) {
            // 监听 AppBarLayout 的偏移量变化
            appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { layout, verticalOffset ->
                // verticalOffset == 0 表示完全展开
                isAppBarExpanded = (verticalOffset == 0)
            })
        }
        // 我们关心垂直滚动
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View, // 我们应用此 Behavior 的 View (NestedScrollView)
        target: View, // 实际滚动的 View (也是 NestedScrollView)
        dx: Int,
        dy: Int, // dy < 0 是向下滚动, dy > 0 是向上滚动
        consumed: IntArray, // 我们消费了多少滚动距离
        type: Int
    ) {
        // 判断滚动方向
        isScrollingDown = dy < 0

        // 核心逻辑: 当【手指向下滚动】且【AppBar没有完全展开】时，我们优先处理
        if (isScrollingDown && !isAppBarExpanded) {
            // 找到 AppBarLayout 和它的 Behavior
            val appBar = findAppBarLayout(coordinatorLayout.getDependencies(child))
            if (appBar != null) {
                val appBarBehavior = (appBar.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior
                if (appBarBehavior is AppBarLayout.Behavior) {
                    // 将滚动事件委托给 AppBarLayout.Behavior 处理，让它去展开
                    // 这会更新 consumed 数组，告诉 NestedScrollView 我们已经处理了滚动
                    appBarBehavior.onNestedPreScroll(coordinatorLayout, appBar, target, dx, dy, consumed, type)
                }
            }
        }

        // 如果我们的逻辑没有消费滚动事件（比如是向上滚动，或AppBar已展开），
        // 就调用父类的默认实现，保持原有行为。
        if (consumed[1] == 0) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }

    // 兼容 RecyclerView 的快速滑动 (fling)
    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        // 当向下快速滑动且 AppBar 未展开时，我们也优先让 AppBar 处理
        if (isScrollingDown && !isAppBarExpanded) {
            val appBar = findAppBarLayout(coordinatorLayout.getDependencies(child))
            if (appBar != null) {
                val appBarBehavior = (appBar.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior
                if (appBarBehavior is AppBarLayout.Behavior) {
                    return appBarBehavior.onNestedPreFling(coordinatorLayout, appBar, target, velocityX, velocityY)
                }
            }
        }
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    private fun findAppBarLayout(views: List<View>): AppBarLayout? {
        for (view in views) {
            if (view is AppBarLayout) {
                return view
            }
        }
        return null
    }
}