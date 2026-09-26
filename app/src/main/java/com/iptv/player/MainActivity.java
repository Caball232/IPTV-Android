package com.iptv.player;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "IPTVPlayerPrefs";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String DEFAULT_URL = "https://landmass-anointer-lilac.ngrok-free.dev";

    private WebView webView;
    private FrameLayout fullscreenContainer;
    private View customVideoView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private boolean doubleBackToExitPressedOnce = false;
    private SharedPreferences prefs;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen active on TV displays
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        applyImmersiveFullscreen();

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        webView = findViewById(R.id.webView);
        fullscreenContainer = findViewById(R.id.fullscreenContainer);

        configureWebView();

        String targetUrl = prefs.getString(KEY_SERVER_URL, DEFAULT_URL);
        loadServerUrl(targetUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyImmersiveFullscreen();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    private void applyImmersiveFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // High performance rendering
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();

        // Custom TV User-Agent
        String defaultUa = settings.getUserAgentString();
        settings.setUserAgentString(defaultUa + " IPTVPlayerApp/1.0 (FireTV; Android)");

        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        // Attach Hardware Device Bridge for MAC & Device ID Linking
        webView.addJavascriptInterface(new DeviceBridge(), "DeviceBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyImmersiveFullscreen();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customVideoView != null) {
                    onHideCustomView();
                    return;
                }
                customVideoView = view;
                customViewCallback = callback;

                fullscreenContainer.addView(view, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                fullscreenContainer.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                applyImmersiveFullscreen();
            }

            @Override
            public void onHideCustomView() {
                if (customVideoView == null) return;

                fullscreenContainer.removeView(customVideoView);
                fullscreenContainer.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);

                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }
                customVideoView = null;
                customViewCallback = null;
                applyImmersiveFullscreen();
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return super.onConsoleMessage(consoleMessage);
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        if (action == KeyEvent.ACTION_DOWN) {
            // TV Remote Media Buttons
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    executeJs("const p = document.getElementById('btnCinemaPlayToggle') || document.getElementById('btnPlayPause'); if (p) p.click();");
                    return true;

                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    executeJs("const v = document.querySelector('video'); if (v && v.paused) v.play();");
                    return true;

                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    executeJs("const v = document.querySelector('video'); if (v && !v.paused) v.pause();");
                    return true;

                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    executeJs("const fwd = document.getElementById('btnCinemaSeekFwd'); if (fwd) fwd.click();");
                    return true;

                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    executeJs("const rew = document.getElementById('btnCinemaSeekBack'); if (rew) rew.click();");
                    return true;

                case KeyEvent.KEYCODE_MENU:
                    showServerUrlDialog();
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // 1. If HTML5 custom video is playing in fullscreen
        if (customVideoView != null) {
            WebChromeClient chromeClient = new WebChromeClient();
            chromeClient.onHideCustomView();
            return;
        }

        // 2. Check if Cinema Modal or any active dialog is open in DOM
        webView.evaluateJavascript(
                "(function() { " +
                        "  const c = document.getElementById('cinemaPlayerModal'); " +
                        "  if (c && c.classList.contains('open')) { " +
                        "    const back = document.getElementById('btnCinemaBack'); " +
                        "    if (back) { back.click(); return 'cinema_closed'; } " +
                        "  } " +
                        "  const m = document.querySelector('.modal-backdrop.open'); " +
                        "  if (m) { " +
                        "    m.classList.remove('open'); return 'modal_closed'; " +
                        "  } " +
                        "  return 'none'; " +
                        "})();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if ("\"cinema_closed\"".equals(value) || "\"modal_closed\"".equals(value)) {
                            return;
                        }

                        // 3. Fallback: Can browser history go back?
                        if (webView.canGoBack()) {
                            webView.goBack();
                            return;
                        }

                        // 4. Double tap back to exit
                        handleAppExit();
                    }
                }
        );
    }

    private void handleAppExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.exit_prompt), Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private void showServerUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_server);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        String currentUrl = prefs.getString(KEY_SERVER_URL, DEFAULT_URL);
        input.setText(currentUrl);
        builder.setView(input);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String newUrl = input.getText().toString().trim();
            if (!newUrl.isEmpty()) {
                if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                    newUrl = "http://" + newUrl;
                }
                prefs.edit().putString(KEY_SERVER_URL, newUrl).apply();
                loadServerUrl(newUrl);
                Toast.makeText(MainActivity.this, "Connecting to: " + newUrl, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadServerUrl(String url) {
        if (webView == null || url == null) return;
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        headers.put("ngrok-skip-browser-warning", "true");
        headers.put("x-device-mac", getMacAddress());
        headers.put("x-device-id", getHardwareDeviceId());
        headers.put("x-device-model", getDeviceModel());
        webView.loadUrl(url, headers);
    }

    public String getHardwareDeviceId() {
        try {
            @SuppressLint("HardwareIds")
            String id = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            if (id != null && !id.isEmpty()) {
                return "FS-" + id.toUpperCase();
            }
        } catch (Exception ignored) {}
        return "FS-" + Integer.toHexString(Build.FINGERPRINT.hashCode()).toUpperCase();
    }

    public String getDeviceModel() {
        return Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL;
    }

    public String getMacAddress() {
        try {
            java.util.List<java.net.NetworkInterface> interfaces = java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces());
            for (java.net.NetworkInterface nif : interfaces) {
                if (nif.getName().equalsIgnoreCase("wlan0") || nif.getName().equalsIgnoreCase("eth0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes != null && macBytes.length > 0) {
                        StringBuilder res = new StringBuilder();
                        for (byte b : macBytes) {
                            res.append(String.format("%02X:", b));
                        }
                        if (res.length() > 0) res.deleteCharAt(res.length() - 1);
                        return res.toString();
                    }
                }
            }
        } catch (Exception ignored) {}

        // Standard IPTV MAG / Stalker persistent MAC fallback derived from Android ID
        try {
            @SuppressLint("HardwareIds")
            String id = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            if (id != null && id.length() >= 6) {
                String sub = id.substring(id.length() - 6).toUpperCase();
                return String.format("00:1A:79:%s:%s:%s", sub.substring(0, 2), sub.substring(2, 4), sub.substring(4, 6));
            }
        } catch (Exception ignored) {}

        return "00:1A:79:B4:C8:10";
    }

    public class DeviceBridge {
        @android.webkit.JavascriptInterface
        public String getMacAddress() {
            return MainActivity.this.getMacAddress();
        }

        @android.webkit.JavascriptInterface
        public String getDeviceId() {
            return MainActivity.this.getHardwareDeviceId();
        }

        @android.webkit.JavascriptInterface
        public String getDeviceModel() {
            return MainActivity.this.getDeviceModel();
        }

        @android.webkit.JavascriptInterface
        public boolean isNativeApp() {
            return true;
        }
    }

    private void executeJs(String code) {
        if (webView != null) {
            webView.evaluateJavascript(code, null);
        }
    }
}
