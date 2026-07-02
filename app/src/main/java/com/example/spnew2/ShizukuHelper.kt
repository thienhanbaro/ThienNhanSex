package com.example.spnew2

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

private const val TAG = "ShizukuHelper"

/**
 * Helper singleton quản lý toàn bộ vòng đời kết nối Shizuku.
 * Cách dùng:
 *  - Gọi ShizukuHelper.init() trong Activity.onCreate()
 *  - Gọi ShizukuHelper.destroy() trong Activity.onDestroy()
 *  - Lắng nghe [onStateChanged] để cập nhật UI
 */
object ShizukuHelper {

    var onStateChanged: ((connected: Boolean) -> Unit)? = null

    private var shellService: IShellService? = null
    private var binderAlive = false

    // ──────────────────────────────────────────────────────────────
    // Binder lifecycle listeners
    // ──────────────────────────────────────────────────────────────

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")
        binderAlive = true
        if (checkPermission()) bindService()
        onStateChanged?.invoke(isReady())
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder dead")
        binderAlive = false
        shellService = null
        onStateChanged?.invoke(false)
    }

    // ──────────────────────────────────────────────────────────────
    // Permission listener
    // ──────────────────────────────────────────────────────────────

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Shizuku permission granted")
                bindService()
                onStateChanged?.invoke(isReady())
            } else {
                Log.w(TAG, "Shizuku permission denied")
                onStateChanged?.invoke(false)
            }
        }

    // ──────────────────────────────────────────────────────────────
    // UserService connection
    // ──────────────────────────────────────────────────────────────

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if (binder != null && binder.pingBinder()) {
                shellService = IShellService.Stub.asInterface(binder)
                Log.d(TAG, "ShellUserService connected")
                onStateChanged?.invoke(true)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            shellService = null
            Log.d(TAG, "ShellUserService disconnected")
            onStateChanged?.invoke(binderAlive)
        }
    }

    private val userServiceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(
                "com.example.spnew2",
                ShellUserService::class.java.name
            )
        )
            .daemon(false)
            .processNameSuffix("shell_service")
            .debuggable(false)
            .version(1)
    }

    // ──────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────

    fun init() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
    }

    fun destroy() {
        runCatching { Shizuku.unbindUserService(userServiceArgs, serviceConnection, true) }
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        shellService = null
    }

    fun isReady(): Boolean = binderAlive && shellService != null

    fun isBinderAlive(): Boolean = binderAlive

    /**
     * Kiểm tra permission Shizuku hiện tại. Trả về true nếu đã được cấp.
     * Nếu chưa có, tự động yêu cầu.
     */
    fun checkPermission(): Boolean {
        if (!binderAlive) return false
        return try {
            when {
                Shizuku.isPreV11() -> false
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> true
                Shizuku.shouldShowRequestPermissionRationale() -> false
                else -> {
                    Shizuku.requestPermission(SpNewApp.SHIZUKU_REQUEST_CODE)
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkPermission error: ${e.message}")
            false
        }
    }

    fun requestPermissionIfNeeded() {
        if (binderAlive && !checkPermission()) {
            runCatching { Shizuku.requestPermission(SpNewApp.SHIZUKU_REQUEST_CODE) }
        }
    }

    private fun bindService() {
        if (shellService != null) return
        runCatching {
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        }.onFailure {
            Log.e(TAG, "bindUserService failed: ${it.message}")
        }
    }

    /**
     * Chạy một lệnh shell qua UserService.
     * Trả về output (stdout+stderr) hoặc null nếu service chưa sẵn sàng.
     * PHẢI gọi từ coroutine / background thread (không chạy trên Main thread).
     */
    suspend fun exec(command: String): String? = withContext(Dispatchers.IO) {
        val svc = shellService ?: return@withContext null
        return@withContext try {
            svc.exec(command)
        } catch (e: Exception) {
            Log.e(TAG, "exec error for [$command]: ${e.message}")
            null
        }
    }
}
