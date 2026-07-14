package com.example.spnew2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.spnew2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment    by lazy { HomeFragment() }
    private val deviceFragment  by lazy { DeviceFragment() }
    private val accountFragment by lazy {
        PlaceholderFragment.newInstance("👤", getString(R.string.nav_account), getString(R.string.placeholder_desc))
    }
    private val settingsFragment by lazy { SettingsFragment() }

    private var activeFragment: Fragment = homeFragment
    private val fragmentById = mutableMapOf<String, Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        PrefsHelper.applySaved(this)
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        applyNavInsets()
        ShizukuHelper.init()
    }

    override fun onDestroy() {
        super.onDestroy()
        ShizukuHelper.destroy()
    }

    private fun applyNavInsets() {
        val baseMarginPx = (18 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val lp = binding.bottomNav.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            lp.bottomMargin = baseMarginPx + bottom
            binding.bottomNav.layoutParams = lp
            insets
        }
    }

    private fun setupNavigation() {
        fragmentById["home"]     = homeFragment
        fragmentById["device"]   = deviceFragment
        fragmentById["account"]  = accountFragment
        fragmentById["settings"] = settingsFragment

        val fm = supportFragmentManager
        fm.beginTransaction().apply {
            add(R.id.fragmentContainer, homeFragment,    "home")
            add(R.id.fragmentContainer, deviceFragment,  "device")
            add(R.id.fragmentContainer, accountFragment, "account")
            add(R.id.fragmentContainer, settingsFragment,"settings")
            hide(deviceFragment); hide(accountFragment); hide(settingsFragment)
        }.commit()

        binding.bottomNav.setItems(
            listOf(
                GlassBottomNavView.NavItem("home",     R.drawable.ic_nav_home,     getString(R.string.nav_home)),
                GlassBottomNavView.NavItem("device",   R.drawable.ic_nav_device,   getString(R.string.nav_device)),
                GlassBottomNavView.NavItem("account",  R.drawable.ic_nav_account,  getString(R.string.nav_account)),
                GlassBottomNavView.NavItem("settings", R.drawable.ic_nav_settings, getString(R.string.nav_settings)),
            ),
            defaultSelectedId = "home"
        )

        binding.bottomNav.onItemSelected = { id ->
            val target = fragmentById[id]
            if (target != null && target != activeFragment) {
                switchTo(target)
            }
        }
    }

    /** Chuyển fragment kèm hiệu ứng fade mượt (thay vì cắt cứng). */
    private fun switchTo(target: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .hide(activeFragment)
            .show(target)
            .commit()
        activeFragment = target
    }

    /** Gọi từ HomeFragment (nút CTA, hamburger→Settings) để chuyển tab theo id. */
    fun navigateTo(id: String) {
        binding.bottomNav.select(id)
        fragmentById[id]?.let { target ->
            if (target != activeFragment) switchTo(target)
        }
    }
}
