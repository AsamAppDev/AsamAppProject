# ⚙️ راهنمای کامل راه‌اندازی (Setup Guide)

این مستند شامل تمام مراحل نصب و راه‌اندازی زیرساخت پروژه **آسام‌اَپ (AsamApp)** است — از نصب وردپرس و VPS گرفته تا پیکربندی اندروید و سرویس‌های ابری.

---

## 🧩 مرحله 1: نصب وردپرس (WordPress Setup)

### پیش‌نیازها

| مورد | مقدار پیشنهادی |
|------|----------------|
| **PHP** | نسخه 8.1 یا بالاتر |
| **MySQL / MariaDB** | نسخه 5.7+ یا 10.3+ |
| **وب‌سرور** | Apache یا Nginx |
| **SSL** | فعال |
| **PHP Extensions** | `curl`, `zip`, `gd`, `intl`, `mbstring`, `xml`, `json`, `mysqli` |

---

### مراحل نصب

1. دانلود وردپرس:
   ```bash
   wget https://wordpress.org/latest.zip
   unzip latest.zip
   mv wordpress asamapp
   ```

2. ایجاد دیتابیس:
   ```sql
   CREATE DATABASE asamapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'asam_user'@'localhost' IDENTIFIED BY 'StrongPassword123';
   GRANT ALL PRIVILEGES ON asamapp.* TO 'asam_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. تنظیم فایل پیکربندی:
   ```bash
   cp wp-config-sample.php wp-config.php
   ```
   سپس مشخصات دیتابیس را وارد کنید.

4. تنظیم مجوزها:
   ```bash
   sudo chown -R www-data:www-data /var/www/asamapp
   sudo chmod -R 755 /var/www/asamapp
   ```

5. ورود از طریق مرورگر و نصب رابط گرافیکی:
   ```
   http://yourdomain.com
   ```

---

## 🖥️ مرحله 2: پیکربندی VPS (Server Setup)

### نصب پیش‌نیازها

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install nginx mysql-server php-fpm php-mysql git unzip -y
```

### فعال‌سازی SSL

اگر دامنه داری:
```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d yourdomain.com
```

### بهینه‌سازی اولیه

```bash
sudo ufw allow 'Nginx Full'
sudo systemctl enable nginx
sudo systemctl restart nginx
```

---

## 🤖 مرحله 3: راه‌اندازی چت‌بات هوش مصنوعی (AI Chatbot Server)

**پیش‌نیازها:**
*   نصب پایتون (نسخه ۳.۱۲ یا بالاتر).
*   نصب و راه‌اندازی یک سرور PostgreSQL.

**مراحل نصب و راه‌اندازی:**
1.  **نصب وابستگی‌ها:** در ریشه پروژه، دستور زیر را برای نصب تمام کتابخانه‌های مورد نیاز اجرا کنید:
    ```bash
    pip install -r requirements.txt
    ```

2.  **پیکربندی متغیرهای محیطی:**
    یک فایل با نام .env در ریشه پروژه ایجاد کرده و مقادیر زیر را مطابق با تنظیمات خود در آن قرار دهید. این مقادیر توسط برنامه برای اتصال به سرویس‌های خارجی استفاده می‌شوند.
    ````
    // filepath: \root\projects\chatbot\.env
    # آدرس پایه و کلید API برای مدل زبان
    OPENAI_API_BASE=https://api.avalai.ir/v1/
    OPENAI_API_KEY=YOUR_API_KEY_HERE

    # اطلاعات اتصال به پایگاه داده PostgreSQL
    DATABASE_URL="postgresql://USERNAME:PASSWORD@HOST:PORT/DATABASE_NAME"
    DATABASE_TABLE="chat_history" # نام جدول برای تاریخچه پیام‌های LangChain
    ````

3.  **اجرای برنامه:**
    برای اجرای سرور توسعه، از دستور زیر در ریشه پروژه استفاده کنید:
    ```bash
    uvicorn src.main:app --host 127.0.0.1 --port 8000
    ```
    این دستور برنامه را با استفاده از Uvicorn اجرا می کند. سرور به طور پیش‌فرض روی آدرس `http://127.0.0.1:8000` در دسترس خواهد بود.

---

## 📱 مرحله 4: راه‌اندازی اندروید استودیو

### پیش‌نیازها

- Android Studio آخرین نسخه
- SDK 34+
- JDK 17

### تنظیمات کلیدی پروژه

1. افزودن URL سایت به WebView در `MainActivity.kt`
2. غیر فعال کردن Zoom و Selection
3. نمایش صفحه خطا در صورت قطع اینترنت
4. افزودن Splash Screen و Onboarding

---

##  مرحله 5: اتصال Firebase

1. ورود به [Firebase Console](https://console.firebase.google.com)
2. ساخت پروژه جدید → افزودن اپ اندروید
3. دریافت `google-services.json` و قرار دادن آن در پوشه `app/`
4. فعال کردن سرویس‌ها:
   - Analytics
   - Crashlytics
   - Cloud Messaging (برای نوتیفیکیشن)

در فایل `build.gradle.kts` اضافه کنید:

```kotlin
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
}
```
