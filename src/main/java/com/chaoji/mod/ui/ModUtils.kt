package com.chaoji.mod.ui

import android.content.Context
import com.chaoji.mod.R
import com.chaoji.mod.ui.xpop.ModXPopupCenterLiBao
import com.chaoji.other.blankj.utilcode.util.ClipboardUtils
import com.chaoji.other.blankj.utilcode.util.ColorUtils
import com.chaoji.other.hjq.toast.Toaster
import com.chaoji.other.xpopup.XPopup
import com.chaoji.common.R as RC


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
