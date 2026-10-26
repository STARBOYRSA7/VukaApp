package com.hustle404.vuka

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val data = result.data
            val uris: Array<Uri>? = if (result.resultCode == RESULT_OK && data?.data != null) {
                arrayOf(data.data!!)
            } else {
                null
            }
            filePathCallback?.onReceiveValue(uris)
        } catch (e: Exception) {
            Log.e("Vuka", "File chooser result failed", e)
            filePathCallback?.onReceiveValue(null)
        } finally {
            filePathCallback = null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("sms:")) {
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        true
                    } catch (e: Exception) {
                        Log.e("Vuka", "No app available to handle $url", e)
                        true
                    }
                }
                // Any real http(s) link (Apply buttons, course links, Google/LinkedIn
                // sign-in) opens in the phone's actual browser, not this WebView.
                // Google specifically blocks sign-in from embedded WebViews.
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback

                return try {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        putExtra(
                            Intent.EXTRA_MIME_TYPES,
                            arrayOf(
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    val chooser = Intent.createChooser(intent, "Choose a PDF or Word file")
                    fileChooserLauncher.launch(chooser)
                    true
                } catch (e: Exception) {
                    Log.e("Vuka", "Could not open file chooser", e)
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = null
                    false
                }
            }
        }

        webView.loadUrl("file:///android_asset/vuka.html")
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "vuka" && uri.host == "login-callback") {
            val code = uri.getQueryParameter("code")
            if (code != null) {
                val js = "window.vukaHandleOAuthCallback && window.vukaHandleOAuthCallback(" +
                    org.json.JSONObject.quote(code) + ");"
                webView.evaluateJavascript(js, null)
            }
        }
    }

    override fun onBackPressed() {
        webView.evaluateJavascript(
            "(function(){ return window.vukaHandleBack ? window.vukaHandleBack() : false; })();"
        ) { result ->
            val handledInApp = result == "true"
            if (!handledInApp) {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    super.onBackPressed()
                }
            }
        }
    }
}
