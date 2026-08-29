package sola.aigd;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SOLA Browser - QR Code Scanner Activity
 * Uses CameraX + ML Kit for QR scanning
 * Created by Awan IGD
 */
public class QRActivity extends AppCompatActivity {

    // ========== CONSTANTS ==========
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final long SCAN_INTERVAL_MS = 500;
    // ========== HANDLER ==========
    private final Handler handler = new Handler(Looper.getMainLooper());
    // ========== VIEWS ==========
    private LinearLayout topBar;
    private LinearLayout backButton;
    private LinearLayout flashButton;
    private ImageView flashIcon;
    private PreviewView previewView;
    private View scannerFrame;
    private View scanningLine;
    private TextView statusText;
    private LinearLayout resultContainer;
    private TextView resultText;
    private TextView resultData;
    private TextView openButton;
    private TextView copyButton;
    private TextView scanAgainButton;
    private Banner bannerAd;
    private ProgressBar adLoader;
    private TextView adLabel;
    private AnimatedBackground animatedBackground;
    // ========== CAMERA ==========
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Camera camera;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private boolean isScanning = false;
    private boolean isFlashOn = false;
    private boolean isResultShown = false;
    private String scannedData = "";
    private Runnable scanLineRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        setupFullScreenWindow();
        initViews();
        setupAnimatedBackground();
        setupClickListeners();
        loadBannerAd();
        startScanLineAnimation();

        // Check camera permission
        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    // =============================================================
    // FULL SCREEN SETUP
    // =============================================================

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

    // =============================================================
    // INIT VIEWS
    // =============================================================

    private void initViews() {
        topBar = findViewById(R.id.topBar);
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
        animatedBackground = findViewById(R.id.animatedBackground);

        // Camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // ML Kit Barcode Scanner
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E
                )
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    // =============================================================
    // BACKGROUND
    // =============================================================

    private void setupAnimatedBackground() {
        if (animatedBackground != null) {
            animatedBackground.setWaveSpeed(5000);
            animatedBackground.resumeAnimation();
        }
    }

    // =============================================================
    // BANNER AD
    // =============================================================

    private void loadBannerAd() {
        if (bannerAd == null) return;

        adLoader.setVisibility(View.VISIBLE);
        bannerAd.setVisibility(View.GONE);

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
            public void onImpression(View banner) {
            }

            @Override
            public void onClick(View banner) {
            }
        });

        bannerAd.loadAd();
    }

    // =============================================================
    // CLICK LISTENERS
    // =============================================================

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> {
            animateButtonClick(backButton);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Flash button
        flashButton.setOnClickListener(v -> {
            animateButtonClick(flashButton);
            toggleFlash();
        });

        // Open URL
        openButton.setOnClickListener(v -> {
            animateButtonClick(openButton);
            if (!scannedData.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedData));
                startActivity(intent);
            }
        });

        // Copy
        copyButton.setOnClickListener(v -> {
            animateButtonClick(copyButton);
            copyToClipboard(scannedData);
        });

        // Scan Again
        scanAgainButton.setOnClickListener(v -> {
            animateButtonClick(scanAgainButton);
            resetScanner();
        });
    }

    // =============================================================
    // CAMERA PERMISSION
    // =============================================================

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                statusText.setText("Camera permission denied");
                Toast.makeText(this, "Camera permission required for QR scanning", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // =============================================================
    // CAMERA START
    // =============================================================

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                statusText.setText("Camera error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image Analysis for QR Scanning
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (isScanning) return;

            @androidx.camera.core.ExperimentalGetImage
            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

            if (image != null) {
                isScanning = true;
                barcodeScanner.process(image)
                        .addOnSuccessListener(barcodes -> {
                            if (barcodes != null && !barcodes.isEmpty()) {
                                processBarcode(barcodes);
                            }
                            isScanning = false;
                        })
                        .addOnFailureListener(e -> {
                            isScanning = false;
                        })
                        .addOnCompleteListener(task -> {
                            imageProxy.close();
                        });
            } else {
                imageProxy.close();
                isScanning = false;
            }
        });

        // Camera Selector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Bind
        camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
        );

        // Update flash status
        updateFlashUI();

        // Hide status text after camera starts
        handler.postDelayed(() -> {
            if (statusText != null) {
                statusText.setText("Position QR code in the frame");
            }
        }, 1000);
    }

    // =============================================================
    // QR PROCESSING
    // =============================================================

    private void processBarcode(List<Barcode> barcodes) {
        if (isResultShown) return;

        for (Barcode barcode : barcodes) {
            String value = barcode.getRawValue();
            if (value != null && !value.isEmpty()) {
                scannedData = value;
                showResult(value);
                break;
            }
        }
    }

    private void showResult(String data) {
        isResultShown = true;
        isScanning = false;

        // Hide scanner
        scannerFrame.setVisibility(View.GONE);
        scanningLine.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);

        // Show result
        resultContainer.setVisibility(View.VISIBLE);
        resultData.setText(data);

        // Check if it's a URL
        if (data.startsWith("http://") || data.startsWith("https://")) {
            resultText.setText("QR Code Scanned!");
            openButton.setVisibility(View.VISIBLE);
            openButton.setText("Open URL");
        } else {
            resultText.setText("QR Code Scanned!");
            openButton.setVisibility(View.GONE);
        }

        // Animate result
        resultContainer.setAlpha(0f);
        resultContainer.setScaleX(0.8f);
        resultContainer.setScaleY(0.8f);
        resultContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Vibrate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Optional: Add vibration
        }
    }

    private void resetScanner() {
        isResultShown = false;
        isScanning = false;
        scannedData = "";

        resultContainer.setVisibility(View.GONE);
        resultContainer.setAlpha(0f);

        scannerFrame.setVisibility(View.VISIBLE);
        scanningLine.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText("Position QR code in the frame");

        // Restart scan line animation
        startScanLineAnimation();
    }

    // =============================================================
    // FLASH
    // =============================================================

    private void toggleFlash() {
        if (camera == null) return;

        isFlashOn = !isFlashOn;
        camera.getCameraControl().enableTorch(isFlashOn);
        updateFlashUI();
    }

    private void updateFlashUI() {
        if (flashIcon != null) {
            if (isFlashOn) {
                flashIcon.setImageResource(R.drawable.ic_flash_on);
                flashIcon.setColorFilter(Color.parseColor("#FFD700"));
            } else {
                flashIcon.setImageResource(R.drawable.ic_flash);
                flashIcon.setColorFilter(Color.WHITE);
            }
        }
        flashButton.setVisibility(View.VISIBLE);
    }

    // =============================================================
    // SCAN LINE ANIMATION
    // =============================================================

    private void startScanLineAnimation() {
        if (scanningLine == null) return;

        scanningLine.setAlpha(1f);
        scanningLine.setTranslationY(-100f);

        scanningLine.animate()
                .translationY(100f)
                .setDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    if (!isResultShown) {
                        startScanLineAnimation();
                    }
                })
                .start();
    }

    // =============================================================
    // COPY TO CLIPBOARD
    // =============================================================

    private void copyToClipboard(String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("QR Code", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
        }
    }

    // =============================================================
    // ANIMATIONS
    // =============================================================

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

    // =============================================================
    // LIFECYCLE
    // =============================================================

    @Override
    protected void onResume() {
        super.onResume();
        if (animatedBackground != null) {
            animatedBackground.resumeAnimation();
        }
        if (!isResultShown) {
            startScanLineAnimation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animatedBackground != null) {
            animatedBackground.pauseAnimation();
        }
        handler.removeCallbacks(scanLineRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

        if (barcodeScanner != null) {
            barcodeScanner.close();
        }

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }

        if (animatedBackground != null) {
            animatedBackground.stopBackgroundAnimation();
        }

        if (bannerAd != null) {
        }
    }
}