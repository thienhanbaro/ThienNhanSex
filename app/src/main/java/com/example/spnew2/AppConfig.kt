package com.example.spnew2

/**
 * Cấu hình toàn cục của app.
 *
 * OPTIMIZATION_LOCKED = true  → G-BOOST+ / SUPPORT NEW² / Factory Reset
 * bị khoá tạm thời trong khi tập trung nâng cấp giao diện. Toàn bộ logic
 * thực thi lệnh Shizuku vẫn còn nguyên trong DeviceFragment/PerformanceCommands,
 * chỉ cần đổi flag này về false để mở lại — không cần viết lại gì thêm.
 */
object AppConfig {
    const val OPTIMIZATION_LOCKED = true
}
