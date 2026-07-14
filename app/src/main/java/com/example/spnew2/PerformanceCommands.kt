package com.example.spnew2

/**
 * Bộ lệnh ADB tối ưu hiệu năng gaming (an toàn, chỉ cần quyền ADB/Shell uid 2000).
 * Mỗi lệnh có mô tả ngắn để hiển thị log realtime trên Dynamic Island.
 */
object PerformanceCommands {

    /**
     * Danh sách package game/app mục tiêu sẽ được:
     *  - Compile lại bằng ART speed-profile (cmd package compile -m speed)
     *  - Đưa vào whitelist deviceidle (không bị Doze giới hạn nền)
     * Sửa danh sách này nếu bạn muốn tối ưu app khác.
     */
    val TARGET_PACKAGES: List<String> = listOf(
        "com.dts.freefiremax",
        "com.dts.freefireth",
        "com.ss.android.ugc.trill"
    )

    val BOOST_ON: List<Pair<String, String>> = buildList {
        add("settings put global activity_manager_constants max_cached_processes=32" to "Tối ưu bộ nhớ tiến trình nền...")
        add("settings put global app_standby_enabled 0" to "Tắt App Standby...")
        add("settings put global background_activity_restrictions_enabled 0" to "Gỡ giới hạn hoạt động nền...")
        add("settings put global sys_free_storage_log_interval 999999999" to "Tắt quét dung lượng định kỳ...")
        add("settings put global fstrim_mandatory_interval 3600000" to "Giảm tần suất dọn ổ đĩa...")
        add("settings put global hidden_api_blacklist_exemptions *" to "Mở khoá Hidden API...")
        add("settings put global wifi_scan_always_enabled 0" to "Tắt quét WiFi nền...")
        add("settings put global wifi_supplicant_scan_interval 300" to "Giãn chu kỳ quét WiFi...")
        add("settings put global network_recommendations_enabled 0" to "Tắt gợi ý mạng...")
        add("settings put global ble_scan_always_enabled 0" to "Tắt quét Bluetooth nền...")
        add("settings put global captive_portal_mode 0" to "Tắt kiểm tra captive portal...")
        add("settings put global window_animation_scale 0.5" to "Giảm hoạt ảnh cửa sổ...")
        add("settings put global transition_animation_scale 0.5" to "Giảm hoạt ảnh chuyển màn hình...")
        add("settings put global animator_duration_scale 0.5" to "Giảm hoạt ảnh hệ thống...")
        add("settings put global debug_layout 0" to "Tắt debug layout...")
        add("settings put global show_touches 0" to "Tắt hiển thị điểm chạm...")
        add("settings put system pointer_speed 7" to "Tăng tốc độ con trỏ/cảm ứng...")
        add("settings put secure long_press_timeout 200" to "Giảm thời gian long-press...")
        add("settings put system touch_exploration_enabled 0" to "Tắt touch exploration...")
        add("settings put secure multitouch_err_threshold 0" to "Tối ưu ngưỡng đa điểm chạm...")
        add("settings put system min_refresh_rate 90.0" to "Đặt tần số quét tối thiểu 90Hz...")
        add("settings put system peak_refresh_rate 120.0" to "Đặt tần số quét tối đa 120Hz...")
        add("settings put secure user_refresh_rate 1" to "Bật chế độ refresh rate cao...")
        add("settings put secure miui_refresh_rate 1" to "Bật refresh rate cao (MIUI)...")
        add("settings put system screen_brightness_mode 0" to "Tắt độ sáng tự động...")
        add("settings put global low_power 0" to "Tắt tiết kiệm pin...")
        add("settings put global automatic_power_save_mode 0" to "Tắt tự động tiết kiệm pin...")
        add("settings put global power_save_mode_trigger_level 0" to "Tắt ngưỡng kích hoạt tiết kiệm pin...")

        for (pkg in TARGET_PACKAGES) {
            add("cmd package compile -m speed $pkg" to "Biên dịch tối ưu tốc độ: $pkg...")
        }
        for (pkg in TARGET_PACKAGES) {
            add("dumpsys deviceidle whitelist +$pkg" to "Thêm $pkg vào whitelist nền...")
        }

        add("settings put global force_allow_on_external 1" to "Cho phép cài app ra bộ nhớ ngoài...")
        add("settings put global enable_ephemeral_apps 0" to "Tắt ứng dụng tức thời...")
        add("settings put secure high_text_contrast_enabled 0" to "Tắt độ tương phản cao...")
        add("settings put global restricted_device_management_disabled 1" to "Tắt giới hạn quản lý thiết bị...")
        add("settings put global package_verifier_enable 0" to "Tắt xác minh gói cài đặt...")
        add("settings put global verifier_verify_adb_installs 0" to "Tắt xác minh cài qua ADB...")
        add("settings put global overlay_display_devices \"\"" to "Xoá thiết bị overlay ảo...")
        add("settings put global zram_enabled 1" to "Bật ZRAM nén bộ nhớ...")
        add("settings put global sys_storage_threshold_percentage 5" to "Giảm ngưỡng cảnh báo dung lượng...")
        add("settings put global sys_storage_threshold_max_bytes 50000000" to "Giảm ngưỡng dung lượng tối đa...")
        add("settings put global user_callback_options 0" to "Tắt callback hệ thống không cần thiết...")
        add("settings put global bluetooth_on 1" to "Đảm bảo Bluetooth sẵn sàng...")
        add("settings put global auto_time 1" to "Đồng bộ giờ tự động...")
        add("settings put global auto_time_zone 1" to "Đồng bộ múi giờ tự động...")
    }

    val BOOST_OFF: List<Pair<String, String>> = buildList {
        add("settings delete global activity_manager_constants" to "Khôi phục cấu hình bộ nhớ...")
        add("settings put global app_standby_enabled 1" to "Bật lại App Standby...")
        add("settings put global background_activity_restrictions_enabled 1" to "Khôi phục giới hạn hoạt động nền...")
        add("settings put global wifi_scan_always_enabled 1" to "Bật lại quét WiFi nền...")
        add("settings delete global window_animation_scale" to "Khôi phục hoạt ảnh cửa sổ...")
        add("settings delete system pointer_speed" to "Khôi phục tốc độ con trỏ...")
        add("settings put global wifi_supplicant_scan_interval 15" to "Khôi phục chu kỳ quét WiFi...")
        add("settings put global network_recommendations_enabled 1" to "Bật lại gợi ý mạng...")
        add("settings put global ble_scan_always_enabled 1" to "Bật lại quét Bluetooth nền...")
        add("settings delete secure long_press_timeout" to "Khôi phục thời gian long-press...")
        add("settings delete system min_refresh_rate" to "Khôi phục tần số quét tối thiểu...")
        add("settings delete system peak_refresh_rate" to "Khôi phục tần số quét tối đa...")
        add("settings delete secure user_refresh_rate" to "Khôi phục chế độ refresh rate...")
        add("settings delete secure miui_refresh_rate" to "Khôi phục refresh rate (MIUI)...")
        add("settings put global automatic_power_save_mode 1" to "Bật lại tự động tiết kiệm pin...")

        for (pkg in TARGET_PACKAGES) {
            add("dumpsys deviceidle whitelist -$pkg" to "Gỡ $pkg khỏi whitelist nền...")
        }
    }

    fun resolutionOn(w: Int, h: Int, dpi: Int): List<String> = listOf(
        "wm size ${w}x${h}",
        "wm density $dpi"
    )

    val RESOLUTION_OFF: List<String> = listOf(
        "wm size reset",
        "wm density reset"
    )

    val FACTORY_RESET: List<String> =
        BOOST_OFF.map { it.first } + RESOLUTION_OFF
}
