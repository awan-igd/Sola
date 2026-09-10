package sola.aigd;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    private static final int VOICE_REQUEST_CODE = 1001;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private View topGlow, sideBg, sideDecor1, sideDecor2, lineView;
    private CardView logoCard, searchCard, aiSearchCard;
    private ImageView logo, searchIcon, voiceSearch, engineSelector;
    private TextView appName, tagline, quickTitle, currentEngineName, footerText, tabBadge, adLabel;
    private EditText searchInput;
    private LinearLayout quickTabs, quickHistory, quickPrivacy, quickQR, quickShare, quickAbout, engineContainer, adContainer;
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
        sideBg = findViewById(R.id.sideBg);
        topGlow = findViewById(R.id.topGlow);
        sideDecor1 = findViewById(R.id.sideDecor1);
        sideDecor2 = findViewById(R.id.sideDecor2);
        logoCard = findViewById(R.id.logoCard);
        logo = findViewById(R.id.logo);
        appName = findViewById(R.id.appName);
        tagline = findViewById(R.id.tagline);
        lineView = findViewById(R.id.lineView);
        searchCard = findViewById(R.id.searchCard);
        aiSearchCard = findViewById(R.id.aiSearchCard);
        searchInput = findViewById(R.id.searchInput);
        searchIcon = findViewById(R.id.searchIcon);
        voiceSearch = findViewById(R.id.voiceSearch);
        quickTitle = findViewById(R.id.quickTitle);
        quickActionsGrid = findViewById(R.id.quickActionsGrid);
        quickTabs = findViewById(R.id.quickTabs);
        quickHistory = findViewById(R.id.quickHistory);
        quickPrivacy = findViewById(R.id.quickPrivacy);
        quickQR = findViewById(R.id.quickQR);
        quickShare = findViewById(R.id.quickShare);
        quickAbout = findViewById(R.id.quickAbout);
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
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
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
        voiceSearch.setOnClickListener(v -> {
            animateButtonClick(voiceSearch);
            startVoiceSearch();
        });

        // AI SEARCH CARD - MAIN
        if (aiSearchCard != null) {
            aiSearchCard.setOnClickListener(v -> {
                animateButtonClick(aiSearchCard);
                openAISearch(null);
            });
        }

        engineContainer.setOnClickListener(v -> {
            animateButtonClick(engineContainer);
            showEnginePicker();
        });
        quickTabs.setOnClickListener(v -> {
            animateButtonClick(quickTabs);
            startActivity(new Intent(this, TabActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        quickHistory.setOnClickListener(v -> {
            animateButtonClick(quickHistory);
            startActivity(new Intent(this, HistoryActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        quickPrivacy.setOnClickListener(v -> {
            animateButtonClick(quickPrivacy);
            startActivity(new Intent(this, PrivacyActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        quickQR.setOnClickListener(v -> {
            animateButtonClick(quickQR);
            startActivity(new Intent(this, QRActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        quickShare.setOnClickListener(v -> {
            animateButtonClick(quickShare);
            shareApp();
        });
        quickAbout.setOnClickListener(v -> {
            animateButtonClick(quickAbout);
            startActivity(new Intent(this, AboutActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        logoCard.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.requestFocus();
            animateViewPop(logoCard);
        });
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                searchCard.animate().scaleX(1.03f).scaleY(1.03f).translationZ(14f).setDuration(220).setInterpolator(new OvershootInterpolator()).start();
            else
                searchCard.animate().scaleX(1f).scaleY(1f).translationZ(6f).setDuration(220).setInterpolator(new DecelerateInterpolator()).start();
        });
    }

    private void openAISearch(String prefillQuery) {
        Intent i = new Intent(this, AISearchActivity.class);
        if (prefillQuery != null && !prefillQuery.isEmpty()) {
            i.putExtra("query", prefillQuery);
        }
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void startEntryAnimations() {
        sideBg.setTranslationX(-250f);
        sideBg.setAlpha(0f);
        if (topGlow != null) {
            topGlow.setAlpha(0f);
            topGlow.setScaleX(0.5f);
            topGlow.setScaleY(0.5f);
        }
        if (sideDecor1 != null) {
            sideDecor1.setAlpha(0f);
            sideDecor1.setScaleX(0.5f);
            sideDecor1.setScaleY(0.5f);
        }
        if (sideDecor2 != null) {
            sideDecor2.setAlpha(0f);
            sideDecor2.setScaleX(0.5f);
            sideDecor2.setScaleY(0.5f);
        }
        logoCard.setAlpha(0f);
        logoCard.setScaleX(0.3f);
        logoCard.setScaleY(0.3f);
        logoCard.setTranslationY(30f);
        appName.setAlpha(0f);
        appName.setTranslationX(-30f);
        tagline.setAlpha(0f);
        tagline.setTranslationX(-30f);
        lineView.setScaleX(0f);
        lineView.setAlpha(0f);
        searchCard.setAlpha(0f);
        searchCard.setTranslationY(50f);
        searchCard.setScaleX(0.85f);
        if (aiSearchCard != null) {
            aiSearchCard.setAlpha(0f);
            aiSearchCard.setTranslationY(50f);
            aiSearchCard.setScaleX(0.85f);
        }
        quickTitle.setAlpha(0f);
        quickTitle.setTranslationX(-20f);
        quickActionsGrid.setAlpha(1f);
        quickActionsGrid.setVisibility(View.VISIBLE);
        quickActionsGrid.setTranslationY(10f);
        engineContainer.setAlpha(0f);
        engineContainer.setScaleX(0.8f);
        engineContainer.setTranslationY(10f);
        footerText.setAlpha(0f);
        footerText.setTranslationY(10f);

        sideBg.animate().translationX(0f).alpha(1f).setDuration(750).setStartDelay(0).setInterpolator(new DecelerateInterpolator()).start();
        if (sideDecor1 != null)
            sideDecor1.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800).setStartDelay(200).setInterpolator(new DecelerateInterpolator()).start();
        if (sideDecor2 != null)
            sideDecor2.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800).setStartDelay(350).setInterpolator(new DecelerateInterpolator()).start();
        if (topGlow != null)
            topGlow.animate().alpha(0.12f).scaleX(1f).scaleY(1f).setDuration(1100).setStartDelay(100).setInterpolator(new DecelerateInterpolator()).start();

        logoCard.animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0f).setDuration(750).setStartDelay(250).setInterpolator(new OvershootInterpolator(1.4f)).start();
        appName.animate().alpha(1f).translationX(0f).setDuration(500).setStartDelay(450).setInterpolator(new DecelerateInterpolator()).start();
        tagline.animate().alpha(0.9f).translationX(0f).setDuration(500).setStartDelay(520).setInterpolator(new DecelerateInterpolator()).start();
        lineView.animate().scaleX(1f).alpha(0.9f).setDuration(400).setStartDelay(650).setInterpolator(new DecelerateInterpolator()).start();
        searchCard.animate().alpha(1f).translationY(0f).scaleX(1f).setDuration(700).setStartDelay(680).setInterpolator(new OvershootInterpolator(0.85f)).start();
        if (aiSearchCard != null) {
            aiSearchCard.animate().alpha(1f).translationY(0f).scaleX(1f).setDuration(700).setStartDelay(780).setInterpolator(new OvershootInterpolator(0.85f)).start();
        }
        quickTitle.animate().alpha(0.55f).translationX(0f).setDuration(400).setStartDelay(950).start();
        quickActionsGrid.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(1000).setInterpolator(new DecelerateInterpolator()).start();

        for (int i = 0; i < quickActionsGrid.getChildCount(); i++) {
            View child = quickActionsGrid.getChildAt(i);
            child.setAlpha(0f);
            child.setScaleX(0.2f);
            child.setScaleY(0.2f);
            child.setTranslationY(40f);
            child.setVisibility(View.VISIBLE);
            child.animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0f).setDuration(500).setStartDelay(1050 + (i * 65L)).setInterpolator(new OvershootInterpolator(1.2f)).start();
        }
        engineContainer.animate().alpha(1f).scaleX(1f).translationY(0f).setDuration(450).setStartDelay(1450).setInterpolator(new DecelerateInterpolator()).start();
        footerText.animate().alpha(0.6f).translationY(0f).setDuration(400).setStartDelay(1520).setInterpolator(new DecelerateInterpolator()).start();
    }

    private void animateButtonClick(View view) {
        if (view == null) return;
        view.animate().scaleX(0.88f).scaleY(0.88f).setDuration(80).withEndAction(() -> {
            view.animate().scaleX(1f).scaleY(1f).setDuration(350).setInterpolator(new OvershootInterpolator(2f)).start();
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
        }).start();
    }

    private void animateViewPop(View view) {
        if (view == null) return;
        view.animate().scaleX(1.12f).scaleY(1.12f).setDuration(130).withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(300).setInterpolator(new OvershootInterpolator(1.8f)).start()).start();
    }

    private void startLogoPulse() {
        if (logo == null) return;
        logoPulse = ValueAnimator.ofFloat(1f, 1.07f, 1f);
        logoPulse.setDuration(2600);
        logoPulse.setRepeatCount(ValueAnimator.INFINITE);
        logoPulse.setInterpolator(new AccelerateDecelerateInterpolator());
        logoPulse.addUpdateListener(a -> {
            float v = (float) a.getAnimatedValue();
            logo.setScaleX(v);
            logo.setScaleY(v);
        });
        logoPulse.start();
    }

    private void animateShake(View view) {
        view.animate().translationX(-10f).setDuration(45).withEndAction(() -> view.animate().translationX(10f).setDuration(45).withEndAction(() -> view.animate().translationX(-6f).setDuration(45).withEndAction(() -> view.animate().translationX(0f).setDuration(45).start()).start()).start()).start();
    }

    private void setupSearchTextWatcher() {
        searchInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSettingsToUI() {
        if (currentEngineName != null && seManager != null)
            currentEngineName.setText(seManager.getCurrentEngineName());
    }

    private void setupDynamicTagline() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String[] arr;
        if (hour >= 5 && hour < 12)
            arr = new String[]{"Good Morning! What's on your mind?", "Morning! Ready to explore?", "Rise & search, early bird!", "Good Morning! Fresh start."};
        else if (hour >= 12 && hour < 17)
            arr = new String[]{"Good Afternoon! Ready to explore?", "Afternoon! Let's find something.", "Good Afternoon! Search anything."};
        else if (hour >= 17 && hour < 21)
            arr = new String[]{"Good Evening! What would you like to find?", "Evening! Time to discover.", "Good Evening! What's next?"};
        else
            arr = new String[]{"The web never sleeps. Search anything!", "Late night search? We got you.", "Night Owl? Let's explore."};
        tagline.setText(arr[random.nextInt(arr.length)]);
    }

    private void updateTabBadge() {
        if (tabBadge == null) return;
        int count = 0;
        if (count > 0) {
            tabBadge.setVisibility(View.VISIBLE);
            tabBadge.setText(String.valueOf(count));
        } else tabBadge.setVisibility(View.GONE);
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            animateShake(searchCard);
            Toast.makeText(this, "Enter search query", Toast.LENGTH_SHORT).show();
            return;
        }
        hideKeyboard();
        animateButtonClick(searchIcon);

        // AI trigger: if query starts with ai: or /ai or ?
        if (query.toLowerCase().startsWith("ai:") || query.toLowerCase().startsWith("ask ai") || query.startsWith("?")) {
            String aiQuery = query.replaceFirst("(?i)ai:|ask ai|\\?", "").trim();
            if (aiQuery.isEmpty()) aiQuery = query;
            openAISearch(aiQuery);
            return;
        }

        if (isValidUrl(query)) webRouter.open(formatUrl(query));
        else webRouter.search(query);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private String formatUrl(String url) {
        return (!url.startsWith("http://") && !url.startsWith("https://")) ? "https://" + url : url;
    }

    private boolean isValidUrl(String input) {
        try {
            Uri uri = Uri.parse(input);
            if (uri.getHost() != null && uri.getHost().contains(".")) return true;
        } catch (Exception ignored) {}
        return input.contains(".") && !input.contains(" ") && (input.endsWith(".com") || input.endsWith(".org") || input.endsWith(".net") || input.endsWith(".io") || input.endsWith(".pk"));
    }

    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your search...");
        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice search not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEnginePicker() {
        List<SEManager.Engine> engines = seManager.getFilteredEngines();
        int currentIdx = seManager.getCurrentEngineIndex();

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_engine_picker, null);
        LinearLayout container = dialogView.findViewById(R.id.engineListContainer);
        View closeBtn = dialogView.findViewById(R.id.closeBtn);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create();

        for (int i = 0; i < engines.size(); i++) {
            SEManager.Engine engine = engines.get(i);
            View item = getLayoutInflater().inflate(R.layout.item_engine, container, false);
            TextView name = item.findViewById(R.id.engineName);
            ImageView icon = item.findViewById(R.id.engineIcon);
            FrameLayout sel = item.findViewById(R.id.selectedIcon);
            View unsel = item.findViewById(R.id.unselectedCircle);
            CardView card = (CardView) item;

            name.setText(engine.name);
            icon.setImageResource(R.drawable.ic_sola);

            boolean isSelected = i == currentIdx;
            if (isSelected) {
                card.setCardBackgroundColor(getColor(R.color.f));
                name.setTextColor(getColor(R.color.b));
                icon.setColorFilter(getColor(R.color.b));
                sel.setVisibility(View.VISIBLE);
                if (unsel != null) unsel.setVisibility(View.GONE);
            } else {
                card.setCardBackgroundColor(getColor(R.color.b));
                name.setTextColor(getColor(R.color.t));
                icon.setColorFilter(getColor(R.color.f));
                sel.setVisibility(View.GONE);
                if (unsel != null) unsel.setVisibility(View.VISIBLE);
            }

            item.setAlpha(0f);
            item.setTranslationY(20f);
            item.animate().alpha(1f).translationY(0f).setDuration(350).setStartDelay(i * 35L).setInterpolator(new DecelerateInterpolator()).start();

            final int idx = i;
            item.setOnClickListener(v -> {
                v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction(() -> {
                    seManager.setCurrentEngineIndex(idx);
                    loadSettingsToUI();
                    dialog.dismiss();
                }).start();
            });
            container.addView(item);
        }

        if (closeBtn != null) closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.92), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void shareApp() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "Check out Sola Browser - Secure, Fast, Private! AI Search Inside!\nhttps://play.google.com/store/apps/details?id=sola.aigd");
        startActivity(Intent.createChooser(i, "Share Sola"));
    }

    private void loadBannerAd() {
        if (bannerAd == null) return;
        adLoader.setVisibility(View.VISIBLE);
        bannerAd.setVisibility(View.GONE);
        adLabel.setVisibility(View.GONE);
        bannerAd.setBannerListener(new BannerListener() {
            public void onReceiveAd(View b) {
                adLoader.setVisibility(View.GONE);
                bannerAd.setVisibility(View.VISIBLE);
                adLabel.setVisibility(View.VISIBLE);
            }
            public void onFailedToReceiveAd(View b) {
                adLoader.setVisibility(View.GONE);
                bannerAd.setVisibility(View.GONE);
                adLabel.setVisibility(View.GONE);
            }
            public void onImpression(View b) {}
            public void onClick(View b) {}
        });
        try {
            bannerAd.loadAd();
        } catch (Exception e) {
            adLoader.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && searchInput.getWindowToken() != null)
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int c, int r, Intent d) {
        super.onActivityResult(c, r, d);
        if (c == VOICE_REQUEST_CODE && r == RESULT_OK && d != null) {
            ArrayList<String> res = d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (res != null && !res.isEmpty()) {
                String spoken = res.get(0);
                searchInput.setText(spoken);
                // If spoken starts with ask AI -> open AI search
                if (spoken.toLowerCase().contains("ask ai") || spoken.toLowerCase().contains("ai search")) {
                    openAISearch(spoken.replaceFirst("(?i)ask ai|ai search", "").trim());
                } else {
                    performSearch();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettingsToUI();
        searchInput.clearFocus();
        updateTabBadge();
        if (logoPulse != null && logoPulse.isPaused()) logoPulse.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (logoPulse != null && logoPulse.isRunning()) logoPulse.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (logoPulse != null) logoPulse.cancel();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) handler.postDelayed(() -> webRouter.open(data.toString()), 300);
        }
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        setIntent(i);
        handleIntent(i);
    }
}