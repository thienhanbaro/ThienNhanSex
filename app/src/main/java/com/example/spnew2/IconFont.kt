package com.example.spnew2

import android.content.Context
import android.widget.TextView

/**
 * Helper chứa các mã unicode của Font Awesome 6 Free Solid.
 *
 * CÁCH KÍCH HOẠT:
 *  1. Xem hướng dẫn tại /FONT_SETUP.md (gốc project)
 *  2. Sau khi đã có R.font.font_fa_solid, gọi IconFont.apply(textView, IconFont.BOLT)
 *
 * Nếu font chưa được thêm, các hàm trong object này sẽ không được gọi —
 * toàn bộ UI hiện tại dùng vector drawable (ImageView) nên app vẫn chạy
 * bình thường mà không phụ thuộc class này.
 */
object IconFont {

    // Unicode points tiêu chuẩn của Font Awesome 6 Free Solid
    const val BOLT = "\uf0e7"          // fa-bolt (G-BOOST+)
    const val DISPLAY = "\uf108"       // fa-display (SUPPORT NEW²)
    const val ROTATE_LEFT = "\uf2ea"   // fa-rotate-left (Factory Reset)
    const val RIGHT_FROM_BRACKET = "\uf2f5" // fa-right-from-bracket (Logout)
    const val XMARK = "\uf00d"         // fa-xmark (Close)
    const val SHARE_NODES = "\uf1e0"   // fa-share-nodes (Share)
    const val CIRCLE_CHECK = "\uf058"  // fa-circle-check (Notification success)
    const val SHIELD_HALVED = "\uf3ed" // fa-shield-halved
    const val MOBILE_SCREEN = "\uf3cd" // fa-mobile-screen-button
    const val GAUGE_HIGH = "\uf625"    // fa-gauge-high (performance)
    const val WIFI = "\uf1eb"          // fa-wifi
    const val MEMORY = "\uf538"        // fa-memory
    const val TRIANGLE_EXCLAMATION = "\uf071" // fa-triangle-exclamation

    /**
     * Áp dụng font FontAwesome + icon unicode vào 1 TextView.
     * Yêu cầu: đã thêm R.font.font_fa_solid theo hướng dẫn FONT_SETUP.md
     */
    fun apply(textView: TextView, context: Context, fontResId: Int, icon: String) {
        runCatching {
            val typeface = androidx.core.content.res.ResourcesCompat.getFont(context, fontResId)
            textView.typeface = typeface
            textView.text = icon
        }
    }
}
