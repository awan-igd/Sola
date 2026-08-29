package sola.aigd;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

public class SearchActivity extends AppCompatActivity {

    private static final int TAB_MANAGER_REQUEST = 2001;
    private static final long ENTRANCE_DELAY = 300;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private DrawerLayout drawerLayout;
    private LinearLayout drawerMenu;

    private LinearLayout topBar;
    private ImageView menuButton;
    private EditText searchEditText;
    private ImageView refreshButton;
    private ImageView shareButton;
    private ProgressBar progressBar;

    private WebView webView;
    private FrameLayout loadingOverlay;
    private LinearLayout errorView;
    private TextView errorMessage;
    private View retryButton;

    private LinearLayout bottomAdContainer;
    private Banner bannerAd;
    private ProgressBar adLoader;
    private TextView adLabel;

    private AnimatedBackground animatedBackground;

    private TextView desktopModeStatus, javascriptStatus, tabCount;
    private LinearLayout drawerNewTab, drawerManageTabs, drawerHistory;
    private LinearLayout drawerDesktopMode, drawerJavaScript;
    private LinearLayout drawerAbout, drawerExit;

    private DataManager dataManager;
    private SEManager searchManager;
    private WebRouter webRouter;

    private String currentUrl = "";
    private String currentTitle = "";
    private long lastDownloadId = -1;
    private int currentTabId = -1;
    private boolean isLoading = false;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private FrameLayout fullscreenContainer;

    private BroadcastReceiver downloadReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupFullScreenWindow();
        initializeCore();
        initViews();
        setupWindowInsets();
        setupFullscreenContainer();
        setupAnimatedBackground();
        setupWebView();
        setupDownloadListener();
        setupClickListeners();
        setupDrawerClickListeners();
        loadSettingsToDrawer();
        startEntranceAnimations();
        registerDownloadReceiver();
        loadBannerAd();

        handler.postDelayed(this::handleIntentAndLoad, ENTRANCE_DELAY);
    }

    private void setupFullScreenWindow() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            Insets statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), statusInsets.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(drawerMenu, (v, insets) -> {
            Insets statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), statusInsets.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private void initializeCore() {
        dataManager = DataManager.getInstance(this);
        searchManager = SEManager.getInstance(this);
        webRouter = WebRouter.getInstance(this);
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerMenu = findViewById(R.id.drawerMenu);

        topBar = findViewById(R.id.topBar);
        menuButton = findViewById(R.id.menuButton);
        searchEditText = findViewById(R.id.searchEditText);
        refreshButton = findViewById(R.id.refreshButton);
        shareButton = findViewById(R.id.shareButton);
        progressBar = findViewById(R.id.progressBar);

        webView = findViewById(R.id.webView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        errorView = findViewById(R.id.errorView);
        errorMessage = findViewById(R.id.errorMessage);
        retryButton = findViewById(R.id.retryButton);

        bottomAdContainer = findViewById(R.id.bottomAdContainer);
        bannerAd = findViewById(R.id.bannerAd);
        adLoader = findViewById(R.id.adLoader);
        adLabel = findViewById(R.id.adLabel);

        animatedBackground = findViewById(R.id.animatedBackground);

        drawerNewTab = findViewById(R.id.drawerNewTab);
        drawerManageTabs = findViewById(R.id.drawerManageTabs);
        drawerHistory = findViewById(R.id.drawerHistory);
        drawerDesktopMode = findViewById(R.id.drawerDesktopMode);
        drawerJavaScript = findViewById(R.id.drawerJavaScript);
        drawerAbout = findViewById(R.id.drawerAbout);
        drawerExit = findViewById(R.id.drawerExit);
        desktopModeStatus = findViewById(R.id.desktopModeStatus);
        javascriptStatus = findViewById(R.id.javascriptStatus);
        tabCount = findViewById(R.id.tabCount);

        updateTabCount();
    }

    private void setupAnimatedBackground() {
        if (animatedBackground != null) {
            animatedBackground.setWaveSpeed(5000);
            animatedBackground.resumeAnimation();
        }
    }

    private void setupFullscreenContainer() {
        fullscreenContainer = new FrameLayout(this);
        fullscreenContainer.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        fullscreenContainer.setBackgroundColor(Color.BLACK);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(dataManager.isJavaScriptEnabled());
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (dataManager.isDesktopMode()) {
            settings.setUserAgentString(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            );
        }

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.addJavascriptInterface(new JavaScriptInterface(), "SolaDownloader");

        setupWebViewClient();
        setupWebChromeClient();
    }

    private void setupWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                currentUrl = url;
                searchEditText.setText(url);
                showLoading();
                isLoading = true;
                hideError();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                currentTitle = view.getTitle();
                if (!TextUtils.isEmpty(currentTitle) && url != null && !url.equals("about:blank")) {
                    dataManager.addToHistory(currentTitle, url);
                    updateCurrentTab();
                }
                hideLoading();
                isLoading = false;
                injectLongPressScript();

                if (!TextUtils.isEmpty(currentTitle)) {
                    searchEditText.setText(currentTitle);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                hideLoading();
                isLoading = false;
                showError(description);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:") || url.startsWith("mailto:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void injectLongPressScript() {
        String jsCode = "javascript:(function() {" +
                "   var elements = document.querySelectorAll('img, video, a[href$=\".jpg\"], a[href$=\".jpeg\"], a[href$=\".png\"], a[href$=\".gif\"], a[href$=\".webp\"], a[href$=\".mp4\"], a[href$=\".mp3\"]');" +
                "   for(var i = 0; i < elements.length; i++) {" +
                "       elements[i].addEventListener('contextmenu', function(e) {" +
                "           e.preventDefault();" +
                "           var url = this.src || this.href;" +
                "           if(url) SolaDownloader.downloadMedia(url, 'file');" +
                "           return false;" +
                "       });" +
                "   }" +
                "})()";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(jsCode, null);
        } else {
            webView.loadUrl(jsCode);
        }
    }

    private void setupWebChromeClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                currentTitle = title;
                updateCurrentTab();
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                customView = view;
                customViewCallback = callback;
                webView.setVisibility(View.GONE);
                fullscreenContainer.addView(customView);
                addContentView(fullscreenContainer,
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        )
                );
                hideSystemUI();
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;
                fullscreenContainer.removeView(customView);
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }
                customView = null;
                webView.setVisibility(View.VISIBLE);
                showSystemUI();
            }
        });
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private void showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    private void setupDownloadListener() {
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            startDownload(url);
        });
    }

    private void startDownload(String url) {
        try {
            String fileName = URLUtil.guessFileName(url, null, null);
            if (fileName.isEmpty()) {
                fileName = "download_" + System.currentTimeMillis();
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("Downloading from SOLA Browser");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SOLA/" + fileName);

            String cookies = CookieManager.getInstance().getCookie(url);
            if (cookies != null && !cookies.isEmpty()) {
                request.addRequestHeader("Cookie", cookies);
            }

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                lastDownloadId = downloadManager.enqueue(request);
                showToast("Download started: " + fileName);
            }
        } catch (Exception e) {
            showToast("Download failed");
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerDownloadReceiver() {
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == lastDownloadId) {
                    runOnUiThread(() -> showToast("Download completed!"));
                }
            }
        };

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(downloadReceiver, filter);
        }
    }

    private void loadBannerAd() {
        if (bannerAd == null) return;

        adLoader.setVisibility(View.VISIBLE);
        bannerAd.setVisibility(View.GONE);
        adLabel.setVisibility(View.GONE);

        bannerAd.setBannerListener(new BannerListener() {
            @Override
            public void onReceiveAd(View banner) {
                adLoader.setVisibility(View.GONE);
                bannerAd.setVisibility(View.VISIBLE);
                adLabel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailedToReceiveAd(View banner) {
                adLoader.setVisibility(View.GONE);
                bannerAd.setVisibility(View.GONE);
                adLabel.setVisibility(View.GONE);
            }

            @Override
            public void onImpression(View banner) {}

            @Override
            public void onClick(View banner) {}
        });

        bannerAd.loadAd();
    }

    private void shareCurrentPage() {
        String url = currentUrl;
        String title = currentTitle;

        if (TextUtils.isEmpty(url)) {
            url = "https://www.google.com";
        }
        if (TextUtils.isEmpty(title)) {
            title = "SOLA Browser";
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                title + "\n" + url + "\n\nShared via SOLA Browser");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void setupClickListeners() {
        menuButton.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
                animateMenuButton();
                updateTabCount();
            }
        });

        refreshButton.setOnClickListener(v -> {
            animateRefreshButton();
            webView.reload();
        });

        shareButton.setOnClickListener(v -> {
            animateButtonClick(shareButton);
            shareCurrentPage();
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                String query = searchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(query)) {
                    hideKeyboard();
                    performSearch(query);
                }
                return true;
            }
            return false;
        });

        retryButton.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(currentUrl)) {
                webView.loadUrl(currentUrl);
                hideError();
                showLoading();
            }
        });

        webView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }

    private void setupDrawerClickListeners() {
        drawerNewTab.setOnClickListener(v -> {
            saveCurrentTab();
            String homeUrl = searchManager.getCurrentEngineHomeUrl();
            if (TextUtils.isEmpty(homeUrl)) {
                homeUrl = "https://www.google.com";
            }
            int newTabId = dataManager.createNewTab("New Tab", homeUrl);
            currentTabId = newTabId;
            currentUrl = homeUrl;
            currentTitle = "New Tab";
            webView.loadUrl(homeUrl);
            searchEditText.setText(homeUrl);
            updateTabCount();
            closeDrawer();
            showToast("New tab opened");
        });

        drawerManageTabs.setOnClickListener(v -> {
            saveCurrentTab();
            Intent intent = new Intent(SearchActivity.this, TabActivity.class);
            startActivityForResult(intent, TAB_MANAGER_REQUEST);
            closeDrawer();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        drawerHistory.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, HistoryActivity.class);
            startActivity(intent);
            closeDrawer();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        drawerDesktopMode.setOnClickListener(v -> {
            boolean newState = !dataManager.isDesktopMode();
            dataManager.setDesktopMode(newState);
            desktopModeStatus.setText(newState ? "ON" : "OFF");
            desktopModeStatus.setBackgroundResource(
                    newState ? R.drawable.bg_filled : R.drawable.bg_outline
            );
            webView.getSettings().setUserAgentString(
                    newState ? "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" : null
            );
            webView.reload();
            animateStatusChange(desktopModeStatus);
            showToast("Desktop Mode: " + (newState ? "ON" : "OFF"));
            closeDrawer();
        });

        drawerJavaScript.setOnClickListener(v -> {
            boolean newState = !dataManager.isJavaScriptEnabled();
            dataManager.setJavaScriptEnabled(newState);
            javascriptStatus.setText(newState ? "ON" : "OFF");
            javascriptStatus.setBackgroundResource(
                    newState ? R.drawable.bg_filled : R.drawable.bg_outline
            );
            webView.getSettings().setJavaScriptEnabled(newState);
            webView.reload();
            animateStatusChange(javascriptStatus);
            showToast("JavaScript: " + (newState ? "ON" : "OFF"));
            closeDrawer();
        });

        drawerAbout.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, AboutActivity.class);
            startActivity(intent);
            closeDrawer();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        drawerExit.setOnClickListener(v -> {
            finishAffinity();
        });
    }

    private void loadSettingsToDrawer() {
        if (desktopModeStatus != null) {
            boolean desktop = dataManager.isDesktopMode();
            desktopModeStatus.setText(desktop ? "ON" : "OFF");
            desktopModeStatus.setBackgroundResource(
                    desktop ? R.drawable.bg_filled : R.drawable.bg_outline
            );
        }
        if (javascriptStatus != null) {
            boolean js = dataManager.isJavaScriptEnabled();
            javascriptStatus.setText(js ? "ON" : "OFF");
            javascriptStatus.setBackgroundResource(
                    js ? R.drawable.bg_filled : R.drawable.bg_outline
            );
        }
        updateTabCount();
    }

    private void updateTabCount() {
        if (tabCount != null && dataManager != null) {
            int count = dataManager.getTabCount();
            tabCount.setText(String.valueOf(count));
            tabCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void handleIntentAndLoad() {
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            Uri data = getIntent().getData();
            if (data != null) {
                String url = data.toString();
                if (!TextUtils.isEmpty(url)) {
                    loadUrl(url);
                    return;
                }
            }
        }

        WebRouter.RouterData data = WebRouter.receive(this, getIntent());

        if (data != null && data.hasData()) {
            if (data.isUrl() && !TextUtils.isEmpty(data.url)) {
                loadUrl(data.url);
                return;
            }
            if (data.isSearch() && !TextUtils.isEmpty(data.url)) {
                if (!TextUtils.isEmpty(data.query)) {
                    searchEditText.setText(data.query);
                }
                loadUrl(data.url);
                return;
            }
            if (data.isHome() && !TextUtils.isEmpty(data.url)) {
                loadUrl(data.url);
                return;
            }
        }

        if (dataManager != null && dataManager.getTabCount() > 0) {
            DataManager.TabItem lastTab = dataManager.getTabAt(0);
            if (lastTab != null && !TextUtils.isEmpty(lastTab.getUrl())) {
                currentTabId = lastTab.getId();
                currentUrl = lastTab.getUrl();
                currentTitle = lastTab.getTitle();
                loadUrl(lastTab.getUrl());
                return;
            }
        }

        String homeUrl = searchManager.getCurrentEngineHomeUrl();
        if (TextUtils.isEmpty(homeUrl)) {
            homeUrl = "https://www.google.com";
        }
        currentTabId = dataManager.createNewTab("New Tab", homeUrl);
        currentUrl = homeUrl;
        currentTitle = "New Tab";
        loadUrl(homeUrl);
    }

    private void loadUrl(String url) {
        if (TextUtils.isEmpty(url)) return;
        searchEditText.setText(url);
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

    private void performSearch(String query) {
        if (TextUtils.isEmpty(query.trim())) {
            showToast("Enter search query");
            return;
        }
        hideKeyboard();
        String searchUrl = searchManager.buildSearchUrl(query);
        loadUrl(searchUrl);
    }

    private void saveCurrentTab() {
        if (currentTabId != -1 && !TextUtils.isEmpty(currentUrl)) {
            String title = TextUtils.isEmpty(currentTitle) ? currentUrl : currentTitle;
            dataManager.updateTab(currentTabId, title, currentUrl);
        }
    }

    private void updateCurrentTab() {
        if (currentTabId != -1 && !TextUtils.isEmpty(currentUrl)) {
            String title = TextUtils.isEmpty(currentTitle) ? currentUrl : currentTitle;
            dataManager.updateTab(currentTabId, title, currentUrl);
        }
    }

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingOverlay.bringToFront();
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    private void showError(String message) {
        if (errorView != null) {
            errorView.setVisibility(View.VISIBLE);
            errorView.bringToFront();
        }
        if (errorMessage != null) {
            errorMessage.setText(message);
        }
    }

    private void hideError() {
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (imm != null && currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            currentFocus.clearFocus();
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startEntranceAnimations() {
        if (topBar != null) {
            topBar.setAlpha(0f);
            topBar.setTranslationY(-30f);
            topBar.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (webView != null) {
            webView.setAlpha(0f);
            webView.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setStartDelay(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void animateMenuButton() {
        menuButton.animate()
                .rotation(90f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> menuButton.animate().rotation(0f).setDuration(300).start())
                .start();
    }

    private void animateRefreshButton() {
        refreshButton.animate()
                .rotation(360f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateButtonClick(View button) {
        if (button == null) return;
        button.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(80)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start())
                .start();
    }

    private void animateStatusChange(TextView view) {
        if (view != null) {
            view.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(150)
                    .withEndAction(() -> view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start())
                    .start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAB_MANAGER_REQUEST && resultCode == RESULT_OK && data != null) {
            int selectedTabId = data.getIntExtra("tab_id", -1);
            String tabUrl = data.getStringExtra("tab_url");
            String tabTitle = data.getStringExtra("tab_title");
            boolean openNewTab = data.getBooleanExtra("open_new_tab", false);

            if (openNewTab) {
                loadTabsFromDataManager();
                return;
            }

            if (selectedTabId != -1 && tabUrl != null) {
                saveCurrentTab();
                currentTabId = selectedTabId;
                currentTitle = tabTitle;
                currentUrl = tabUrl;
                searchEditText.setText(tabUrl);
                webView.loadUrl(tabUrl);
                showToast("Switched to tab");
                updateTabCount();
            }
        }
    }

    private void loadTabsFromDataManager() {
        if (dataManager != null && dataManager.getTabCount() > 0) {
            DataManager.TabItem lastTab = dataManager.getTabAt(0);
            if (lastTab != null && !TextUtils.isEmpty(lastTab.getUrl())) {
                currentTabId = lastTab.getId();
                currentUrl = lastTab.getUrl();
                currentTitle = lastTab.getTitle();
                searchEditText.setText(currentUrl);
                webView.loadUrl(currentUrl);
                updateTabCount();
                showToast("Switched to tab");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
        if (animatedBackground != null) animatedBackground.resumeAnimation();
        updateTabCount();
        loadSettingsToDrawer();

        if (currentTabId != -1 && !TextUtils.isEmpty(currentUrl)) {
            updateCurrentTab();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
        if (animatedBackground != null) animatedBackground.pauseAnimation();
        saveCurrentTab();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

        if (webView != null) {
            webView.destroy();
        }

        if (downloadReceiver != null) {
            try {
                unregisterReceiver(downloadReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver wasn't registered
            }
        }

        if (animatedBackground != null) {
            animatedBackground.stopBackgroundAnimation();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private class JavaScriptInterface {
        @android.webkit.JavascriptInterface
        public void downloadMedia(String url, String type) {
            runOnUiThread(() -> {
                if (url != null && !url.isEmpty()) {
                    startDownload(url);
                }
            });
        }
    }
}