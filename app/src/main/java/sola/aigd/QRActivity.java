package sola.aigd;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sola.aigd.R;

public class QRActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION = 1001;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ExecutorService cameraExecutor;

    private CardView backButton, flashButton, openButton, copyButton, scanAgainButton;
    private ImageView flashIcon;
    private PreviewView previewView;
    private View scannerFrame, scanningLine;
    private TextView statusText, resultText, resultData, adLabel;
    private androidx.cardview.widget.CardView resultContainer, scannerContainer;
    private Banner bannerAd;
    private ProgressBar adLoader;
    private LinearLayout bottomAdContainer;
    private View topBar, divider;

    private Camera camera;
    private BarcodeScanner scanner;
    private boolean isScanning = false, isFlashOn = false, isResultShown = false;
    private String scannedData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        setupWindow();
        initViews();
        setupInsets();
        setupScanner();
        setupClicks();
        loadAd();
        startLineAnim();
        checkPermission();
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
        topBar = findViewById(R.id.topBar);
        if (topBar != null) ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, ins) -> {
            Insets s = ins.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), s.top + 12, v.getPaddingRight(), 12);
            return ins;
        });
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        flashButton = findViewById(R.id.flashButton);
        flashIcon = findViewById(R.id.flashIcon);
        previewView = findViewById(R.id.previewView);
        scannerFrame = findViewById(R.id.scannerFrame);
        scanningLine = findViewById(R.id.scanningLine);
        statusText = findViewById(R.id.statusText);
        resultContainer = findViewById(R.id.resultContainer);
        resultText = findViewById(R.id.resultText);
        resultData = findViewById(R.id.resultData);
        openButton = findViewById(R.id.openButton);
        copyButton = findViewById(R.id.copyButton);
        scanAgainButton = findViewById(R.id.scanAgainButton);
        bannerAd = findViewById(R.id.bannerAd);
        adLoader = findViewById(R.id.adLoader);
        adLabel = findViewById(R.id.adLabel);
        scannerContainer = findViewById(R.id.scannerContainer);
        bottomAdContainer = findViewById(R.id.bottomAdContainer);
        cameraExecutor = Executors.newSingleThreadExecutor();
        BarcodeScannerOptions opts = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_ALL_FORMATS).build();
        scanner = BarcodeScanning.getClient(opts);
    }

    private void setupScanner() {
        if (scannerContainer != null) {
            scannerContainer.setAlpha(0f);
            scannerContainer.setScaleX(0.9f);
            scannerContainer.setScaleY(0.9f);
            scannerContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            startCamera();
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] p, @NonNull int[] g) {
        super.onRequestPermissionsResult(req, p, g);
        if (req == CAMERA_PERMISSION) {
            if (g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) startCamera();
            else {
                Toast.makeText(this, "Camera permission needed", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                ImageAnalysis analysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(cameraExecutor, proxy -> {
                    if (isScanning || isResultShown) {
                        proxy.close();
                        return;
                    }
                    if (proxy.getImage() == null) {
                        proxy.close();
                        return;
                    }
                    InputImage img = InputImage.fromMediaImage(proxy.getImage(), proxy.getImageInfo().getRotationDegrees());
                    isScanning = true;
                    scanner.process(img).addOnSuccessListener(b -> {
                        if (b != null && !b.isEmpty()) {
                            for (Barcode bc : b) {
                                String v = bc.getRawValue();
                                if (v != null && !v.isEmpty()) {
                                    scannedData = v;
                                    runOnUiThread(() -> showResult(v));
                                    break;
                                }
                            }
                        }
                        isScanning = false;
                    }).addOnFailureListener(e -> isScanning = false).addOnCompleteListener(t -> proxy.close());
                });
                CameraSelector sel = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                provider.unbindAll();
                camera = provider.bindToLifecycle(this, sel, preview, analysis);
                updateFlashUI();
                runOnUiThread(() -> {
                    if (statusText != null) statusText.setText("Position QR code in frame");
                });
            } catch (Exception e) {
                if (statusText != null) statusText.setText("Camera error");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void showResult(String data) {
        if (isResultShown) return;
        isResultShown = true;
        if (scanningLine != null) scanningLine.animate().cancel();
        if (scanningLine != null) scanningLine.setVisibility(View.GONE);
        if (statusText != null) statusText.setVisibility(View.GONE);
        if (resultData != null) resultData.setText(data);
        boolean isUrl = data.startsWith("http://") || data.startsWith("https://") || data.startsWith("www.");
        if (openButton != null) openButton.setVisibility(isUrl ? View.VISIBLE : View.GONE);
        if (resultContainer != null) {
            resultContainer.setVisibility(View.VISIBLE);
            resultContainer.setAlpha(0f);
            resultContainer.setTranslationY(40f);
            resultContainer.animate().alpha(1f).translationY(0f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        }
    }

    private void reset() {
        isResultShown = false;
        isScanning = false;
        scannedData = "";
        if (resultContainer != null) resultContainer.setVisibility(View.GONE);
        if (scanningLine != null) {
            scanningLine.setVisibility(View.VISIBLE);
            startLineAnim();
        }
        if (statusText != null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText("Position QR code in frame");
        }
    }

    private void toggleFlash() {
        if (camera == null) return;
        isFlashOn = !isFlashOn;
        camera.getCameraControl().enableTorch(isFlashOn);
        updateFlashUI();
    }

    private void updateFlashUI() {
        if (flashIcon != null) {
            flashIcon.setImageResource(isFlashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash);
            flashIcon.setColorFilter(isFlashOn ? Color.parseColor("#FFC107") : ContextCompat.getColor(this, R.color.f));
        }
    }

    private void startLineAnim() {
        if (scanningLine == null || isResultShown) return;
        scanningLine.setTranslationY(-90f);
        scanningLine.animate().translationY(90f).setDuration(1800).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(() -> {
            if (!isResultShown) startLineAnim();
        }).start();
    }

    private void setupClicks() {
        if (backButton != null) backButton.setOnClickListener(v -> {
            anim(v);
            finish();
        });
        if (flashButton != null) flashButton.setOnClickListener(v -> {
            anim(v);
            toggleFlash();
        });
        if (openButton != null) openButton.setOnClickListener(v -> {
            anim(v);
            if (!scannedData.isEmpty()) {
                String url = scannedData.startsWith("www.") ? "https://" + scannedData : scannedData;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    Toast.makeText(this, "Can't open", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (copyButton != null) copyButton.setOnClickListener(v -> {
            anim(v);
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("QR", scannedData));
            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
        });
        if (scanAgainButton != null) scanAgainButton.setOnClickListener(v -> {
            anim(v);
            reset();
        });
    }

    private void loadAd() {
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

    private void anim(View v) {
        if (v == null) return;
        v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isResultShown) startLineAnim();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        if (scanningLine != null) scanningLine.animate().cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (scanner != null) scanner.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}