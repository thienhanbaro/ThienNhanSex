package com.example.spnew2

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat

/**
 * Thanh điều hướng nổi kiểu "kính mờ" (glass) — nền bán trong suốt, viền
 * sáng nhẹ mô phỏng cạnh kính, và một viên pill trượt mượt phía sau icon
 * đang chọn (lấy cảm hứng từ Liquid Glass trên iOS 26).
 *
 * Backdrop-blur thật của nội dung cuộn phía sau là không khả thi trên
 * Android nếu không dùng thư viện ngoài; view này dùng nền bán trong suốt
 * + (tùy chọn, API 31+) tự làm mờ lớp nền của chính nó để tăng cảm giác
 * "kính" mà vẫn chạy ổn định ở mọi phiên bản Android từ 8.0 trở lên.
 */
class GlassBottomNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    data class NavItem(val id: String, val iconRes: Int, val label: String)

    private val tabRow: LinearLayout
    private val indicator: View
    private var items: List<NavItem> = emptyList()
    private var selectedIndex = 0
    private var indicatorReady = false
    private val iconViews = mutableListOf<ImageView>()
    private val labelViews = mutableListOf<TextView>()

    var onItemSelected: ((String) -> Unit)? = null

    init {
        clipChildren = false
        clipToPadding = false

        val glassBg = View(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.bg_glass_nav)
        }
        addView(glassBg, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        applySelfBlurIfSupported(glassBg)

        indicator = View(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.bg_glass_indicator)
        }
        addView(
            indicator,
            LayoutParams(0, 0, Gravity.START or Gravity.CENTER_VERTICAL)
        )

        tabRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        addView(tabRow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /** Tự làm mờ lớp nền kính trên API 31+; bỏ qua êm ái trên API thấp hơn. */
    private fun applySelfBlurIfSupported(target: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                target.setRenderEffect(
                    RenderEffect.createBlurEffect(16f, 16f, Shader.TileMode.CLAMP)
                )
            } catch (_: Throwable) {
                // Một số driver GPU cũ có thể không hỗ trợ — bỏ qua, nền vẫn
                // hiển thị bình thường (chỉ mất hiệu ứng mờ, không mất chức năng).
            }
        }
    }

    fun setItems(newItems: List<NavItem>, defaultSelectedId: String? = null) {
        items = newItems
        tabRow.removeAllViews()
        iconViews.clear()
        labelViews.clear()
        indicatorReady = false

        newItems.forEachIndexed { index, item ->
            val tab = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                val outValue = android.util.TypedValue()
                context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackgroundBorderless, outValue, true
                )
                if (outValue.resourceId != 0) setBackgroundResource(outValue.resourceId)
            }

            val icon = ImageView(context).apply {
                setImageResource(item.iconRes)
                layoutParams = LinearLayout.LayoutParams(dp(22), dp(22))
            }
            val label = TextView(context).apply {
                text = item.label
                textSize = 10f
                gravity = Gravity.CENTER
                setPadding(0, dp(3), 0, 0)
            }

            tab.addView(icon)
            tab.addView(label)
            tab.setOnClickListener {
                if (index != selectedIndex) {
                    selectIndex(index, animate = true)
                    onItemSelected?.invoke(item.id)
                }
            }
            tabRow.addView(
                tab,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            )
            iconViews.add(icon)
            labelViews.add(label)
        }

        val initialIndex = defaultSelectedId
            ?.let { id -> newItems.indexOfFirst { it.id == id } }
            ?.takeIf { it >= 0 } ?: 0
        selectedIndex = initialIndex
        updateTint()

        post { moveIndicatorTo(selectedIndex, animate = false) }
    }

    /** Chọn tab theo id một cách lập trình (VD: CTA button trên Home). */
    fun select(id: String) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0 && idx != selectedIndex) {
            selectIndex(idx, animate = true)
        }
    }

    private fun selectIndex(index: Int, animate: Boolean) {
        selectedIndex = index
        updateTint()
        moveIndicatorTo(index, animate)
        iconViews.getOrNull(index)?.popScale()
    }

    private fun updateTint() {
        val selColor = ContextCompat.getColor(context, R.color.bottom_nav_selected)
        val unselColor = ContextCompat.getColor(context, R.color.bottom_nav_unselected)
        iconViews.forEachIndexed { i, iv ->
            ImageViewCompat.setImageTintList(
                iv, ColorStateList.valueOf(if (i == selectedIndex) selColor else unselColor)
            )
        }
        labelViews.forEachIndexed { i, tv ->
            val active = i == selectedIndex
            tv.setTextColor(if (active) selColor else unselColor)
            tv.setTypeface(tv.typeface, if (active) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    private fun moveIndicatorTo(index: Int, animate: Boolean) {
        val tab = tabRow.getChildAt(index) ?: return
        if (tab.width == 0) {
            // Layout chưa xong (VD: gọi ngay sau setItems trước khi đo xong) — thử lại sau.
            post { moveIndicatorTo(index, animate) }
            return
        }

        val inset = dp(6)
        val targetWidth = tab.width - inset * 2
        val targetHeight = height - dp(14)
        val targetX = (tab.left + inset).toFloat()

        val lp = indicator.layoutParams
        if (lp.width != targetWidth || lp.height != targetHeight) {
            lp.width = targetWidth
            lp.height = targetHeight
            indicator.layoutParams = lp
        }

        if (!indicatorReady || !animate) {
            indicator.translationX = targetX
            indicatorReady = true
        } else {
            indicator.animate()
                .translationX(targetX)
                .setDuration(420)
                .setInterpolator(OvershootInterpolator(1.05f))
                .start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && items.isNotEmpty()) {
            post { moveIndicatorTo(selectedIndex, animate = false) }
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
