package sola.aigd;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final int VOICE_REQUEST_CODE = 1001;
    private static final long ANIMATION_DELAY = 300;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private AnimatedBackground animatedBackground;
    private ImageView logo, searchIcon, voiceSearch, engineSelector;
    private TextView appName, tagline, currentEngineName, footerText, tabBadge, adLabel;
    private EditText searchInput;
    private CardView searchCard;
    private LinearLayout quickTabs, quickHistory, quickPrivacy, quickQR, quickShare, engineContainer, adContainer;
    private GridLayout quickActionsGrid;
    private Banner bannerAd;
    private ProgressBar adLoader;

    private ValueAnimator logoPulse;

    private SEManager seManager;
    private WebRouter webRouter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeCore();
        initializeViews();
        setupWindow();
        setupBackground();
        setupClickListeners();
        setupSearchTextWatcher();
        loadSettingsToUI();
        setupDynamicTagline();
        startEntryAnimations();
        startLogoPulse();
        loadBannerAd();
        updateTabBadge();
        handleIntent(getIntent());
    }

    private void initializeCore() {
        seManager = SEManager.getInstance(this);
        webRouter = WebRouter.getInstance(this);
    }

    private void initializeViews() {
        animatedBackground = findViewById(R.id.animatedBackground);
        logo = findViewById(R.id.logo);
        appName = findViewById(R.id.appName);
        tagline = findViewById(R.id.tagline);
        searchCard = findViewById(R.id.searchCard);
        searchInput = findViewById(R.id.searchInput);
        searchIcon = findViewById(R.id.searchIcon);
        voiceSearch = findViewById(R.id.voiceSearch);
        quickActionsGrid = findViewById(R.id.quickActionsGrid);
        quickTabs = findViewById(R.id.quickTabs);
        quickHistory = findViewById(R.id.quickHistory);
        quickPrivacy = findViewById(R.id.quickPrivacy);
        quickQR = findViewById(R.id.quickQR);
        quickShare = findViewById(R.id.quickShare);
        tabBadge = findViewById(R.id.tabBadge);
        engineContainer = findViewById(R.id.engineContainer);
        currentEngineName = findViewById(R.id.currentEngineName);
        engineSelector = findViewById(R.id.engineSelector);
        footerText = findViewById(R.id.footerText);
        adContainer = findViewById(R.id.adContainer);
        bannerAd = findViewById(R.id.bannerAd);
        adLoader = findViewById(R.id.adLoader);
        adLabel = findViewById(R.id.adLabel);
    }

    private void setupWindow() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView()
        );
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            );
        }
    }

    private void setupBackground() {
        if (animatedBackground != null) {
            animatedBackground.setWaveSpeed(5000);
            animatedBackground.resumeAnimation();
        }
    }

    private void setupClickListeners() {
        searchIcon.setOnClickListener(v -> performSearch());

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_GO) {
                performSearch();
                return true;
            }
            return false;
        });

        voiceSearch.setOnClickListener(v -> startVoiceSearch());

        engineContainer.setOnClickListener(v -> showEnginePicker());
        engineSelector.setOnClickListener(v -> showEnginePicker());

        quickTabs.setOnClickListener(v -> {
            animateButtonClick(quickTabs);
            Intent intent = new Intent(HomeActivity.this, TabActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        quickHistory.setOnClickListener(v -> {
            animateButtonClick(quickHistory);
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        quickPrivacy.setOnClickListener(v -> {
            animateButtonClick(quickPrivacy);
            Intent intent = new Intent(this, PrivacyActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        quickQR.setOnClickListener(v -> {
            animateButtonClick(quickQR);
            Intent intent = new Intent(this, QRActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        quickShare.setOnClickListener(v -> {
            animateButtonClick(quickShare);
            shareApp();
        });

        logo.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.requestFocus();
            animateViewPop(logo);
        });

        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                searchCard.animate()
                        .scaleX(1.02f)
                        .scaleY(1.02f)
                        .setDuration(200)
                        .start();
            } else {
                searchCard.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start();
            }
        });
    }

    private void setupSearchTextWatcher() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String url = data.toString();
                handler.postDelayed(() -> {
                    webRouter.open(url);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }, ANIMATION_DELAY);
            }
        }
    }

    private void setupDynamicTagline() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String timeTagline;

        if (hour >= 5 && hour < 12) {
            timeTagline = "Good Morning! What's on your mind?";
        } else if (hour >= 12 && hour < 17) {
            timeTagline = "Good Afternoon! Ready to explore?";
        } else if (hour >= 17 && hour < 22) {
            timeTagline = "Good Evening! What would you like to find?";
        } else {
            timeTagline = "The web never sleeps. Search anything!";
        }

        tagline.setText(timeTagline);
    }

    private void loadSettingsToUI() {
        if (currentEngineName != null && seManager != null) {
            currentEngineName.setText(seManager.getCurrentEngineName());
        }
    }

    private void updateTabBadge() {
        if (tabBadge == null) return;
        int tabCount = getTabCountFromPrefs();
        if (tabCount > 0) {
            tabBadge.setVisibility(View.VISIBLE);
            tabBadge.setText(String.valueOf(tabCount));
        } else {
            tabBadge.setVisibility(View.GONE);
        }
    }

    private int getTabCountFromPrefs() {
        return 0;
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();

        if (query.isEmpty()) {
            animateShake(searchCard);
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();
        animateButtonClick(searchIcon);

        if (isValidUrl(query)) {
            String url = formatUrl(query);
            webRouter.open(url);
        } else {
            webRouter.search(query);
        }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private String formatUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    private boolean isValidUrl(String input) {
        try {
            Uri uri = Uri.parse(input);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
                return true;
            }

            if (host != null && host.contains(".")) {
                return true;
            }
        } catch (Exception e) {
            // Not a valid URL
        }

        return input.contains(".") &&
                !input.contains(" ") &&
                (input.endsWith(".com") || input.endsWith(".org") ||
                        input.endsWith(".net") || input.endsWith(".edu") ||
                        input.endsWith(".gov") || input.endsWith(".io") ||
                        input.endsWith(".co") || input.endsWith(".xyz") ||
                        input.endsWith(".info") || input.endsWith(".me") ||
                        input.endsWith(".tv") || input.endsWith(".app") ||
                        input.endsWith(".uk") || input.endsWith(".de") ||
                        input.endsWith(".fr") || input.endsWith(".jp"));
    }

    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your search...");

        try {
            animateViewClick(voiceSearch);
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice search not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEnginePicker() {
        String[] engines = seManager.getEngineNames();
        int currentIndex = seManager.getCurrentEngineIndex();

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_engine_picker, null);
        LinearLayout engineListContainer = dialogView.findViewById(R.id.engineListContainer);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        for (int i = 0; i < engines.length; i++) {
            View engineItem = getLayoutInflater().inflate(R.layout.item_engine,
                    engineListContainer, false);

            TextView engineName = engineItem.findViewById(R.id.engineName);
            ImageView engineIcon = engineItem.findViewById(R.id.engineIcon);
            FrameLayout selectedIcon = engineItem.findViewById(R.id.selectedIcon);

            engineName.setText(engines[i]);

            String engine = engines[i].toLowerCase();
            if (engine.contains("google")) {
                engineIcon.setImageResource(R.drawable.ic_search);
            } else if (engine.contains("duck")) {
                engineIcon.setImageResource(R.drawable.ic_search);
            } else if (engine.contains("bing")) {
                engineIcon.setImageResource(R.drawable.ic_search);
            } else if (engine.contains("yahoo")) {
                engineIcon.setImageResource(R.drawable.ic_search);
            } else if (engine.contains("brave")) {
                engineIcon.setImageResource(R.drawable.ic_search);
            } else {
                engineIcon.setImageResource(R.drawable.ic_search);
            }

            if (i == currentIndex) {
                selectedIcon.setVisibility(View.VISIBLE);
            } else {
                selectedIcon.setVisibility(View.GONE);
            }

            final int selectedIndex = i;
            engineItem.setOnClickListener(v -> {
                seManager.setCurrentEngineIndex(selectedIndex);
                loadSettingsToUI();
                animateEngineChange();
                Toast.makeText(HomeActivity.this,
                        "Switched to " + engines[selectedIndex],
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            engineListContainer.addView(engineItem);
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Check out SOLA Browser - Secure, Fast, and Private!\n" +
                        "Download now: https://play.google.com/store/apps/details?id=sola.aigd");
        startActivity(Intent.createChooser(shareIntent, "Share SOLA"));
    }

    private void loadBannerAd() {
        if (bannerAd == null) {
            return;
        }

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

        try {
            bannerAd.loadAd();
        } catch (Exception e) {
            e.printStackTrace();
            adLoader.setVisibility(View.GONE);
            bannerAd.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && searchInput != null && searchInput.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }

    private void startLogoPulse() {
        if (logo == null) return;

        logoPulse = ValueAnimator.ofFloat(1f, 1.06f, 1f);
        logoPulse.setDuration(2000);
        logoPulse.setRepeatCount(ValueAnimator.INFINITE);
        logoPulse.setRepeatMode(ValueAnimator.REVERSE);
        logoPulse.setInterpolator(new AccelerateDecelerateInterpolator());
        logoPulse.addUpdateListener(anim -> {
            float val = (float) anim.getAnimatedValue();
            logo.setScaleX(val);
            logo.setScaleY(val);
        });
        logoPulse.start();
    }

    private void startRingAnimations() {
        // Rings removed from XML - no animations needed
    }

    private void startEntryAnimations() {
        if (appName != null) {
            appName.setAlpha(0f);
            appName.setTranslationY(30f);
            appName.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (tagline != null) {
            tagline.setAlpha(0f);
            tagline.setTranslationY(20f);
            tagline.animate()
                    .alpha(0.8f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(400)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (searchCard != null) {
            searchCard.setAlpha(0f);
            searchCard.setTranslationY(50f);
            searchCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(600)
                    .setInterpolator(new OvershootInterpolator(1f))
                    .start();
        }

        if (quickActionsGrid != null) {
            quickActionsGrid.setAlpha(0f);
            quickActionsGrid.setScaleX(0.95f);
            quickActionsGrid.setScaleY(0.95f);
            quickActionsGrid.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .setStartDelay(800)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (engineContainer != null) {
            engineContainer.setAlpha(0f);
            engineContainer.setScaleX(0.9f);
            engineContainer.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setDuration(400)
                    .setStartDelay(1000)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (footerText != null) {
            footerText.setAlpha(0f);
            footerText.setTranslationY(20f);
            footerText.animate()
                    .alpha(0.4f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(1100)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void animateButtonClick(View button) {
        if (button == null) return;
        button.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(80)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start())
                .start();
    }

    private void animateViewClick(View view) {
        if (view == null) return;
        view.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(80)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start())
                .start();
    }

    private void animateViewPop(View view) {
        if (view == null) return;
        view.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(150)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start())
                .start();
    }

    private void animateEngineChange() {
        if (engineSelector == null || currentEngineName == null) return;

        engineSelector.animate()
                .rotation(360f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        currentEngineName.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(150)
                .withEndAction(() -> currentEngineName.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start())
                .start();
    }

    private void animateShake(View view) {
        if (view == null) return;
        view.animate()
                .translationX(-12f)
                .setDuration(50)
                .withEndAction(() -> view.animate()
                        .translationX(12f)
                        .setDuration(50)
                        .withEndAction(() -> view.animate()
                                .translationX(-6f)
                                .setDuration(50)
                                .withEndAction(() -> view.animate()
                                        .translationX(6f)
                                        .setDuration(50)
                                        .withEndAction(() -> view.animate()
                                                .translationX(0f)
                                                .setDuration(50)
                                                .start())
                                        .start())
                                .start())
                        .start())
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                searchInput.setText(result.get(0));
                searchInput.setSelection(result.get(0).length());
                performSearch();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettingsToUI();
        searchInput.clearFocus();
        updateTabBadge();

        if (animatedBackground != null) {
            animatedBackground.resumeAnimation();
        }

        if (logoPulse != null && logoPulse.isPaused()) {
            logoPulse.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animatedBackground != null) {
            animatedBackground.pauseAnimation();
        }

        if (logoPulse != null && logoPulse.isRunning()) {
            logoPulse.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

        if (logoPulse != null) {
            logoPulse.cancel();
        }

        if (animatedBackground != null) {
            animatedBackground.stopBackgroundAnimation();
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}