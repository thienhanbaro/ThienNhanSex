package com.example.spnew2

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Calendar

object DeviceInfoHelper {

    fun getModel(): String =
        "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    fun getAndroid(): String =
        "Android ${Build.VERSION.RELEASE}  (API ${Build.VERSION.SDK_INT})"

    fun getCpuAbi(): String =
        Build.SUPPORTED_ABIS.firstOrNull() ?: Build.CPU_ABI

    fun getBoard(): String = Build.BOARD.uppercase()

    fun getRam(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mem)
        val totalGb  = "%.1f".format(mem.totalMem  / 1_073_741_824.0)
        val availGb  = "%.1f".format(mem.availMem  / 1_073_741_824.0)
        return "$availGb GB khả dụng / $totalGb GB tổng"
    }

    fun getScreen(context: Context): String {
        val dm = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(dm)
        return "${dm.widthPixels} × ${dm.heightPixels} px  •  ${dm.densityDpi} DPI"
    }

    fun getBattery(context: Context): String {
        val intent = context.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return "N/A"
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct   = if (level >= 0 && scale > 0) level * 100 / scale else -1
        val status = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING    -> " ⚡sạc"
            BatteryManager.BATTERY_STATUS_FULL        -> " ✓đầy"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> ""
            else -> ""
        }
        return if (pct >= 0) "$pct%$status" else "N/A"
    }

    /** Phải gọi từ background thread (network call) */
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
        val y = c.get(Calendar.YEAR)
        return "$d | Tháng: $m | Năm: $y"
    }

    /**
     * Build full info block cho hiển thị terminal-style.
     * Gọi từ background thread (vì getIpAddress() block).
     */
    fun buildInfoBlock(context: Context): String {
        val pad = 10   // padding label
        return buildString {
            appendLine("► ${"Model".padEnd(pad)}: ${getModel()}")
            appendLine("► ${"Android".padEnd(pad)}: ${getAndroid()}")
            appendLine("► ${"Chip".padEnd(pad)}: ${getCpuAbi()}")
            appendLine("► ${"RAM".padEnd(pad)}: ${getRam(context)}")
            appendLine("► ${"Màn hình".padEnd(pad)}: ${getScreen(context)}")
            appendLine("► ${"Pin".padEnd(pad)}: ${getBattery(context)}")
            appendLine("► ${"IP".padEnd(pad)}: ${getIpAddress()}")
            append("► ${"Ngày".padEnd(pad)}: ${getDate()}")
        }
    }

    /**
     * Thông tin ngắn gọn cho Dynamic Island welcome.
     * Gọi từ background thread.
     */
    fun buildIslandWelcome(context: Context): String {
        val boostCount = PerformanceCommands.BOOST_ON.size
        val dm = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(dm)
        val screen = "${dm.widthPixels}×${dm.heightPixels}"
        val dpi = dm.densityDpi

        return buildString {
            appendLine("by Le Thien Nhan")
            appendLine("─────────────────────────────")
            appendLine("⚡ $boostCount lệnh ADB tối ưu đã nạp")
            appendLine("🎮 Target: Free Fire  •  TikTok")
            appendLine("📱 ${getAndroid()}")
            appendLine("🖥  $screen px  •  $dpi DPI")
            appendLine("🔋 Pin: ${getBattery(context)}")
            appendLine("─────────────────────────────")
            appendLine("💡 Hiệu quả nhất: 90Hz / 120Hz")
            append("⚠  60Hz: hiệu năng có thể giới hạn")
        }
    }
}
