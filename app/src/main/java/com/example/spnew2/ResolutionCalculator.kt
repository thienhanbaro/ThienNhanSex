package com.example.spnew2

import android.util.DisplayMetrics
import kotlin.math.roundToInt

/**
 * Thuật toán tính độ phân giải mới (thay thế bản snap-theo-ngưỡng cũ):
 *
 *  1. LUÔN reset về độ phân giải + density GỐC của máy trước tiên
 *     (bắt buộc — tránh cộng dồn sai số nếu trước đó đã áp dụng một
 *     lần chỉnh khác, đảm bảo phép nhân sau đây luôn tính từ số gốc thật).
 *  2. Lấy độ phân giải gốc (đọc lại SAU khi reset) nhân đều cho 1.8
 *     cho cả chiều rộng, chiều cao VÀ density theo cùng một tỉ lệ —
 *     giữ tỉ lệ khung hình + tỉ lệ dp-to-px giống hệt máy gốc (không
 *     làm UI hệ thống bị to/nhỏ bất thường), chỉ tăng số điểm ảnh
 *     "khai báo" mà thiết bị báo cáo cho ứng dụng.
 *  3. Làm tròn width/height về bội số của 4 (an toàn cho buffer GPU,
 *     tránh lỗi vỡ hình ở một số driver).
 */
object ResolutionCalculator {

    private const val SCALE_FACTOR = 1.8

    data class Resolution(
        val width: Int,
        val height: Int,
        val density: Int,
        val description: String
    )

    /** @param nativeW/H/Dpi PHẢI là số liệu đọc SAU KHI đã wm reset (số gốc thật). */
    fun calculateFromNative(nativeW: Int, nativeH: Int, nativeDpi: Int): Resolution {
        val targetW = align4((nativeW * SCALE_FACTOR).roundToInt())
        val targetH = align4((nativeH * SCALE_FACTOR).roundToInt())
        val targetDpi = (nativeDpi * SCALE_FACTOR).roundToInt()

        return Resolution(
            width = targetW,
            height = targetH,
            density = targetDpi,
            description = "${nativeW}×${nativeH} (dpi $nativeDpi)\n→ ${targetW}×${targetH} (dpi $targetDpi) • ×$SCALE_FACTOR"
        )
    }

    /** Làm tròn lên bội số gần nhất của 4. */
    private fun align4(v: Int): Int {
        val rem = v % 4
        return if (rem == 0) v else v + (4 - rem)
    }

    /** Đọc kích thước hiển thị hiện tại (gọi NGAY SAU khi đã wm reset để có số gốc thật). */
    fun getNativeDisplay(metrics: DisplayMetrics): Triple<Int, Int, Int> =
        Triple(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
}
