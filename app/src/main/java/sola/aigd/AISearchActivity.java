package sola.aigd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AISearchActivity extends AppCompatActivity {

    private EditText aiSearchBox;
    private WebView aiWebView;
    private View emptyState;
    private ProgressBar progressBar;
    private CardView backButton, searchActionButton;
    private AnimatedBackground animatedBg;

    private String currentEngine = "perplexity";
    private final Map<String, String> engines = new HashMap<>();
    private final Map<String, CardView> chipCards = new HashMap<>();
    private final Map<String, TextView> chipTexts = new HashMap<>();

    private static final String PREFS = "sola_ai_prefs";
    private static final String KEY_HISTORY = "ai_history";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge to Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_ai_search);

        // ✅ FIX STATUS BAR MIXING - Insets handler
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            View topBar = findViewById(R.id.topBar);
            if (topBar!= null) {
                topBar.setPadding(topBar.getPaddingLeft(), bars.top, topBar.getPaddingRight(), topBar.getPaddingBottom());
            }
            // Web container bottom inset for nav bar
            View webContainer = findViewById(R.id.webContainer);
            if (webContainer!= null) {
                webContainer.setPadding(0,0,0, bars.bottom);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        initEngines();
        bindViews();
        setupWebView();
        setupChips();
        setupSearch();
        setupSuggestions();
        handleIncomingIntent();
    }

    private void initEngines() {
        engines.put("perplexity", "https://www.perplexity.ai/search?q=");
        engines.put("you", "https://you.com/search?q=");
        engines.put("phind", "https://www.phind.com/search?q=");
        engines.put("gemini", "https://www.google.com/search?udm=50&q=");
        engines.put("chatgpt", "https://chatgpt.com/?q=");
    }

    private void bindViews() {
        aiSearchBox = findViewById(R.id.aiSearchBox);
        aiWebView = findViewById(R.id.aiWebView);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backButton);
        searchActionButton = findViewById(R.id.searchActionButton);
        animatedBg = findViewById(R.id.animatedBg);

        // Animated bg is inside emptyState? make sure its behind
        // If animatedBg is separate, keep it running

        chipCards.put("perplexity", findViewById(R.id.chipPerplexity));
        chipCards.put("you", findViewById(R.id.chipYou));
        chipCards.put("phind", findViewById(R.id.chipPhind));
        chipCards.put("gemini", findViewById(R.id.chipGemini));
        chipCards.put("chatgpt", findViewById(R.id.chipChatGPT));

        chipTexts.put("perplexity", findViewById(R.id.textPerplexity));
        chipTexts.put("you", findViewById(R.id.textYou));
        chipTexts.put("phind", findViewById(R.id.textPhind));
        chipTexts.put("gemini", findViewById(R.id.textGemini));
        chipTexts.put("chatgpt", findViewById(R.id.textChatGPT));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = aiWebView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setAllowFileAccess(false);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 Sola/4.0");

        aiWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                // Hide animated bg when loading to save GPU
                if (animatedBg!= null) animatedBg.pauseAnimation();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AISearchActivity.this, "Failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    if (animatedBg!= null) animatedBg.resumeAnimation();
                }
            }
        });

        aiWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(newProgress < 100? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupChips() {
        for (Map.Entry<String, CardView> entry : chipCards.entrySet()) {
            String key = entry.getKey();
            CardView card = entry.getValue();
            if (card == null) continue;
            card.setOnClickListener(v -> {
                currentEngine = key;
                refreshChipUI();
                String q = aiSearchBox.getText().toString().trim();
                if (!q.isEmpty()) performSearch(q);
                else Toast.makeText(this, getEngineName(key) + " selected", Toast.LENGTH_SHORT).show();
            });
        }
        refreshChipUI();
    }

    private String getEngineName(String key) {
        switch (key) {
            case "perplexity": return "Perplexity";
            case "you": return "You.com";
            case "phind": return "Phind";
            case "gemini": return "Gemini";
            case "chatgpt": return "ChatGPT";
            default: return key;
        }
    }

    private void refreshChipUI() {
        for (Map.Entry<String, CardView> entry : chipCards.entrySet()) {
            String key = entry.getKey();
            CardView card = entry.getValue();
            TextView tv = chipTexts.get(key);
            if (card == null || tv == null) continue;
            if (key.equals(currentEngine)) {
                card.setCardBackgroundColor(getColor(R.color.f));
                tv.setTextColor(getColor(R.color.b));
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                card.setCardBackgroundColor(0xFFF1F3F4);
                tv.setTextColor(getColor(R.color.t));
                tv.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    private void setupSearch() {
        backButton.setOnClickListener(v -> onBackPressedCustom());
        searchActionButton.setOnClickListener(v -> triggerSearch());
        aiSearchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event!= null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                triggerSearch();
                return true;
            }
            return false;
        });
    }

    // ✅ FIXED SUGGESTIONS - 100% WORKING WITH NEW IDs
    private void setupSuggestions() {
        int[] cardIds = {R.id.suggestionCard1, R.id.suggestionCard2, R.id.suggestionCard3, R.id.suggestionCard4};
        int[] textIds = {R.id.suggestionText1, R.id.suggestionText2, R.id.suggestionText3, R.id.suggestionText4};

        for (int i = 0; i < cardIds.length; i++) {
            CardView card = findViewById(cardIds[i]);
            TextView tv = findViewById(textIds[i]);
            if (card!= null && tv!= null) {
                card.setOnClickListener(v -> {
                    String raw = tv.getText().toString();
                    String query = raw.replaceAll("^[^\\p{L}\\p{N}]+", "").trim();
                    aiSearchBox.setText(query);
                    aiSearchBox.setSelection(query.length());
                    performSearch(query);
                });
            }
        }
    }

    private void triggerSearch() {
        String q = aiSearchBox.getText().toString().trim();
        if (q.isEmpty()) {
            aiSearchBox.setError("Ask something");
            return;
        }
        if (!isOnline()) {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
            return;
        }
        performSearch(q);
    }

    private void performSearch(String query) {
        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            String base = engines.get(currentEngine);
            if (base == null) base = engines.get("perplexity");
            String url = base + encoded;

            emptyState.setVisibility(View.GONE);
            aiWebView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            aiWebView.loadUrl(url);
            hideKeyboard();
            saveHistory(query);
            if (animatedBg!= null) animatedBg.pauseAnimation();

        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "Encoding error", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveHistory(String query) {
        try {
            Set<String> history = new HashSet<>(prefs.getStringSet(KEY_HISTORY, new HashSet<>()));
            history.add(query + " | " + currentEngine + " | " + System.currentTimeMillis());
            if (history.size() > 100) history.clear();
            prefs.edit().putStringSet(KEY_HISTORY, history).apply();
        } catch (Exception ignored) {}
    }

    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni!= null && ni.isConnected();
        } catch (Exception e) { return true; }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View v = getCurrentFocus();
            if (v == null) v = aiSearchBox;
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    private void handleIncomingIntent() {
        String q = getIntent().getStringExtra("query");
        String engine = getIntent().getStringExtra("engine");
        if (engine!= null && engines.containsKey(engine)) {
            currentEngine = engine;
            refreshChipUI();
        }
        if (q!= null &&!q.isEmpty()) {
            aiSearchBox.setText(q);
            performSearch(q);
        }
    }

    private void onBackPressedCustom() {
        if (aiWebView.getVisibility() == View.VISIBLE && aiWebView.canGoBack()) {
            aiWebView.goBack();
        } else if (aiWebView.getVisibility() == View.VISIBLE) {
            aiWebView.setVisibility(View.GONE);
            aiWebView.loadUrl("about:blank");
            emptyState.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            if (animatedBg!= null) animatedBg.resumeAnimation();
        } else {
            finish();
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        }
    }

    @Override
    public void onBackPressed() { onBackPressedCustom(); }

    @Override
    protected void onResume() {
        super.onResume();
        if (animatedBg!= null && emptyState.getVisibility() == View.VISIBLE) {
            animatedBg.resumeAnimation();
        }
        aiWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animatedBg!= null) animatedBg.pauseAnimation();
        aiWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (animatedBg!= null) animatedBg.stopAnimation();
        if (aiWebView!= null) {
            aiWebView.loadUrl("about:blank");
            aiWebView.stopLoading();
            aiWebView.setWebChromeClient(null);
            aiWebView.setWebViewClient(null);
            aiWebView.destroy();
        }
        super.onDestroy();
    }
}