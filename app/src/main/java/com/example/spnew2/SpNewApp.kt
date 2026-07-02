package com.example.spnew2

import android.app.Application
import rikka.shizuku.Shizuku

class SpNewApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Không bắt buộc phải làm gì ở đây cho Shizuku (ShizukuProvider tự
        // chạy khi process được tạo), nhưng ta add listener sớm để tránh
        // miss event "binder received" nếu Shizuku đã sẵn sàng trước khi
        // Activity được tạo.
    }

    companion object {
        const val SHIZUKU_REQUEST_CODE = 1001
    }
}
