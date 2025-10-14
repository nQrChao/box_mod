package com.chaoji.mod.ui.activity.about
import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

class CustomAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {

    // 覆盖 getBehavior() 方法，返回我们自定义的 Behavior
    override fun getBehavior(): CoordinatorLayout.Behavior<AppBarLayout> {
        return Behavior().apply {
            // 设置一个总是允许拖拽的回调
            setDragCallback(object : Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return true
                }
            })
        }
    }
}