# SP NEW² — Gaming Performance Center

<p align="center">
  <img src="https://img.shields.io/badge/Android-26%2B-green?logo=android" />
  <img src="https://img.shields.io/badge/Kotlin-1.9-blue?logo=kotlin" />
  <img src="https://img.shields.io/badge/Shizuku-13.1.5-red" />
  <img src="https://img.shields.io/badge/Build-GitHub%20Actions-black?logo=github" />
</p>

App tối ưu hiệu năng gaming cho Android sử dụng Shizuku (không cần root).

---

## Tính năng

| Feature | Mô tả |
|---|---|
| **G-BOOST+** | Áp dụng bộ lệnh ADB tối ưu CPU scheduler, memory, WiFi, animation |
| **SUPPORT NEW²** | Tự tính và set độ phân giải + density tối ưu theo từng loại màn hình |
| **Factory Reset** | Hoàn nguyên tất cả về mặc định gốc |

---

## Yêu cầu

- Android 8.0+ (minSdk 26)
- [Shizuku](https://shizuku.rikka.app/) đã cài và đang chạy

---

## Build & Push lên GitHub

```bash
# Bước 1: Init git trong thư mục project (nếu chưa có)
git init
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git

# Bước 2: Commit tất cả code
git add .
git commit -m "Initial commit: SP NEW² v1.0"

# Bước 3: Push lên GitHub — Actions sẽ tự build APK
git push -u origin main
```

Sau khi push, vào tab **Actions** trên GitHub để theo dõi build.
APK debug và release sẽ xuất hiện ở mục **Artifacts** sau khi build xong (~5–8 phút).

---

## Release tự động

Khi push một tag dạng `vX.Y.Z`, Actions sẽ tự tạo GitHub Release và đính kèm APK:

```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## Quyền yêu cầu

```xml
<!-- Hiển thị overlay -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<!-- BroadcastReceiver động (Android 13+) -->
<uses-permission android:name="com.example.spnew2.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" />

<!-- Giao tiếp với Shizuku -->
<uses-permission android:name="moe.shizuku.manager.permission.API_V23" />
```

---

## Cấu trúc thư mục

```
app/src/main/
├── aidl/com/example/spnew2/
│   └── IShellService.aidl       ← Interface AIDL cho UserService
├── java/com/example/spnew2/
│   ├── SpNewApp.kt              ← Application class
│   ├── MainActivity.kt          ← UI + logic chính
│   ├── ShizukuHelper.kt         ← Quản lý kết nối Shizuku
│   ├── ShellUserService.kt      ← UserService chạy lệnh với quyền shell
│   ├── ResolutionCalculator.kt  ← Tính wm size + density tối ưu
│   └── PerformanceCommands.kt   ← Danh sách lệnh ADB
└── res/
    ├── layout/activity_main.xml
    ├── drawable/                ← Icons + backgrounds tùy chỉnh
    └── values/                  ← Colors, strings, themes
```

---

*SP NEW² • 2026*
