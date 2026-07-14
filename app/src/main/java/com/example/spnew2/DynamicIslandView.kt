package com.example.spnew2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Dynamic Island notification pill — trượt từ trên xuống.
 *
 * FIX "cục cục": class này extends LinearLayout trực tiếp.
 * Background (bo góc, viền cyan) được set lên CHÍNH view này.
 * clipToOutline = true đảm bảo không có vùng thừa lộ ra ngoài.
 * Animate cũng áp dụng lên CHÍNH view này → single smooth pill.
 */
class DynamicIslandView(context: Context) : LinearLayout(context) {

    private val tvTitle: TextView
    private val tvBody: TextView
    private val llExpanded: LinearLayout
    private var hideRunnable: Runnable? = null

    init {
        orientation = VERTICAL
        // Background + clipping = viên thuốc thực sự
        setBackgroundResource(R.drawable.bg_island)
        clipToOutline = true
        elevation = dpF(10)

        val padH = dp(20); val padV = dp(13)
        setPadding(padH, padV, padH, padV)
        minimumWidth = dp(160)

        // inflate <merge> children trực tiếp vào this
        LayoutInflater.from(context).inflate(R.layout.view_dynamic_island, this, true)

        tvTitle  = findViewById(R.id.islandTitle)
        tvBody   = findViewById(R.id.islandBody)
        llExpanded = findViewById(R.id.islandExpandedContent)

        // Trạng thái ẩn ban đầu
        alpha = 0f
        translationY = -dp(120).toFloat()
        scaleX = 0.7f
        scaleY = 0.7f
    }

    // ──────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────

    /** Hiện pill với title, tùy chọn body chi tiết. */
    fun show(title: String, body: String? = null, autoHideMs: Long = 4500) {
        hideRunnable?.let { removeCallbacks(it) }
        tvTitle.text = title
        if (body != null) {
            tvBody.text = body
            llExpanded.visibility = View.VISIBLE
        } else {
            llExpanded.visibility = View.GONE
        }
        // Animate THIS → đảm bảo toàn bộ pill move as one piece
        animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(460)
            .setInterpolator(OvershootInterpolator(1.1f))
            .start()

        if (autoHideMs > 0) {
            hideRunnable = Runnable { hide() }
            postDelayed(hideRunnable, autoHideMs)
        }
    }

    fun updateTitle(title: String) { tvTitle.text = title }

    fun updateBody(body: String) {
        tvBody.text = body
        if (llExpanded.visibility != View.VISIBLE)
            llExpanded.visibility = View.VISIBLE
    }

    fun hide() {
        hideRunnable?.let { removeCallbacks(it) }
        animate()
            .alpha(0f)
            .translationY(-dp(120).toFloat())
            .scaleX(0.7f)
            .scaleY(0.7f)
            .setDuration(340)
            .setInterpolator(android.view.animation.AccelerateInterpolator(1.5f))
            .start()
    }

    // ──────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun dpF(v: Int): Float = v * resources.displayMetrics.density

    companion object {
        /**
         * Tạo và gắn island vào parent FrameLayout (thường là rootOverlay).
         * Margin top tự động tính sau khi insets được áp dụng.
         */
        fun attachTo(parent: ViewGroup): DynamicIslandView {
            val island = DynamicIslandView(parent.context)
            val params = android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
                topMargin = (16 * parent.context.resources.displayMetrics.density).toInt()
            }
            parent.addView(island, params)
            return island
        }
    }
}
