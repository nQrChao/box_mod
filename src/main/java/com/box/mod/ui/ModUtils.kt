package com.box.mod.ui

import android.content.Context
import com.box.mod.ui.xpop.ModXPopupCenterLiBao
import com.box.other.blankj.utilcode.util.ClipboardUtils
import com.box.other.blankj.utilcode.util.ColorUtils
import com.box.other.hjq.toast.Toaster
import com.box.other.xpopup.XPopup
import com.box.com.R as RC

fun copyCodeX(context: Context, codeText: String) {
    XPopup.Builder(context)
        .dismissOnTouchOutside(false)
        .dismissOnBackPressed(false)
        .isDestroyOnDismiss(true)
        .hasStatusBar(true)
        .isLightStatusBar(true)
        .animationDuration(5)
        .navigationBarColor(ColorUtils.getColor(RC.color.xpop_shadow_color))
        .hasNavigationBar(true)
        .asCustom(
            ModXPopupCenterLiBao(context, codeText, {
            }) {
                ClipboardUtils.copyText(codeText)
                Toaster.show("礼包码已复制")
            })
        .show()
}
