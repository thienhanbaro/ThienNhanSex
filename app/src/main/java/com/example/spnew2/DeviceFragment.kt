package com.example.spnew2

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spnew2.databinding.FragmentDeviceBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tab Thiết bị — cùng ngôn ngữ thị giác thẻ-trắng với tab Trang chủ.
 * Chứa: logo ASCII, thông tin thiết bị chi tiết, trạng thái Shizuku,
 * 2 công tắc tối ưu (tạm khoá theo AppConfig), nút chạy script (rocket,
 * nội dung script sẽ bổ sung sau), Factory Reset, Logout.
 */
class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    private var gBoostEnabled = false
    private var supportEnabled = false
    private var isRunning = false

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
        0xFFE8280A.toInt(), 0xFFE87A00.toInt(), 0xFFCC9A00.toInt(),
        0xFF00A83A.toInt(), 0xFF0095B8.toInt(), 0xFF3355EE.toInt(), 0xFF9A1FE0.toInt()
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyInsets()
        renderAsciiArt()
        setupDeviceInfoRows()
        setupToggles()
        setupButtons()
        setupRocket()
        loadDeviceInfo()
        updateShizukuUi(ShizukuHelper.isReady())
        playEntranceAnimations()

        binding.lockBanner.visibility = if (AppConfig.OPTIMIZATION_LOCKED) View.VISIBLE else View.GONE

        ShizukuHelper.onStateChanged = { connected ->
            activity?.runOnUiThread { updateShizukuUi(connected) }
        }
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.statusBarSpacer) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val lp = v.layoutParams; lp.height = top; v.layoutParams = lp
            insets
        }
    }

    private fun renderAsciiArt() {
        val sb = SpannableStringBuilder()
        ASCII_ROWS.forEachIndexed { i, row ->
            val start = sb.length
            sb.append(row)
            sb.setSpan(ForegroundColorSpan(ASCII_COLORS[i]), start, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (i < ASCII_ROWS.size - 1) sb.append("\n")
        }
        binding.tvAsciiArt.text = sb
    }

    // ──────────────────────────────────────────────────────────────
    // Device info: gán nhãn tĩnh cho 9 dòng, giá trị điền async sau
    // ──────────────────────────────────────────────────────────────

    private fun setupDeviceInfoRows() {
        binding.rowModel.tvLabel.text = getString(R.string.label_model)
        binding.rowAndroid.tvLabel.text = getString(R.string.label_android)
        binding.rowChip.tvLabel.text = getString(R.string.label_chip)
        binding.rowVulkan.tvLabel.text = getString(R.string.label_vulkan)
        binding.rowRam.tvLabel.text = getString(R.string.label_ram)
        binding.rowScreen.tvLabel.text = getString(R.string.label_screen)
        binding.rowBattery.tvLabel.text = getString(R.string.label_battery)
        binding.rowIp.tvLabel.text = getString(R.string.label_ip)
        binding.rowDate.tvLabel.text = getString(R.string.label_date)
        // Dòng cuối không cần đường kẻ dưới
        binding.rowDate.rowDivider.visibility = View.GONE
    }

    private fun loadDeviceInfo() {
        val ctx = requireContext()
        lifecycleScope.launch {
            val model   = DeviceInfoHelper.getModel()
            val android = DeviceInfoHelper.getAndroid()
            val chip    = DeviceInfoHelper.getCpuAbi()
            val vulkan  = DeviceInfoHelper.getVulkanInfo(
                ctx, getString(R.string.vulkan_yes_ver), getString(R.string.vulkan_yes), getString(R.string.vulkan_no)
            )
            val ram     = DeviceInfoHelper.getRam(ctx)
            val screen  = DeviceInfoHelper.getScreen(ctx)
            val battery = DeviceInfoHelper.getBattery(ctx)
            val ip      = withContext(Dispatchers.IO) { DeviceInfoHelper.getIpAddress() }
            val date    = DeviceInfoHelper.getDate()

            if (_binding == null) return@launch
            binding.rowModel.tvValue.text = model
            binding.rowAndroid.tvValue.text = android
            binding.rowChip.tvValue.text = chip
            binding.rowVulkan.tvValue.text = vulkan
            binding.rowRam.tvValue.text = ram
            binding.rowScreen.tvValue.text = screen
            binding.rowBattery.tvValue.text = battery
            binding.rowIp.tvValue.text = ip
            binding.rowDate.tvValue.text = date
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Shizuku status
    // ──────────────────────────────────────────────────────────────

    private fun updateShizukuUi(connected: Boolean) {
        if (_binding == null) return
        binding.tvShizukuStatus.text = when {
            connected -> getString(R.string.shizuku_connected)
            ShizukuHelper.isBinderAlive() -> getString(R.string.shizuku_permission_needed)
            else -> getString(R.string.shizuku_disconnected)
        }
        binding.dotShizukuStatus.setBackgroundResource(
            if (connected) R.drawable.dot_status_on else R.drawable.dot_status_off
        )
        binding.tvShizukuHint.visibility = if (!connected) View.VISIBLE else View.GONE

        if (!AppConfig.OPTIMIZATION_LOCKED) {
            binding.switchGBoost.isEnabled = connected
            binding.switchSupport.isEnabled = connected
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Toggles
    // ──────────────────────────────────────────────────────────────

    private fun setupToggles() {
        binding.switchGBoost.isEnabled = false
        binding.switchSupport.isEnabled = false

        if (AppConfig.OPTIMIZATION_LOCKED) {
            binding.cardGBoost.setOnClickListener { toast(R.string.lock_toast) }
            binding.cardSupport.setOnClickListener { toast(R.string.lock_toast) }
            return
        }

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

        lifecycleScope.launch {
            var allOk = true
            for ((index, pair) in commands.withIndex()) {
                val (cmd, label) = pair
                withContext(Dispatchers.Main) {
                    binding.tvGBoostLog.text = "[${index + 1}/${commands.size}] $label"
                }
                if (ShizukuHelper.exec(cmd) == null) { allOk = false; break }
                delay(60)
            }
            withContext(Dispatchers.Main) {
                if (allOk) {
                    gBoostEnabled = enable
                    binding.tvGBoostLog.text = if (enable) "✓ Hoàn tất ${commands.size} lệnh" else "✓ Đã khôi phục"
                } else {
                    binding.switchGBoost.isChecked = !enable
                    updateCardStyle(!enable, isGBoost = true)
                    binding.boxGBoostLog.visibility = View.GONE
                    toast(R.string.toast_need_shizuku)
                }
                showProgress(false); isRunning = false
            }
        }
    }

    /**
     * Thuật toán mới: LUÔN reset về gốc trước (bắt buộc, tránh cộng dồn sai
     * số nếu máy đã có custom wm size từ trước). Nếu bật, đọc lại độ phân
     * giải gốc THẬT (sau reset) rồi nhân 1.8 cho cả width/height/density.
     */
    private fun toggleSupport(enable: Boolean) {
        if (isRunning) return
        isRunning = true
        showProgress(true)
        updateCardStyle(enable, isGBoost = false)

        lifecycleScope.launch {
            var allOk = true

            // Bước bắt buộc: reset về gốc trước, áp dụng cho cả bật lẫn tắt
            for (cmd in PerformanceCommands.RESOLUTION_OFF) {
                if (ShizukuHelper.exec(cmd) == null) { allOk = false; break }
                delay(200)
            }

            if (allOk && enable) {
                delay(400) // đợi reset lan truyền tới WindowManager trước khi đọc lại

                val dm = DisplayMetrics()
                @Suppress("DEPRECATION")
                activity?.windowManager?.defaultDisplay?.getRealMetrics(dm)
                val (nW, nH, nDpi) = ResolutionCalculator.getNativeDisplay(dm)
                val res = ResolutionCalculator.calculateFromNative(nW, nH, nDpi)

                withContext(Dispatchers.Main) {
                    binding.tvResolutionDetail.text = res.description
                    binding.boxResolutionInfo.visibility = View.VISIBLE
                }

                for (cmd in PerformanceCommands.resolutionOn(res.width, res.height, res.density)) {
                    if (ShizukuHelper.exec(cmd) == null) { allOk = false; break }
                    delay(200)
                }
            } else if (!enable) {
                withContext(Dispatchers.Main) { binding.boxResolutionInfo.visibility = View.GONE }
            }

            withContext(Dispatchers.Main) {
                if (allOk) {
                    supportEnabled = enable
                } else {
                    binding.switchSupport.isChecked = !enable
                    updateCardStyle(!enable, isGBoost = false)
                    if (!enable) binding.boxResolutionInfo.visibility = View.GONE
                    toast(R.string.toast_need_shizuku)
                }
                showProgress(false); isRunning = false
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Rocket: chạy script tối ưu (nội dung script sẽ thêm ở bản sau)
    // ──────────────────────────────────────────────────────────────

    private fun setupRocket() {
        binding.cardRocket.applyPressFeedback()
        binding.cardRocket.setOnClickListener {
            toast(R.string.rocket_coming_soon)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Buttons
    // ──────────────────────────────────────────────────────────────

    private fun setupButtons() {
        binding.btnFactoryReset.setOnClickListener {
            if (AppConfig.OPTIMIZATION_LOCKED) { toast(R.string.lock_toast); return@setOnClickListener }
            if (!guardReady()) return@setOnClickListener
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_factory_reset_title)
                .setMessage(R.string.dialog_factory_reset_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm) { _, _ -> doFactoryReset() }
                .show()
        }
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm) { _, _ -> activity?.finishAndRemoveTask() }
                .show()
        }
    }

    private fun doFactoryReset() {
        if (isRunning) return
        isRunning = true; showProgress(true)
        lifecycleScope.launch {
            for (cmd in PerformanceCommands.FACTORY_RESET) { ShizukuHelper.exec(cmd); delay(50) }
            withContext(Dispatchers.Main) {
                gBoostEnabled = false; supportEnabled = false
                binding.switchGBoost.isChecked = false
                binding.switchSupport.isChecked = false
                binding.boxResolutionInfo.visibility = View.GONE
                binding.boxGBoostLog.visibility = View.GONE
                updateCardStyle(false, true); updateCardStyle(false, false)
                showProgress(false); isRunning = false
                toast("✓ Đã khôi phục cài đặt gốc")
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Animation & UI helpers
    // ──────────────────────────────────────────────────────────────

    private fun playEntranceAnimations() {
        binding.cardAsciiLogo.fadeInUp(delayMs = 40)
        binding.cardDeviceInfo.fadeInUp(delayMs = 100)
        binding.cardShizuku.fadeInUp(delayMs = 160)
    }

    private fun updateCardStyle(active: Boolean, isGBoost: Boolean) {
        val card = if (isGBoost) binding.cardGBoost else binding.cardSupport
        card.setBackgroundResource(if (active) R.drawable.bg_terminal_row_active else R.drawable.bg_terminal_row)
    }

    private fun showProgress(show: Boolean) {
        binding.progressRunning.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun guardReady(): Boolean {
        if (ShizukuHelper.isReady()) return true
        if (ShizukuHelper.isBinderAlive()) ShizukuHelper.requestPermissionIfNeeded()
        toast(R.string.toast_need_shizuku)
        return false
    }

    private fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    private fun toast(resId: Int) = Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
