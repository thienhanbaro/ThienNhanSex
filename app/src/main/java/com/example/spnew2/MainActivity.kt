package com.example.spnew2

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.spnew2.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var island: DynamicIslandView

    private var gBoostEnabled  = false
    private var supportEnabled = false
    private var isRunning      = false

    // ──────────────────────────────────────────────────────────────
    // ASCII art "TNhan" — 7 dòng × 7 màu cầu vồng
    // ──────────────────────────────────────────────────────────────
    private val ASCII_ROWS = arrayOf(
        "█████████  ██     ██  ██                             ",
        "    █      ███    ██  ██       ██████    ███████      ",
        "    █      ████   ██  ██      ██    ██   ██    ██     ",
        "    █      ██ ██  ██  ███████ ████████   ██    ██     ",
        "    █      ██  ██ ██  ██   ██ ██    ██   ██    ██     ",
        "    █      ██   ████  ██   ██ ██    ██   ██    ██     ",
        "    █      ██    ███  ██   ██  ██████     ███████     "
    )
    private val ASCII_COLORS = intArrayOf(
        0xFFFF2200.toInt(),
        0xFFFF8800.toInt(),
        0xFFFFE000.toInt(),
        0xFF00FF44.toInt(),
        0xFF00CCFF.toInt(),
        0xFF4466FF.toInt(),
        0xFFBB22FF.toInt()
    )

    // ──────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        island = DynamicIslandView.attachTo(binding.rootOverlay)

        applyWindowInsets()
        renderAsciiArt()
        initShizuku()
        setupToggles()
        setupButtons()
        loadDeviceInfoAsync()
    }

    override fun onDestroy() {
        super.onDestroy()
        ShizukuHelper.destroy()
    }

    // ──────────────────────────────────────────────────────────────
    // Insets: chỉ status bar spacer thay đổi chiều cao
    // ──────────────────────────────────────────────────────────────

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.statusBarSpacer) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val lp = view.layoutParams
            lp.height = top
            view.layoutParams = lp
            // cũng đẩy island xuống để không nằm dưới status bar
            val islandLp = island.layoutParams as android.widget.FrameLayout.LayoutParams
            islandLp.topMargin = top + (8 * resources.displayMetrics.density).toInt()
            island.layoutParams = islandLp
            insets
        }
    }

    // ──────────────────────────────────────────────────────────────
    // ASCII art render
    // ──────────────────────────────────────────────────────────────

    private fun renderAsciiArt() {
        val sb = SpannableStringBuilder()
        ASCII_ROWS.forEachIndexed { i, row ->
            val start = sb.length
            sb.append(row)
            sb.setSpan(
                ForegroundColorSpan(ASCII_COLORS[i]),
                start, sb.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (i < ASCII_ROWS.size - 1) sb.append("\n")
        }
        binding.tvAsciiArt.text = sb
    }

    // ──────────────────────────────────────────────────────────────
    // Device info (chạy background thread)
    // ──────────────────────────────────────────────────────────────

    private fun loadDeviceInfoAsync() {
        binding.tvDeviceInfo.text = "► Loading device info..."
        lifecycleScope.launch {
            val info   = withContext(Dispatchers.IO) { DeviceInfoHelper.buildInfoBlock(this@MainActivity) }
            val welcome= withContext(Dispatchers.IO) { DeviceInfoHelper.buildIslandWelcome(this@MainActivity) }

            withContext(Dispatchers.Main) {
                binding.tvDeviceInfo.text = info
                island.show(
                    title = getString(R.string.island_welcome_title),
                    body  = welcome,
                    autoHideMs = 7000
                )
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Shizuku
    // ──────────────────────────────────────────────────────────────

    private fun initShizuku() {
        ShizukuHelper.onStateChanged = { connected ->
            runOnUiThread { updateShizukuUi(connected) }
        }
        ShizukuHelper.init()
        updateShizukuUi(ShizukuHelper.isReady())
    }

    private fun updateShizukuUi(connected: Boolean) {
        binding.tvShizukuStatus.text = when {
            connected                    -> getString(R.string.shizuku_connected)
            ShizukuHelper.isBinderAlive()-> getString(R.string.shizuku_permission_needed)
            else                         -> getString(R.string.shizuku_disconnected)
        }
        binding.dotShizukuStatus.setBackgroundResource(
            if (connected) R.drawable.dot_status_on else R.drawable.dot_status_off
        )
        binding.tvShizukuHint.visibility = if (!connected) View.VISIBLE else View.GONE
        binding.switchGBoost.isEnabled   = connected
        binding.switchSupport.isEnabled  = connected
    }

    // ──────────────────────────────────────────────────────────────
    // Toggles
    // ──────────────────────────────────────────────────────────────

    private fun setupToggles() {
        binding.switchGBoost.isEnabled  = false
        binding.switchSupport.isEnabled = false

        binding.switchGBoost.setOnCheckedChangeListener { _, checked ->
            if (!guardReady()) { binding.switchGBoost.isChecked = gBoostEnabled; return@setOnCheckedChangeListener }
            if (checked == gBoostEnabled) return@setOnCheckedChangeListener
            toggleGBoost(checked)
        }
        binding.switchSupport.setOnCheckedChangeListener { _, checked ->
            if (!guardReady()) { binding.switchSupport.isChecked = supportEnabled; return@setOnCheckedChangeListener }
            if (checked == supportEnabled) return@setOnCheckedChangeListener
            toggleSupport(checked)
        }
    }

    private fun toggleGBoost(enable: Boolean) {
        if (isRunning) return
        isRunning = true
        showProgress(true)
        updateCardStyle(enable, isGBoost = true)
        binding.boxGBoostLog.visibility = View.VISIBLE

        val commands = if (enable) PerformanceCommands.BOOST_ON else PerformanceCommands.BOOST_OFF

        island.show(
            title = if (enable) "⚡ G-BOOST+ đang khởi động..." else "↺ Đang khôi phục hệ thống...",
            body  = "Chuẩn bị ${commands.size} lệnh...",
            autoHideMs = 0
        )

        lifecycleScope.launch {
            var allOk = true
            for ((index, pair) in commands.withIndex()) {
                val (cmd, label) = pair
                val step = "[${index + 1}/${commands.size}] $label"
                withContext(Dispatchers.Main) {
                    binding.tvGBoostLog.text = step
                    island.updateBody(step)
                }
                if (ShizukuHelper.exec(cmd) == null) { allOk = false; break }
                delay(60)
            }
            withContext(Dispatchers.Main) {
                if (allOk) {
                    gBoostEnabled = enable
                    val done = if (enable) "✓ G-BOOST+ đã bật  (${commands.size} lệnh)" else "✓ Hệ thống đã khôi phục"
                    binding.tvGBoostLog.text = done
                    island.updateTitle(done)
                    island.updateBody("Hoàn tất • nhấn để đóng")
                    postDelayed { island.hide() }
                } else {
                    binding.switchGBoost.isChecked = !enable
                    updateCardStyle(!enable, isGBoost = true)
                    binding.boxGBoostLog.visibility = View.GONE
                    island.show(title = "✗ Lỗi Shizuku", body = "Kiểm tra lại quyền Shizuku", autoHideMs = 3500)
                }
                showProgress(false); isRunning = false
            }
        }
    }

    private fun toggleSupport(enable: Boolean) {
        if (isRunning) return
        isRunning = true
        showProgress(true)
        updateCardStyle(enable, isGBoost = false)

        lifecycleScope.launch {
            var allOk = true
            if (enable) {
                val dm = DisplayMetrics()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(dm)
                val (nW, nH, nDpi) = ResolutionCalculator.getNativeDisplay(dm)
                val res = ResolutionCalculator.calculate(nW, nH, nDpi)
                withContext(Dispatchers.Main) {
                    binding.tvResolutionDetail.text = res.description
                    binding.boxResolutionInfo.visibility = View.VISIBLE
                    island.show(title = "🖥 Đang tối ưu màn hình...", body = res.description, autoHideMs = 0)
                }
                for (cmd in PerformanceCommands.resolutionOn(res.width, res.height, res.density)) {
                    if (ShizukuHelper.exec(cmd) == null) { allOk = false; break }
                    delay(200)
                }
            } else {
                for (cmd in PerformanceCommands.RESOLUTION_OFF) {
                    if (ShizukuHelper.exec(cmd) == null) { allOk = false; break }
                    delay(200)
                }
                withContext(Dispatchers.Main) { binding.boxResolutionInfo.visibility = View.GONE }
            }
            withContext(Dispatchers.Main) {
                if (allOk) {
                    supportEnabled = enable
                    island.show(title = if (enable) "✓ Màn hình đã tối ưu" else "✓ Độ phân giải đã khôi phục", autoHideMs = 3000)
                } else {
                    binding.switchSupport.isChecked = !enable
                    updateCardStyle(!enable, isGBoost = false)
                    if (!enable) binding.boxResolutionInfo.visibility = View.GONE
                    island.show(title = "✗ Lỗi Shizuku", autoHideMs = 3000)
                }
                showProgress(false); isRunning = false
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Buttons
    // ──────────────────────────────────────────────────────────────

    private fun setupButtons() {
        binding.btnFactoryReset.setOnClickListener {
            if (!guardReady()) return@setOnClickListener
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_factory_reset_title)
                .setMessage(R.string.dialog_factory_reset_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm) { _, _ -> doFactoryReset() }
                .show()
        }
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm) { _, _ -> finishAndRemoveTask() }
                .show()
        }
    }

    private fun doFactoryReset() {
        if (isRunning) return
        isRunning = true
        showProgress(true)
        island.show(title = "↺ Factory Reset đang chạy...", autoHideMs = 0)
        lifecycleScope.launch {
            for (cmd in PerformanceCommands.FACTORY_RESET) {
                ShizukuHelper.exec(cmd); delay(50)
            }
            withContext(Dispatchers.Main) {
                gBoostEnabled = false; supportEnabled = false
                binding.switchGBoost.isChecked = false
                binding.switchSupport.isChecked = false
                binding.boxResolutionInfo.visibility = View.GONE
                binding.boxGBoostLog.visibility = View.GONE
                updateCardStyle(false, true); updateCardStyle(false, false)
                showProgress(false); isRunning = false
                island.show(title = "✓ Đã khôi phục cài đặt gốc", autoHideMs = 3000)
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // UI helpers
    // ──────────────────────────────────────────────────────────────

    private fun updateCardStyle(active: Boolean, isGBoost: Boolean) {
        val card = if (isGBoost) binding.cardGBoost else binding.cardSupport
        card.setBackgroundResource(
            if (active) R.drawable.bg_terminal_row_active else R.drawable.bg_terminal_row
        )
    }

    private fun showProgress(show: Boolean) {
        binding.progressRunning.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun guardReady(): Boolean {
        if (ShizukuHelper.isReady()) return true
        if (ShizukuHelper.isBinderAlive()) {
            ShizukuHelper.requestPermissionIfNeeded()
            island.show(title = "⚠ Cần cấp quyền Shizuku", autoHideMs = 3000)
        } else {
            island.show(title = "⚠ Shizuku chưa chạy", body = "Mở app Shizuku trước khi dùng", autoHideMs = 3500)
        }
        return false
    }

    private fun postDelayed(ms: Long = 2000, action: () -> Unit) {
        binding.root.postDelayed(action, ms)
    }
}
