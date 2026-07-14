package com.example.spnew2

import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * Hiệu ứng bấm-lún nhẹ khi chạm (scale 0.95), không chặn sự kiện click.
 * Dùng cho mọi nút bấm cần cảm giác "mềm" hơn là chỉ đổi màu ripple.
 */
fun View.applyPressFeedback() {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN ->
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
        }
        false // không tiêu thụ sự kiện — click listener vẫn hoạt động bình thường
    }
}

/**
 * Hiệu ứng xuất hiện mờ dần + trượt lên nhẹ, dùng cho các section trên
 * trang chủ khi vừa load xong (staggered entrance animation).
 */
fun View.fadeInUp(delayMs: Long = 0, duration: Long = 480) {
    alpha = 0f
    translationY = 36f
    animate()
        .alpha(1f)
        .translationY(0f)
        .setStartDelay(delayMs)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator(1.4f))
        .start()
}

/** Pop nhẹ (phóng to rồi về lại) — dùng khi icon tab được chọn. */
fun View.popScale() {
    animate().scaleX(1.18f).scaleY(1.18f).setDuration(140)
        .withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(160).start()
        }.start()
}
