package com.chaoji.mod.ui.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

class CustomAppBarLayoutBehavior @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppBarLayout.Behavior(context, attrs) {

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        // 如果是向下滑动，并且 AppBar 当前已经折叠
        if (dy < 0 && !child.isLiftOnScroll) {
            // 手动设置为展开
            child.setExpanded(true, true)
            // 消费此次事件，避免交由默认处理
            consumed[1] = dy
        } else {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }
}
