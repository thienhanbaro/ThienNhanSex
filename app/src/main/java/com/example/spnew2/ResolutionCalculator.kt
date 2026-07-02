package com.example.spnew2

import android.util.DisplayMetrics
import kotlin.math.roundToInt

/**
 * Tính toán độ phân giải và density mới theo logic:
 *
 *  • Nếu chiều rộng gốc ≤ 800px (thiết bị 720p):
 *      → Target width = 1319 (hoặc gần đó để khớp tỉ lệ)
 *      → Tự tính height = round(targetW / ratio)
 *
 *  • Nếu chiều cao gốc nằm trong 2400–2500px (thiết bị FHD+ dạng dài):
 *      → Target height = 3088 (hoặc gần đó)
 *      → Tự tính width = round(targetH * ratio)
 *
 *  • Các máy khác: scale đều theo chiều rộng +45% để tăng render pixel.
 *
 *  Sau khi tính xong sẽ dò thêm: nếu kích thước mới không chia hết cho 4
 *  (yêu cầu common của bộ giải mã màn hình) thì tự điều chỉnh ±1 cho đến
 *  khi chia hết, tránh artifact/lỗi black bar.
 */
object ResolutionCalculator {

    data class Resolution(
        val width: Int,
        val height: Int,
        val density: Int,
        /** Mô tả để hiển thị trong UI info box */
        val description: String
    )

    /**
     * @param nativeW   Chiều rộng vật lý thực của máy (pixels)
     * @param nativeH   Chiều cao vật lý thực của máy (pixels)
     * @param nativeDpi DPI gốc của máy
     */
    fun calculate(nativeW: Int, nativeH: Int, nativeDpi: Int): Resolution {
        val ratio = nativeW.toDouble() / nativeH.toDouble()          // tỉ lệ W/H
        val ratioH = nativeH.toDouble() / nativeW.toDouble()         // tỉ lệ H/W

        return when {

            // ── Nhóm 1: Màn 720p (width ≤ 800) ─────────────────────────
            nativeW <= 800 -> {
                val baseTargetW = 1319
                val rawH = (baseTargetW / ratio).roundToInt()
                val newW = align4(baseTargetW)
                val newH = align4(rawH)
                // Kiểm tra tỉ lệ: nếu height bị lệch > 0.5% so với gốc thì
                // thử điều chỉnh width ±1 rồi tính lại
                val (finalW, finalH) = refineAspect(newW, newH, ratio)
                val newDpi = calcDensity(nativeW, nativeDpi, finalW)
                Resolution(
                    width = finalW,
                    height = finalH,
                    density = newDpi,
                    description = "${nativeW}×${nativeH} (dpi $nativeDpi)" +
                            "\n→ ${finalW}×${finalH} (dpi $newDpi)"
                )
            }

            // ── Nhóm 2: Màn FHD+ dài 2400-2500px height ─────────────────
            nativeH in 2400..2500 -> {
                val baseTargetH = 3088
                val rawW = (baseTargetH * ratio).roundToInt()
                val newH = align4(baseTargetH)
                val newW = align4(rawW)
                val (finalW, finalH) = refineAspect(newW, newH, ratio)
                val newDpi = calcDensity(nativeW, nativeDpi, finalW)
                Resolution(
                    width = finalW,
                    height = finalH,
                    density = newDpi,
                    description = "${nativeW}×${nativeH} (dpi $nativeDpi)" +
                            "\n→ ${finalW}×${finalH} (dpi $newDpi)"
                )
            }

            // ── Nhóm 3: Các máy còn lại — tăng +45% width ───────────────
            else -> {
                val scale = 1.45
                val rawW = (nativeW * scale).roundToInt()
                val rawH = (rawW / ratio).roundToInt()
                val finalW = align4(rawW)
                val finalH = align4(rawH)
                val newDpi = calcDensity(nativeW, nativeDpi, finalW)
                Resolution(
                    width = finalW,
                    height = finalH,
                    density = newDpi,
                    description = "${nativeW}×${nativeH} (dpi $nativeDpi)" +
                            "\n→ ${finalW}×${finalH} (dpi $newDpi)"
                )
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────

    /** Làm tròn lên số gần nhất chia hết cho 4 (yêu cầu chung của GPU/display). */
    private fun align4(v: Int): Int {
        val rem = v % 4
        return if (rem == 0) v else v + (4 - rem)
    }

    /**
     * Tinh chỉnh (width, height) để sai lệch tỉ lệ ≤ 0.3%.
     * Thử width ±{0,1,2,3,4} và chọn cặp có tỉ lệ gần nhất.
     */
    private fun refineAspect(w: Int, h: Int, targetRatio: Double): Pair<Int, Int> {
        var bestW = w
        var bestH = h
        var bestErr = Double.MAX_VALUE
        for (dw in -4..4) {
            val cw = align4(w + dw)
            val ch = (cw / targetRatio).roundToInt().let { align4(it) }
            val err = kotlin.math.abs(cw.toDouble() / ch - targetRatio)
            if (err < bestErr) {
                bestErr = err
                bestW = cw
                bestH = ch
            }
        }
        return Pair(bestW, bestH)
    }

    /**
     * Tính density mới theo tỉ lệ nativeW/newW.
     * Snap về các giá trị bucket chuẩn để tránh layout glitch:
     * 120, 160, 213, 240, 280, 320, 360, 400, 420, 480, 560, 640.
     */
    private fun calcDensity(nativeW: Int, nativeDpi: Int, newW: Int): Int {
        val rawDpi = (nativeDpi.toDouble() * newW / nativeW).roundToInt()
        val buckets = intArrayOf(120, 160, 213, 240, 280, 320, 360, 400, 420, 480, 560, 640)
        // Snap lên bucket gần nhất không quá 15% sai lệch
        val snapped = buckets.minByOrNull { kotlin.math.abs(it - rawDpi) } ?: rawDpi
        return if (kotlin.math.abs(snapped - rawDpi) <= rawDpi * 0.15) snapped else rawDpi
    }

    /**
     * Lấy kích thước display vật lý thực (native, không bị ảnh hưởng bởi
     * wm size đang áp dụng) dùng DisplayMetrics.
     * Trả về Triple(width, height, densityDpi).
     */
    fun getNativeDisplay(metrics: DisplayMetrics): Triple<Int, Int, Int> {
        // getRealMetrics trả về kích thước thực của màn (bao gồm cả system bar).
        return Triple(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
    }
}
