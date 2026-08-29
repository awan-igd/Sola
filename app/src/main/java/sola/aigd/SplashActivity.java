package sola.aigd;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 3500;
    private static final long FADE_DURATION = 400;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isNavigating = false;

    private AnimatedBackground animatedBackground;
    private ImageView logoIcon;
    private TextView appName, tagline1, tagline2, tagline3, footerText;
    private LinearLayout progressContainer;
    private ProgressBar progressBar;
    private TextView[] taglines;

    private ValueAnimator progressAnimator;
    private AnimatorSet logoEntryAnim, textRevealAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setupFullScreen();
        initializeViews();
        setupBackground();
        startSplashAnimations();
        startProgressAnimation();
        handler.postDelayed(this::navigateToHome, SPLASH_DURATION);
    }

    private void setupFullScreen() {
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

    private void initializeViews() {
        animatedBackground = findViewById(R.id.animatedBackground);
        logoIcon = findViewById(R.id.logoIcon);
        appName = findViewById(R.id.appName);
        tagline1 = findViewById(R.id.tagline1);
        tagline2 = findViewById(R.id.tagline2);
        tagline3 = findViewById(R.id.tagline3);
        footerText = findViewById(R.id.footerText);
        progressContainer = findViewById(R.id.progressContainer);
        progressBar = findViewById(R.id.progressBar);

        taglines = new TextView[]{tagline1, tagline2, tagline3};

        setViewVisibility(progressContainer, View.INVISIBLE);
        setViewVisibility(footerText, View.INVISIBLE);
        setViewVisibility(appName, View.INVISIBLE);
        for (TextView tagline : taglines) {
            setViewVisibility(tagline, View.INVISIBLE);
        }

        ViewCompat.setLayerType(logoIcon, View.LAYER_TYPE_HARDWARE, null);
    }

    private void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private void setupBackground() {
        if (animatedBackground != null) {
            animatedBackground.setWaveSpeed(5000);
            animatedBackground.resumeAnimation();
        }
    }

    private void startSplashAnimations() {
        startLogoEntry();
        startTextReveal();
        startTaglineSequence();
        startFooterFade();
        startProgressEntry();
    }

    private void startLogoEntry() {
        logoIcon.setVisibility(View.VISIBLE);
        logoIcon.setAlpha(0f);
        logoIcon.setScaleX(0.3f);
        logoIcon.setScaleY(0.3f);
        logoIcon.setRotationY(180f);

        logoEntryAnim = new AnimatorSet();
        logoEntryAnim.playTogether(
                ObjectAnimator.ofFloat(logoIcon, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(logoIcon, "scaleX", 0.3f, 1f),
                ObjectAnimator.ofFloat(logoIcon, "scaleY", 0.3f, 1f),
                ObjectAnimator.ofFloat(logoIcon, "rotationY", 180f, 0f)
        );
        logoEntryAnim.setDuration(800);
        logoEntryAnim.setInterpolator(new OvershootInterpolator(1.5f));
        logoEntryAnim.start();
    }

    private void startTextReveal() {
        appName.setVisibility(View.VISIBLE);
        appName.setAlpha(0f);
        appName.setTranslationY(50f);
        appName.setScaleX(0.8f);
        appName.setScaleY(0.8f);

        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f);
        ObjectAnimator nameY = ObjectAnimator.ofFloat(appName, "translationY", 50f, 0f);
        ObjectAnimator nameScaleX = ObjectAnimator.ofFloat(appName, "scaleX", 0.8f, 1f);
        ObjectAnimator nameScaleY = ObjectAnimator.ofFloat(appName, "scaleY", 0.8f, 1f);

        textRevealAnim = new AnimatorSet();
        textRevealAnim.playTogether(nameAlpha, nameY, nameScaleX, nameScaleY);
        textRevealAnim.setDuration(700);
        textRevealAnim.setInterpolator(new OvershootInterpolator(1.2f));
        textRevealAnim.setStartDelay(400);
        textRevealAnim.start();
    }

    private void startTaglineSequence() {
        final long[] delays = {1000, 1900, 2700};

        for (int i = 0; i < taglines.length && i < delays.length; i++) {
            final int index = i;
            handler.postDelayed(() -> showTagline(index), delays[i]);
        }
    }

    private void showTagline(int index) {
        if (index >= taglines.length || taglines[index] == null) return;

        TextView current = taglines[index];
        current.setVisibility(View.VISIBLE);
        current.setAlpha(0f);
        current.setTranslationY(20f);

        current.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        if (index > 0 && taglines[index - 1] != null) {
            TextView previous = taglines[index - 1];
            previous.animate()
                    .alpha(0f)
                    .translationY(-15f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();
        }
    }

    private void startFooterFade() {
        footerText.setVisibility(View.VISIBLE);
        footerText.setAlpha(0f);
        footerText.animate()
                .alpha(0.6f)
                .setDuration(600)
                .setStartDelay(1200)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void startProgressEntry() {
        progressContainer.setVisibility(View.VISIBLE);
        progressContainer.setAlpha(0f);
        progressContainer.setTranslationY(20f);
        progressContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(1100)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void startProgressAnimation() {
        progressBar.setProgress(0);
        progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(SPLASH_DURATION);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.addUpdateListener(anim -> {
            int progress = (int) anim.getAnimatedValue();
            progressBar.setProgress(progress);
        });
        progressAnimator.start();
    }

    private void navigateToHome() {
        if (isNavigating) return;
        isNavigating = true;

        cancelAllAnimations();
        pauseBackground();

        View root = findViewById(android.R.id.content);
        if (root != null) {
            root.animate()
                    .alpha(0f)
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .setDuration(350)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    })
                    .start();
        } else {
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void cancelAllAnimations() {
        handler.removeCallbacksAndMessages(null);

        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }

        if (logoEntryAnim != null) {
            logoEntryAnim.cancel();
            logoEntryAnim = null;
        }

        if (textRevealAnim != null) {
            textRevealAnim.cancel();
            textRevealAnim = null;
        }

        if (logoIcon != null) logoIcon.animate().cancel();
        if (appName != null) appName.animate().cancel();
        if (footerText != null) footerText.animate().cancel();
        if (progressContainer != null) progressContainer.animate().cancel();

        for (TextView tagline : taglines) {
            if (tagline != null) tagline.animate().cancel();
        }
    }

    private void pauseBackground() {
        if (animatedBackground != null) {
            animatedBackground.pauseAnimation();
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
    protected void onResume() {
        super.onResume();
        if (animatedBackground != null && !isNavigating) {
            animatedBackground.resumeAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        cancelAllAnimations();
        if (animatedBackground != null) {
            animatedBackground.stopBackgroundAnimation();
            animatedBackground = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Disable back press during splash
    }
}