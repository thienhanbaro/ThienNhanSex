package com.example.spnew2

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spnew2.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyInsets()
        setupTopBar()
        setupParallax()
        renderCodeSnippet()
        loadDeviceStatus()
        playEntranceAnimations()
        binding.btnCta.setOnClickListener {
            (activity as? MainActivity)?.navigateTo("device")
        }
        binding.btnCta.applyPressFeedback()
    }

    // ──────────────────────────────────────────────────────────────
    // Insets: đẩy hero xuống dưới status bar
    // ──────────────────────────────────────────────────────────────

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.statusBarSpacer) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val lp = v.layoutParams
            lp.height = top
            v.layoutParams = lp
            binding.topBar.setPadding(
                binding.topBar.paddingLeft, top,
                binding.topBar.paddingRight, binding.topBar.paddingBottom
            )
            insets
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Top bar: menu / theme toggle / language toggle
    // ──────────────────────────────────────────────────────────────

    private fun setupTopBar() {
        // Theme/ngôn ngữ đã chuyển vào tab Cài đặt — hamburger giờ dẫn thẳng tới đó.
        binding.btnMenu.setOnClickListener {
            (activity as? MainActivity)?.navigateTo("settings")
        }
        binding.btnMenu.applyPressFeedback()
    }

    // ──────────────────────────────────────────────────────────────
    // Parallax: phone mockups trôi chậm hơn nội dung khi cuộn xuống
    // ──────────────────────────────────────────────────────────────

    private fun setupParallax() {
        binding.scrollRoot.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.phoneMockups.translationY = scrollY * 0.3f
            binding.phoneMockups.alpha = (1f - scrollY / 380f).coerceIn(0f, 1f)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Entrance animation: hero fade-in nhẹ khi vào trang
    // ──────────────────────────────────────────────────────────────

    private fun playEntranceAnimations() {
        binding.heroTextGroup.fadeInUp(delayMs = 80)
        binding.phoneMockups.fadeInUp(delayMs = 160)
    }

    // ──────────────────────────────────────────────────────────────
    // Code preview: tô màu cú pháp thủ công (giống ASCII art ở DeviceFragment)
    // ──────────────────────────────────────────────────────────────

    private fun renderCodeSnippet() {
        val ctx = requireContext()
        val kw      = ContextCompat.getColor(ctx, R.color.code_kw)
        val str     = ContextCompat.getColor(ctx, R.color.code_str)
        val num     = ContextCompat.getColor(ctx, R.color.code_num)
        val comment = ContextCompat.getColor(ctx, R.color.code_comment)
        val text    = ContextCompat.getColor(ctx, R.color.code_text)

        val sb = SpannableStringBuilder()
        fun line(vararg segments: Pair<String, Int>) {
            segments.forEach { (t, c) ->
                val start = sb.length
                sb.append(t)
                sb.setSpan(ForegroundColorSpan(c), start, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            sb.append("\n")
        }

        line("# G-BOOST+ engine — trích đoạn thật" to comment)
        line("settings " to kw, "put global " to text, "window_animation_scale " to str, "0" to num)
        line("settings " to kw, "put global " to text, "app_standby_enabled " to str, "0" to num)
        line("dumpsys " to kw, "deviceidle whitelist " to text, "+com.dts.freefiremax" to str)
        line("cmd " to kw, "package compile -m speed " to text, "com.dts.freefiremax" to str)
        line("wm " to kw, "size reset" to text, "  # bắt buộc trước" to comment)
        line("wm " to kw, "size " to text, "1944x4320" to num, "  # gốc × 1.8" to comment)

        binding.tvCodeSnippet.text = sb
    }

    // ──────────────────────────────────────────────────────────────
    // Device status: thông tin thật, chạy background thread
    // ──────────────────────────────────────────────────────────────

    private fun loadDeviceStatus() {
        val ctx = requireContext()
        lifecycleScope.launch {
            val status = withContext(Dispatchers.IO) { DeviceInfoHelper.buildQuickStatus(ctx) }
            if (_binding == null) return@launch
            binding.tvStatusModel.text = status.model
            binding.tvStatusAndroid.text = status.android
            binding.tvStatusVulkan.text = status.vulkan
            binding.tvStatusCores.text = status.cores
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
