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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import sola.aigd.R;

public class SearchActivity extends AppCompatActivity {

    private static final int TAB_MANAGER_REQUEST = 2001;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private DrawerLayout drawerLayout;
    private LinearLayout drawerMenu, topBar, errorView, bottomAdContainer;
    private CardView menuButton, refreshButton, shareButton;
    private CardView drawerNewTab, drawerManageTabs, drawerHistory, drawerDesktopMode, drawerJavaScript, drawerAbout, drawerExit;
    private EditText searchEditText;
    private ProgressBar progressBar, adLoader;
    private WebView webView;
    private FrameLayout loadingOverlay, fullscreenContainer;
    private TextView errorMessage, adLabel, desktopModeStatus, javascriptStatus, tabCount;
    private View retryButton;
    private Banner bannerAd;

    private DataManager dataManager;
    private SEManager searchManager;
    private WebRouter webRouter;

    private String currentUrl = "", currentTitle = "";
    private long lastDownloadId = -1;
    private int currentTabId = -1;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
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
        setupWebView();
        setupDownloadListener();
        setupClickListeners();
        setupDrawerClickListeners();
        loadSettingsToDrawer();
        startEntranceAnimations();
        registerDownloadReceiver();
        loadBannerAd();
        handler.postDelayed(this::handleIntentAndLoad, 250);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handler.postDelayed(this::handleIntentAndLoad, 100);
    }

    private void setupFullScreenWindow() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (c != null) {
            c.setAppearanceLightStatusBars(true);
            c.setAppearanceLightNavigationBars(true);
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            Insets s = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), s.top + 12, v.getPaddingRight(), 12);
            return insets;
        });
        if (drawerMenu != null)
            ViewCompat.setOnApplyWindowInsetsListener(drawerMenu, (v, insets) -> {
                Insets s = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(v.getPaddingLeft(), s.top, v.getPaddingRight(), v.getPaddingBottom());
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

    private void setupFullscreenContainer() {
        fullscreenContainer = new FrameLayout(this);
        fullscreenContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fullscreenContainer.setBackgroundColor(Color.BLACK);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(dataManager.isJavaScriptEnabled());
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setSupportZoom(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        if (dataManager.isDesktopMode())
            s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        else s.setUserAgentString(null);
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
                if (searchEditText != null) searchEditText.setText(url);
                showLoading();
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
                injectLongPressScript();
            }

            @Override
            public void onReceivedError(WebView view, int code, String desc, String url) {
                hideLoading();
                showError(desc);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("intent:")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception ignored) {
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void injectLongPressScript() {
        String js = "javascript:(function(){var els=document.querySelectorAll('img,video,a[href$=\".jpg\"],a[href$=\".png\"],a[href$=\".mp4\"]');for(var i=0;i<els.length;i++){els[i].addEventListener('contextmenu',function(e){e.preventDefault();var u=this.src||this.href;if(u)SolaDownloader.downloadMedia(u,'file');return false;});}})()";
        if (Build.VERSION.SDK_INT >= 19) webView.evaluateJavascript(js, null);
        else webView.loadUrl(js);
    }

    private void setupWebChromeClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int p) {
                if (progressBar != null) {
                    if (progressBar.getVisibility() != View.VISIBLE)
                        progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(p);
                    if (p >= 100) handler.postDelayed(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    }, 300);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                currentTitle = title;
                updateCurrentTab();
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback cb) {
                if (customView != null) {
                    cb.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = cb;
                webView.setVisibility(View.GONE);
                fullscreenContainer.addView(customView);
                addContentView(fullscreenContainer, new ViewGroup.LayoutParams(-1, -1));
                hideSystemUI();
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;
                fullscreenContainer.removeView(customView);
                if (customViewCallback != null) customViewCallback.onCustomViewHidden();
                customView = null;
                webView.setVisibility(View.VISIBLE);
                showSystemUI();
            }
        });
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(0x00000106 | 0x00000200 | 0x00000400);
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void setupDownloadListener() {
        webView.setDownloadListener((url, ua, cd, mime, len) -> startDownload(url));
    }

    private void startDownload(String url) {
        try {
            String fileName = URLUtil.guessFileName(url, null, null);
            if (fileName.isEmpty()) fileName = "Sola_" + System.currentTimeMillis();
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setTitle(fileName);
            req.setDescription("Downloading via Sola");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Sola/" + fileName);
            String cookies = CookieManager.getInstance().getCookie(url);
            if (!TextUtils.isEmpty(cookies)) req.addRequestHeader("Cookie", cookies);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                lastDownloadId = dm.enqueue(req);
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
            public void onReceive(Context c, Intent i) {
                long id = i.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == lastDownloadId) runOnUiThread(() -> showToast("Download completed!"));
            }
        };
        IntentFilter f = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= 33)
            registerReceiver(downloadReceiver, f, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(downloadReceiver, f);
    }

    private void loadBannerAd() {
        if (bannerAd == null) return;
        if (adLoader != null) adLoader.setVisibility(View.VISIBLE);
        bannerAd.setVisibility(View.GONE);
        if (adLabel != null) adLabel.setVisibility(View.GONE);
        bannerAd.setBannerListener(new BannerListener() {
            @Override
            public void onReceiveAd(View b) {
                if (adLoader != null) adLoader.setVisibility(View.GONE);
                bannerAd.setVisibility(View.VISIBLE);
                if (adLabel != null) adLabel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailedToReceiveAd(View b) {
                if (adLoader != null) adLoader.setVisibility(View.GONE);
                bannerAd.setVisibility(View.GONE);
                if (adLabel != null) adLabel.setVisibility(View.GONE);
            }

            @Override
            public void onImpression(View b) {
            }

            @Override
            public void onClick(View b) {
            }
        });
        try {
            bannerAd.loadAd();
        } catch (Exception e) {
            if (adLoader != null) adLoader.setVisibility(View.GONE);
        }
    }

    private void shareCurrentPage() {
        String url = TextUtils.isEmpty(currentUrl) ? "https://www.google.com" : currentUrl;
        String title = TextUtils.isEmpty(currentTitle) ? "Sola Browser" : currentTitle;
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, title + "\n" + url + "\n\nShared via Sola Browser");
        startActivity(Intent.createChooser(i, "Share via"));
    }

    private void setupClickListeners() {
        if (menuButton != null) menuButton.setOnClickListener(v -> {
            if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            animateMenuButton();
            updateTabCount();
        });
        if (refreshButton != null) refreshButton.setOnClickListener(v -> {
            animateRefreshButton();
            if (webView != null) webView.reload();
        });
        if (shareButton != null) shareButton.setOnClickListener(v -> {
            animateButtonClick(shareButton);
            shareCurrentPage();
        });
        if (searchEditText != null) searchEditText.setOnEditorActionListener((v, id, e) -> {
            if (id == EditorInfo.IME_ACTION_GO) {
                String q = searchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(q)) {
                    hideKeyboard();
                    performSearch(q);
                }
                return true;
            }
            return false;
        });
        if (retryButton != null) retryButton.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(currentUrl)) {
                if (webView != null) webView.loadUrl(currentUrl);
                hideError();
                showLoading();
            }
        });
        if (webView != null) webView.setOnTouchListener((v, e) -> {
            hideKeyboard();
            return false;
        });
    }

    private void setupDrawerClickListeners() {
        if (drawerNewTab != null) drawerNewTab.setOnClickListener(v -> {
            animateDrawerItem(v);
            saveCurrentTab();
            String home = searchManager.getCurrentEngineHomeUrl();
            if (TextUtils.isEmpty(home)) home = "https://www.google.com";
            int nid = dataManager.createNewTab("New Tab", home);
            currentTabId = nid;
            currentUrl = home;
            currentTitle = "New Tab";
            if (webView != null) webView.loadUrl(home);
            if (searchEditText != null) searchEditText.setText(home);
            updateTabCount();
            closeDrawer();
            showToast("New tab");
        });
        if (drawerManageTabs != null) drawerManageTabs.setOnClickListener(v -> {
            animateDrawerItem(v);
            saveCurrentTab();
            startActivityForResult(new Intent(this, TabActivity.class), TAB_MANAGER_REQUEST);
            closeDrawer();
        });
        if (drawerHistory != null) drawerHistory.setOnClickListener(v -> {
            animateDrawerItem(v);
            startActivity(new Intent(this, HistoryActivity.class));
            closeDrawer();
        });
        if (drawerDesktopMode != null) drawerDesktopMode.setOnClickListener(v -> {
            animateDrawerItem(v);
            boolean ns = !dataManager.isDesktopMode();
            dataManager.setDesktopMode(ns);
            if (desktopModeStatus != null) {
                desktopModeStatus.setText(ns ? "ON" : "OFF");
                desktopModeStatus.setBackgroundResource(ns ? R.drawable.bg_filled : R.drawable.bg_outline);
                desktopModeStatus.setTextColor(getColor(ns ? R.color.b : R.color.f));
                animateStatusChange(desktopModeStatus);
            }
            if (webView != null) {
                webView.getSettings().setUserAgentString(ns ? "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" : null);
                webView.reload();
            }
            showToast("Desktop: " + (ns ? "ON" : "OFF"));
            closeDrawer();
        });
        if (drawerJavaScript != null) drawerJavaScript.setOnClickListener(v -> {
            animateDrawerItem(v);
            boolean ns = !dataManager.isJavaScriptEnabled();
            dataManager.setJavaScriptEnabled(ns);
            if (javascriptStatus != null) {
                javascriptStatus.setText(ns ? "ON" : "OFF");
                javascriptStatus.setBackgroundResource(ns ? R.drawable.bg_filled : R.drawable.bg_outline);
                javascriptStatus.setTextColor(getColor(ns ? R.color.b : R.color.f));
                animateStatusChange(javascriptStatus);
            }
            if (webView != null) {
                webView.getSettings().setJavaScriptEnabled(ns);
                webView.reload();
            }
            showToast("JavaScript: " + (ns ? "ON" : "OFF"));
            closeDrawer();
        });
        if (drawerAbout != null) drawerAbout.setOnClickListener(v -> {
            animateDrawerItem(v);
            startActivity(new Intent(this, AboutActivity.class));
            closeDrawer();
        });
        if (drawerExit != null) drawerExit.setOnClickListener(v -> {
            animateDrawerItem(v);
            finishAffinity();
        });
    }

    private void loadSettingsToDrawer() {
        if (desktopModeStatus != null) {
            boolean d = dataManager.isDesktopMode();
            desktopModeStatus.setText(d ? "ON" : "OFF");
            desktopModeStatus.setBackgroundResource(d ? R.drawable.bg_filled : R.drawable.bg_outline);
            desktopModeStatus.setTextColor(getColor(d ? R.color.b : R.color.f));
        }
        if (javascriptStatus != null) {
            boolean j = dataManager.isJavaScriptEnabled();
            javascriptStatus.setText(j ? "ON" : "OFF");
            javascriptStatus.setBackgroundResource(j ? R.drawable.bg_filled : R.drawable.bg_outline);
            javascriptStatus.setTextColor(getColor(j ? R.color.b : R.color.f));
        }
        updateTabCount();
    }

    private void updateTabCount() {
        if (tabCount != null && dataManager != null) {
            int c = dataManager.getTabCount();
            tabCount.setText(String.valueOf(c));
            tabCount.setVisibility(c > 0 ? View.VISIBLE : View.GONE);
        }
    }

    // ========= FIXED INTENT HANDLER - INTERNAL + EXTERNAL =========
    private void handleIntentAndLoad() {
        Intent intent = getIntent();
        if (intent == null) {
            loadDefaultOrLastTab();
            return;
        }

        // 1. INTERNAL: from TabActivity with tab_id
        int tabIdFromIntent = intent.getIntExtra("tab_id", -1);
        String tabUrlFromIntent = intent.getStringExtra("tab_url");
        if (tabIdFromIntent != -1 && tabUrlFromIntent != null) {
            currentTabId = tabIdFromIntent;
            currentUrl = tabUrlFromIntent;
            currentTitle = intent.getStringExtra("tab_title");
            loadUrl(currentUrl);
            return;
        }

        // 2. EXTERNAL: ACTION_VIEW from other apps / browser intent
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String url = data.toString();
                if (!TextUtils.isEmpty(url)) {
                    // create new tab for external link
                    int newId = dataManager.createNewTab(url, url);
                    currentTabId = newId;
                    currentUrl = url;
                    currentTitle = url;
                    loadUrl(url);
                    updateTabCount();
                    return;
                }
            }
        }

        // 3. EXTERNAL: ACTION_SEND text shared from other app
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(sharedText)) {
                String url = extractUrlFromText(sharedText);
                if (url != null) {
                    int newId = dataManager.createNewTab(url, url);
                    currentTabId = newId;
                    currentUrl = url;
                    currentTitle = url;
                    loadUrl(url);
                    updateTabCount();
                    return;
                } else {
                    // treat as search query
                    performSearch(sharedText);
                    return;
                }
            }
        }

        // 4. WebRouter
        WebRouter.RouterData rd = WebRouter.receive(this, intent);
        if (rd != null && rd.hasData()) {
            if (!TextUtils.isEmpty(rd.url)) {
                loadUrl(rd.url);
                return;
            }
        }

        // 5. DEFAULT: last tab
        loadDefaultOrLastTab();
    }

    private void loadDefaultOrLastTab() {
        if (dataManager != null && dataManager.getTabCount() > 0) {
            DataManager.TabItem t = dataManager.getTabAt(0);
            if (t != null && !TextUtils.isEmpty(t.getUrl())) {
                currentTabId = t.getId();
                currentUrl = t.getUrl();
                currentTitle = t.getTitle();
                loadUrl(t.getUrl());
                return;
            }
        }
        String home = searchManager.getCurrentEngineHomeUrl();
        if (TextUtils.isEmpty(home)) home = "https://www.google.com";
        currentTabId = dataManager.createNewTab("New Tab", home);
        currentUrl = home;
        currentTitle = "New Tab";
        loadUrl(home);
    }

    private String extractUrlFromText(String text) {
        if (text == null) return null;
        if (text.startsWith("http://") || text.startsWith("https://")) return text;
        // simple regex
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(https?://[^\\s]+)").matcher(text);
        if (m.find()) return m.group(1);
        return null;
    }

    private void loadUrl(String url) {
        if (TextUtils.isEmpty(url)) return;
        if (searchEditText != null) searchEditText.setText(url);
        if (webView != null) webView.loadUrl(url);
    }

    private void performSearch(String q) {
        if (TextUtils.isEmpty(q.trim())) {
            showToast("Enter search query");
            return;
        }
        hideKeyboard();
        String searchUrl = searchManager.buildSearchUrl(q);
        // create new tab for search if needed
        if (currentTabId == -1) {
            currentTabId = dataManager.createNewTab(q, searchUrl);
        }
        loadUrl(searchUrl);
    }

    private void saveCurrentTab() {
        if (currentTabId != -1 && !TextUtils.isEmpty(currentUrl)) {
            String t = TextUtils.isEmpty(currentTitle) ? currentUrl : currentTitle;
            dataManager.updateTab(currentTabId, t, currentUrl);
        }
    }

    private void updateCurrentTab() {
        if (currentTabId != -1 && !TextUtils.isEmpty(currentUrl)) {
            String t = TextUtils.isEmpty(currentTitle) ? currentUrl : currentTitle;
            dataManager.updateTab(currentTabId, t, currentUrl);
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
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    private void showError(String msg) {
        if (errorView != null) {
            errorView.setVisibility(View.VISIBLE);
            errorView.bringToFront();
        }
        if (errorMessage != null) errorMessage.setText(msg);
    }

    private void hideError() {
        if (errorView != null) errorView.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View f = getCurrentFocus();
        if (imm != null && f != null) {
            imm.hideSoftInputFromWindow(f.getWindowToken(), 0);
            f.clearFocus();
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void showToast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    private void startEntranceAnimations() {
        if (topBar != null) {
            topBar.setAlpha(0f);
            topBar.setTranslationY(-20f);
            topBar.animate().alpha(1f).translationY(0f).setDuration(350).setInterpolator(new DecelerateInterpolator()).start();
        }
        if (webView != null) {
            webView.setAlpha(0f);
            webView.animate().alpha(1f).setDuration(450).setStartDelay(150).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    private void animateMenuButton() {
        if (menuButton != null)
            menuButton.animate().rotation(90f).setDuration(200).withEndAction(() -> menuButton.animate().rotation(0f).setDuration(200).start()).start();
    }

    private void animateRefreshButton() {
        if (refreshButton != null)
            refreshButton.animate().rotation(360f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    private void animateButtonClick(View b) {
        if (b == null) return;
        b.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).withEndAction(() -> b.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
    }

    private void animateDrawerItem(View v) {
        if (v == null) return;
        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(150).setInterpolator(new DecelerateInterpolator()).start()).start();
    }

    private void animateStatusChange(TextView v) {
        if (v == null) return;
        v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(120).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
    }

    // ========= FIXED TAB OPEN FROM TabActivity =========
    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == TAB_MANAGER_REQUEST && res == RESULT_OK && data != null) {
            int id = data.getIntExtra("tab_id", -1);
            String url = data.getStringExtra("tab_url");
            String title = data.getStringExtra("tab_title");
            if (id != -1 && url != null && !url.isEmpty()) {
                saveCurrentTab();
                currentTabId = id;
                currentTitle = title != null ? title : url;
                currentUrl = url;
                if (searchEditText != null) searchEditText.setText(url);
                if (webView != null) webView.loadUrl(url);
                updateTabCount();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
        updateTabCount();
        loadSettingsToDrawer();
        if (currentTabId != -1) updateCurrentTab();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
        saveCurrentTab();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (webView != null) webView.destroy();
        if (downloadReceiver != null) try {
            unregisterReceiver(downloadReceiver);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else if (webView != null && webView.canGoBack()) webView.goBack();
        else {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private class JavaScriptInterface {
        @android.webkit.JavascriptInterface
        public void downloadMedia(String url, String type) {
            runOnUiThread(() -> {
                if (!TextUtils.isEmpty(url)) startDownload(url);
            });
        }
    }
}