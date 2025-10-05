package com.asamapp.ir

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.asamapp.ir.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slides = listOf(
            OnboardingItem(
                R.drawable.slide1, // تصویر اول
                "همراه دیجیتال شما در مسیر زندگی سالم",
                "آسام‌اَپ با پایش سلامت و مشاوره‌های هوشمند، همیشه کنار سالمندان و خانواده‌هایشان است."
            ),
            OnboardingItem(
                R.drawable.slide2,
                "از آموزش تا پرستاری در منزل",
                "با یک اپلیکیشن همه‌کاره، به خدمات پرستاری، مشاوره پزشکی، آموزش‌های ویدیویی و پشتیبانی دسترسی داشته باشید."
            ),
            OnboardingItem(
                R.drawable.slide3,
                "سلامتی سالمندان، آرامش خانواده",
                "با مراقبت پیشگیرانه و ابزارهای هوشمند آسام‌اَپ، کیفیت زندگی سالمندان را ارتقا دهید."
            )
        )

        onboardingAdapter = OnboardingAdapter(slides)
        binding.viewPager.adapter = onboardingAdapter

        // دکمه بعدی
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < onboardingAdapter.itemCount) {
                binding.viewPager.currentItem += 1
            } else {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("startWithLoginPage", true) // همیشه بار اول به لاگین بره
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
