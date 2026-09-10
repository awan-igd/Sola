package sola.aigd;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import java.io.File;

import sola.aigd.R;

public class PrivacyActivity extends AppCompatActivity {

    private CardView backButton, clearHistoryOption, clearCookiesOption, clearCacheOption, clearAllOption;
    private Banner bannerAd;
    private ProgressBar adLoader;
    private TextView adLabel;
    private View topBar;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        setupWindow();
        dataManager = DataManager.getInstance(this);
        initViews();
        setupInsets();
        setupClicks();
        loadAd();
        startAnim();
    }

    private void setupWindow() {
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

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, ins) -> {
            int top = ins.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), top + 12, v.getPaddingRight(), 12);
            return ins;
        });
    }

    private void initViews() {
        topBar = findViewById(R.id.topBar);
        backButton = findViewById(R.id.backButton);
        clearHistoryOption = findViewById(R.id.clearHistoryOption);
        clearCookiesOption = findViewById(R.id.clearCookiesOption);
        clearCacheOption = findViewById(R.id.clearCacheOption);
        clearAllOption = findViewById(R.id.clearAllOption);
        bannerAd = findViewById(R.id.bannerAd);
        adLoader = findViewById(R.id.adLoader);
        adLabel = findViewById(R.id.adLabel);
    }

    private void setupClicks() {
        backButton.setOnClickListener(v -> {
            anim(v);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        clearHistoryOption.setOnClickListener(v -> {
            anim(v);
            dataManager.clearHistory();
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
        });
        clearCookiesOption.setOnClickListener(v -> {
            anim(v);
            android.webkit.CookieManager.getInstance().removeAllCookies(null);
            android.webkit.CookieManager.getInstance().flush();
            Toast.makeText(this, "Cookies cleared", Toast.LENGTH_SHORT).show();
        });
        clearCacheOption.setOnClickListener(v -> {
            anim(v);
            deleteDir(getCacheDir());
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
        });
        clearAllOption.setOnClickListener(v -> {
            anim(v);
            dataManager.clearAllData();
            android.webkit.CookieManager.getInstance().removeAllCookies(null);
            deleteDir(getCacheDir());
            Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] ch = dir.list();
            if (ch != null) for (String c : ch) deleteDir(new File(dir, c));
            return dir.delete();
        } else if (dir != null && dir.isFile()) return dir.delete();
        return false;
    }

    private void startAnim() {
        topBar.setAlpha(0f);
        topBar.setTranslationY(-30f);
        topBar.animate().alpha(1f).translationY(0f).setDuration(350).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    private void anim(View v) {
        v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
    }

    private void loadAd() {
        if (bannerAd == null) return;
        if (adLoader != null) adLoader.setVisibility(View.VISIBLE);
        bannerAd.setVisibility(View.GONE);
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}