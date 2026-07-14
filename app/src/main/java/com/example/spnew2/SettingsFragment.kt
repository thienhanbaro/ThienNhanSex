package com.example.spnew2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.spnew2.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyInsets()
        setupAppearancePills()
        setupLanguagePills()

        binding.cardAppearance.fadeInUp(delayMs = 40)
        binding.cardLanguage.fadeInUp(delayMs = 100)
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.statusBarSpacer) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val lp = v.layoutParams; lp.height = top; v.layoutParams = lp
            insets
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Giao diện sáng/tối
    // ──────────────────────────────────────────────────────────────

    private fun setupAppearancePills() {
        val ctx = requireContext()
        updateThemePillStyle(PrefsHelper.isNightMode(ctx))

        binding.pillThemeLight.setOnClickListener {
            updateThemePillStyle(dark = false)
            PrefsHelper.setNightMode(ctx, false) // kích hoạt AppCompat tự recreate với theme mới
        }
        binding.pillThemeDark.setOnClickListener {
            updateThemePillStyle(dark = true)
            PrefsHelper.setNightMode(ctx, true)
        }
        binding.pillThemeLight.applyPressFeedback()
        binding.pillThemeDark.applyPressFeedback()
    }

    private fun updateThemePillStyle(dark: Boolean) {
        binding.pillThemeLight.setBackgroundResource(if (!dark) R.drawable.bg_pill_selected else R.drawable.bg_pill_unselected)
        binding.pillThemeDark.setBackgroundResource(if (dark) R.drawable.bg_pill_selected else R.drawable.bg_pill_unselected)
        binding.pillThemeLight.setTextColor(textColorFor(!dark))
        binding.pillThemeDark.setTextColor(textColorFor(dark))
    }

    // ──────────────────────────────────────────────────────────────
    // Ngôn ngữ
    // ──────────────────────────────────────────────────────────────

    private fun setupLanguagePills() {
        val ctx = requireContext()
        updateLangPillStyle(PrefsHelper.getLocaleTag(ctx) == "en")

        binding.pillLangVi.setOnClickListener {
            updateLangPillStyle(isEnglish = false)
            PrefsHelper.setLocaleTag(ctx, "vi")
        }
        binding.pillLangEn.setOnClickListener {
            updateLangPillStyle(isEnglish = true)
            PrefsHelper.setLocaleTag(ctx, "en")
        }
        binding.pillLangVi.applyPressFeedback()
        binding.pillLangEn.applyPressFeedback()
    }

    private fun updateLangPillStyle(isEnglish: Boolean) {
        binding.pillLangVi.setBackgroundResource(if (!isEnglish) R.drawable.bg_pill_selected else R.drawable.bg_pill_unselected)
        binding.pillLangEn.setBackgroundResource(if (isEnglish) R.drawable.bg_pill_selected else R.drawable.bg_pill_unselected)
        binding.pillLangVi.setTextColor(textColorFor(!isEnglish))
        binding.pillLangEn.setTextColor(textColorFor(isEnglish))
    }

    private fun textColorFor(selected: Boolean): Int =
        androidx.core.content.ContextCompat.getColor(
            requireContext(),
            if (selected) android.R.color.white else R.color.terminal_secondary
        )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
