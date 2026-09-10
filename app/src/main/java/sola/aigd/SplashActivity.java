package sola.aigd;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import sola.aigd.R;

public class SplashActivity extends AppCompatActivity {

    private View blobBg, blobGlow, lineView, contentLayout;
    private CardView logoCard;
    private ImageView logoImage;
    private TextView appName, tagline, awanIgdText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findViews();
        setInitialState();
        playEnterAnimation();
        goToHome();
    }

    private void findViews() {
        blobBg = findViewById(R.id.blobBg);
        blobGlow = findViewById(R.id.blobGlow);
        logoCard = findViewById(R.id.logoCard);
        logoImage = findViewById(R.id.logoImage);
        appName = findViewById(R.id.appName);
        tagline = findViewById(R.id.tagline);
        awanIgdText = findViewById(R.id.awanIgdText);
        progressBar = findViewById(R.id.progressBar);
        lineView = findViewById(R.id.lineView);
        contentLayout = findViewById(R.id.contentLayout);
    }

    private void setInitialState() {
        blobBg.setScaleX(0.6f); blobBg.setScaleY(0.6f); blobBg.setAlpha(0f);
        blobGlow.setScaleX(0.6f); blobGlow.setScaleY(0.6f); blobGlow.setAlpha(0f);

        logoCard.setScaleX(0f); logoCard.setScaleY(0f); logoCard.setAlpha(0f);
        logoCard.setRotation(-15f);

        appName.setAlpha(0f); appName.setTranslationX(-30f);
        lineView.setScaleX(0f);
        tagline.setAlpha(0f); tagline.setTranslationX(-20f);
        progressBar.setAlpha(0f);
        awanIgdText.setAlpha(0f); awanIgdText.setTranslationY(15f);
    }

    private void playEnterAnimation() {
        // Blob
        blobBg.animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(900).setInterpolator(new DecelerateInterpolator())
                .setStartDelay(100).start();

        blobGlow.animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(1100).setInterpolator(new DecelerateInterpolator())
                .setStartDelay(200).start();

        // Logo with bounce
        logoCard.animate().scaleX(1f).scaleY(1f).alpha(1f).rotation(0f)
                .setDuration(700).setInterpolator(new OvershootInterpolator(2f))
                .setStartDelay(400).start();

        appName.animate().alpha(1f).translationX(0f)
                .setDuration(600).setStartDelay(700).start();

        lineView.animate().scaleX(1f)
                .setDuration(500).setStartDelay(850).start();

        tagline.animate().alpha(1f).translationX(0f)
                .setDuration(600).setStartDelay(950).start();

        progressBar.animate().alpha(1f)
                .setDuration(400).setStartDelay(1150).start();

        awanIgdText.animate().alpha(1f).translationY(0f)
                .setDuration(600).setStartDelay(1250).start();
    }

    private void goToHome() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            contentLayout.animate().alpha(0f).translationY(-15f)
                    .setDuration(350)
                    .withEndAction(() -> {
                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }).start();
            awanIgdText.animate().alpha(0f).setDuration(250).start();
            blobBg.animate().scaleX(1.15f).scaleY(1.15f).alpha(0f).setDuration(400).start();
        }, 3000);
    }
}