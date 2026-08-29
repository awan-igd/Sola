package sola.aigd;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import java.io.File;

public class PrivacyActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private LinearLayout backButton;
    private LinearLayout clearHistoryOption, clearCookiesOption;
    private LinearLayout clearCacheOption, clearAllOption;
    private AnimatedBackground animatedBackground;

    private LinearLayout bottomAdContainer;
    private Banner bannerAd;
    private ProgressBar adLoader;
    private TextView adLabel;

    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        setupFullScreenWindow();
        initializeCore();
        initViews();
        setupAnimatedBackground();
        setupWindowInsets();
        setupClickListeners();
        loadBannerAd();
    }

    private void initializeCore() {
        dataManager = DataManager.getInstance(this);
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        clearHistoryOption = findViewById(R.id.clearHistoryOption);
        clearCookiesOption = findViewById(R.id.clearCookiesOption);
        clearCacheOption = findViewById(R.id.clearCacheOption);
        clearAllOption = findViewById(R.id.clearAllOption);
        animatedBackground = findViewById(R.id.animatedBackground);
        bottomAdContainer = findViewById(R.id.bottomAdContainer);
        bannerAd = findViewById(R.id.bannerAd);
        adLoader = findViewById(R.id.adLoader);
        adLabel = findViewById(R.id.adLabel);
    }

    private void setupFullScreenWindow() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private void setupAnimatedBackground() {
        if (animatedBackground != null) {
            animatedBackground.setWaveSpeed(5000);
            animatedBackground.resumeAnimation();
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

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            animateButtonClick(backButton);
            finishWithAnimation();
        });

        clearHistoryOption.setOnClickListener(v -> {
            animateButtonClick(clearHistoryOption);
            showClearDialog("Clear History",
                    "Are you sure you want to clear all browsing history?",
                    () -> {
                        dataManager.clearHistory();
                        showToast("History cleared");
                    });
        });

        clearCookiesOption.setOnClickListener(v -> {
            animateButtonClick(clearCookiesOption);
            showClearDialog("Clear Cookies",
                    "Are you sure you want to clear all cookies?",
                    () -> {
                        android.webkit.CookieManager.getInstance().removeAllCookies(null);
                        showToast("Cookies cleared");
                    });
        });

        clearCacheOption.setOnClickListener(v -> {
            animateButtonClick(clearCacheOption);
            showClearDialog("Clear Cache",
                    "Are you sure you want to clear all cache?",
                    () -> {
                        clearWebViewCache();
                        showToast("Cache cleared");
                    });
        });

        clearAllOption.setOnClickListener(v -> {
            animateButtonClick(clearAllOption);
            showClearDialog("Clear All Data",
                    "Are you sure you want to clear all browsing data?\n\nThis will clear:\n• History\n• Cookies\n• Cache\n• Tabs\n• Downloads\n• Bookmarks",
                    () -> {
                        dataManager.clearAllData();
                        android.webkit.CookieManager.getInstance().removeAllCookies(null);
                        clearWebViewCache();
                        showToast("All data cleared");
                    });
        });
    }

    private void showClearDialog(String title, String message, Runnable onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_clear_tabs, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView titleView = dialogView.findViewById(R.id.dialogTitle);
        TextView messageView = dialogView.findViewById(R.id.dialogMessage);
        TextView positiveBtn = dialogView.findViewById(R.id.positiveBtn);
        TextView negativeBtn = dialogView.findViewById(R.id.negativeBtn);

        if (titleView != null) titleView.setText(title);
        if (messageView != null) messageView.setText(message);

        positiveBtn.setOnClickListener(v -> {
            animateButtonClick(positiveBtn);
            if (onConfirm != null) {
                onConfirm.run();
            }
            dialog.dismiss();
        });

        negativeBtn.setOnClickListener(v -> {
            animateButtonClick(negativeBtn);
            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        dialog.show();
    }

    private void clearWebViewCache() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                android.webkit.CookieManager.getInstance().flush();
            }
            clearCacheDir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearCacheDir() {
        try {
            File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }

    private void animateButtonClick(View button) {
        button.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    private void finishWithAnimation() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animatedBackground != null) {
            animatedBackground.resumeAnimation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animatedBackground != null) {
            animatedBackground.pauseAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (animatedBackground != null) {
            animatedBackground.stopBackgroundAnimation();
        }
    }

    @Override
    public void onBackPressed() {
        finishWithAnimation();
    }
}