package com.asamapp.ir

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var noInternetView: LinearLayout
    private lateinit var retryButton: Button
    private var currentUrl: String = ""
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_REQUEST_CODE = 1001

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // اتصال ویوها
        swipeRefreshLayout = findViewById(R.id.swipeRefresh)
        webView = findViewById(R.id.webview)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        noInternetView = findViewById(R.id.noInternetView)
        retryButton = findViewById(R.id.retryButton)

        // تنظیمات WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            allowFileAccess = true
            loadsImagesAutomatically = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        webView.isLongClickable = false
        webView.isHapticFeedbackEnabled = false
        webView.setOnLongClickListener { true }

        // جلوگیری از رفتن زیر status bar
        ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val sharedPref = getSharedPreferences("AsamAppPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        val targetUrl = if (isLoggedIn) "https://asamapp.ir" else "https://asamapp.ir/auth"

        // WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    currentUrl = it
                    return when {
                        it.startsWith("tel:") || it.startsWith("mailto:") ||
                                it.startsWith("sms:") || it.startsWith("intent:") ||
                                it.endsWith(".pdf") || it.contains("download") -> {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                            true
                        }
                        else -> {
                            view?.loadUrl(it)
                            true
                        }
                    }
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefreshLayout.isRefreshing = false
                loadingIndicator.visibility = View.GONE
                noInternetView.visibility = View.GONE
                currentUrl = url ?: ""
                swipeRefreshLayout.isEnabled = !currentUrl.contains("/chatbot")

                if (url?.contains("dashboard") == true || url?.contains("profile") == true) {
                    sharedPref.edit().putBoolean("is_logged_in", true).apply()
                }
            }
        }

        // WebChromeClient برای آپلود فایل
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: WebChromeClient.FileChooserParams?
            ): Boolean {
                // پاک کردن callback قبلی (اگر وجود داره)
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback

                // createIntent ممکنه null باشه => هندل کن
                val chooserIntent = try {
                    fileChooserParams?.createIntent()
                } catch (e: Exception) {
                    null
                }

                if (chooserIntent == null) {
                    this@MainActivity.filePathCallback = null
                    return false
                }

                return try {
                    // توجه: نام‌گذاری پارامتر requestCode در اینجا نذار (اسم پارامتر قبول نمیشه برای این متد)
                    startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE)
                    true
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    false
                }
            }
        }

        // تلاش مجدد
        retryButton.setOnClickListener {
            loadWebPage(targetUrl)
        }

        // SwipeRefresh
        webView.viewTreeObserver.addOnScrollChangedListener {
            val canScrollUp = webView.scrollY > 0
            swipeRefreshLayout.isEnabled = !canScrollUp
        }
        swipeRefreshLayout.setOnRefreshListener { webView.reload() }

        // بارگذاری اولیه
        loadWebPage(targetUrl)
    }

    // ✅ تابع بارگذاری
    private fun loadWebPage(url: String) {
        if (isConnected()) {
            loadingIndicator.visibility = View.VISIBLE
            noInternetView.visibility = View.GONE
            webView.loadUrl(url)
        } else {
            loadingIndicator.visibility = View.GONE
            noInternetView.visibility = View.VISIBLE
        }
    }

    // ✅ بررسی اینترنت
    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    // ✅ نتیجه انتخاب فایل
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            val result =
                if (resultCode == RESULT_OK && data != null) {
                    data.data?.let { arrayOf(it) }
                } else null

            filePathCallback?.onReceiveValue(result)
            filePathCallback = null
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
