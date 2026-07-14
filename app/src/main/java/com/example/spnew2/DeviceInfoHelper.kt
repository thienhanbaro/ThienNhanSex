package com.example.spnew2

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Calendar

/**
 * Thu thập thông tin thiết bị thật (model, Android version, Vulkan, RAM...).
 * Nhãn hiển thị lấy từ string resource nên tự đổi theo ngôn ngữ đang chọn.
 * Các hàm gọi network/IO (getIpAddress) phải chạy trên background thread.
 */
object DeviceInfoHelper {

    fun getModel(): String =
        "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    fun getAndroid(): String =
        "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    fun getCpuAbi(): String =
        Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"

    fun getCoreCount(): Int = Runtime.getRuntime().availableProcessors()

    fun getRam(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mem)
        val totalGb = "%.1f".format(mem.totalMem / 1_073_741_824.0)
        val availGb = "%.1f".format(mem.availMem / 1_073_741_824.0)
        return "$availGb / $totalGb GB"
    }

    fun getScreen(context: Context): String {
        val dm = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(dm)
        return "${dm.widthPixels}×${dm.heightPixels} px • ${dm.densityDpi} DPI"
    }

    fun getBattery(context: Context): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return "N/A"
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else -1
        val charging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
            BatteryManager.BATTERY_STATUS_CHARGING
        return if (pct >= 0) "$pct%${if (charging) " ⚡" else ""}" else "N/A"
    }

    /** Kiểm tra hỗ trợ Vulkan thật của GPU, kèm version nếu decode được. */
    fun getVulkanInfo(context: Context, yesVerFmt: String, yesPlain: String, noStr: String): String {
        val pm = context.packageManager
        val supported = pm.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
        if (!supported) return noStr
        val feature = pm.systemAvailableFeatures.firstOrNull {
            it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION
        }
        val version = feature?.version ?: 0
        if (version <= 0) return yesPlain
        val major = version shr 22
        val minor = (version shr 12) and 0x3FF
        return yesVerFmt.format("$major.$minor")
    }

    /** Phải gọi từ background thread (network interface lookup). */
    fun getIpAddress(): String {
        return try {
            val ifaces = NetworkInterface.getNetworkInterfaces() ?: return "N/A"
            for (iface in ifaces) {
                if (!iface.isUp || iface.isLoopback) continue
                for (addr in iface.inetAddresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: continue
                    }
                }
            }
            "N/A"
        } catch (e: Exception) { "N/A" }
    }

    fun getDate(): String {
        val c = Calendar.getInstance()
        val d = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val m = (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        return "$d/$m/${c.get(Calendar.YEAR)}"
    }

    /** Dữ liệu gọn cho dải trạng thái thiết bị ở đầu trang chủ. */
    data class QuickStatus(
        val model: String,
        val android: String,
        val vulkan: String,
        val cores: String
    )

    fun buildQuickStatus(context: Context): QuickStatus {
        val vulkan = getVulkanInfo(
            context,
            context.getString(R.string.vulkan_yes_ver),
            context.getString(R.string.vulkan_yes),
            context.getString(R.string.vulkan_no)
        )
        return QuickStatus(
            model = getModel(),
            android = getAndroid(),
            vulkan = vulkan,
            cores = "${getCoreCount()} ${context.getString(R.string.label_cores)}"
        )
    }
}
