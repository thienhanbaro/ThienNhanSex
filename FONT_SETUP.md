HƯỚNG DẪN THÊM FONT AWESOME
============================

1. Tải file "fa-solid-900.ttf" (Font Awesome 6 Free - Solid) tại:
   https://fontawesome.com/download

2. Đổi tên file thành: fa_solid_900.ttf
   (chỉ chữ thường, số, gạch dưới — đúng quy tắc đặt tên resource Android)

3. Copy file fa_solid_900.ttf vào CHÍNH thư mục này:
   app/src/main/res/font/fa_solid_900.ttf

4. Tạo file font_fa_solid.xml trong thư mục này với nội dung:

   <?xml version="1.0" encoding="utf-8"?>
   <font-family xmlns:android="http://schemas.android.com/apk/res/android">
       <font
           android:fontStyle="normal"
           android:fontWeight="900"
           android:font="@font/fa_solid_900" />
   </font-family>

5. Trong code Kotlin, dùng IconFont.kt (đã có sẵn unicode constants),
   set fontFamily cho TextView:

       textView.typeface = ResourcesCompat.getFont(context, R.font.font_fa_solid)
       textView.text = IconFont.BOLT  // ví dụ icon tia sét

6. Build lại app — icon FontAwesome sẽ hiển thị thay cho vector hiện tại.

Nếu KHÔNG thêm font, app vẫn chạy bình thường với bộ vector icon
tự vẽ có sẵn (ic_bolt, ic_display, ic_refresh, ic_logout...).
