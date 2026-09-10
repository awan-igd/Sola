package sola.aigd;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import sola.aigd.R;

public class AboutActivity extends AppCompatActivity {

    private CardView backButton, companyWebsiteButton, ceoWebsiteButton;
    private TextView versionText, versionText2, adLabel;
    private Banner bannerAd;
    private ProgressBar adLoader;
    private View topBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupWindow();
        initViews();
        setupInsets();
        setupClicks();
        loadAd();
        updateVersion();
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
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), top + 12, v.getPaddingRight(), 12);
            return insets;
        });
    }

    private void initViews() {
        topBar = findViewById(R.id.topBar);
        backButton = findViewById(R.id.backButton);
        companyWebsiteButton = findViewById(R.id.companyWebsiteButton);
        ceoWebsiteButton = findViewById(R.id.ceoWebsiteButton);
        versionText = findViewById(R.id.versionText);
        versionText2 = findViewById(R.id.versionText2);
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
        companyWebsiteButton.setOnClickListener(v -> {
            anim(v);
            openUrl("https://awan-igd.web.app/");
        });
        ceoWebsiteButton.setOnClickListener(v -> {
            anim(v);
            openUrl("https://bilal-rasheed-official.web.app/");
        });
    }

    private void openUrl(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            try {
                Intent i = new Intent(this, SearchActivity.class);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception ex) {
            }
        }
    }

    private void updateVersion() {
        String v = "1.0.0";
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        if (versionText != null) versionText.setText("v" + v);
        if (versionText2 != null) versionText2.setText("Version " + v);
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