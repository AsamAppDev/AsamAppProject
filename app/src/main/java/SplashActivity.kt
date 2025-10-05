package com.asamapp.ir

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // تمام‌صفحه کردن اسپلش
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        // انیمیشن لوگو و لوگوتایپ
        val logo = findViewById<ImageView>(R.id.logo)
        val logotype = findViewById<ImageView>(R.id.logotype)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(fadeIn)
        logotype.startAnimation(fadeIn)

        // SharedPreferences
        val sharedPref = getSharedPreferences("AsamAppPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        val isFirstTime = sharedPref.getBoolean("is_first_time", true)

        // Intent نهایی
        val nextIntent = if (isFirstTime) {
            // فقط بار اول آنبوردینگ نمایش داده می‌شود
            sharedPref.edit().putBoolean("is_first_time", false).apply()
            Intent(this, OnboardingActivity::class.java)
        } else {
            // در دفعات بعدی بر اساس وضعیت لاگین، تصمیم گرفته می‌شود
            Intent(this, MainActivity::class.java).apply {
                putExtra("startWithLoginPage", !isLoggedIn)
            }
        }

        // رفتن به صفحه بعدی بعد از ۳ ثانیه
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(nextIntent)
            finish()
        }, 3000) // ۳ ثانیه
    }
}
