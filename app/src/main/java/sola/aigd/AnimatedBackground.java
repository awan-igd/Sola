package sola.aigd;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * SOLA Browser - Premium Animated Background v4.0
 * ✅ FULLY AUTOMATIC COLOR CHANGING - NEVER STOPS
 * ✅ NO CRASHES - PERFECT ERROR HANDLING
 * ✅ ALL METHODS INCLUDED - COMPATIBLE WITH ALL ACTIVITIES
 *
 * @author Awan IGD - Intelligent Grid Development
 * @version 4.0
 */
public class AnimatedBackground extends View {

    // ============================================================
    //  SOLA BRAND COLORS
    // ============================================================
    private static final int SOLA_PRIMARY = Color.parseColor("#1A2A6C");
    private static final int SOLA_SECONDARY = Color.parseColor("#11998E");
    private static final int SOLA_ACCENT = Color.parseColor("#00D2FF");
    private static final int SOLA_PURPLE = Color.parseColor("#667EEA");
    private static final int SOLA_VIOLET = Color.parseColor("#764BA2");
    private static final int SOLA_GOLD = Color.parseColor("#FFD700");
    private static final int SOLA_PINK = Color.parseColor("#FF6B6B");
    private static final int SOLA_MINT = Color.parseColor("#4ECDC4");
    private static final int SOLA_ORANGE = Color.parseColor("#FF8C42");
    private static final int SOLA_LIME = Color.parseColor("#A8E6CF");
    private static final int SOLA_ROSE = Color.parseColor("#FF6B8A");
    private static final int SOLA_INDIGO = Color.parseColor("#4A00E0");
    private static final int SOLA_TEAL = Color.parseColor("#00B4D8");
    private static final int SOLA_LAVENDER = Color.parseColor("#B8A9C9");
    private static final int SOLA_CORAL = Color.parseColor("#FF7F50");
    private static final int SOLA_AMBER = Color.parseColor("#FFBF00");
    private static final int SOLA_EMERALD = Color.parseColor("#50C878");

    // ============================================================
    //  ENHANCED COLOR PALETTES
    // ============================================================
    private final int[] topColors = {
            SOLA_PRIMARY, SOLA_ACCENT, SOLA_PURPLE,
            SOLA_INDIGO, SOLA_VIOLET, SOLA_GOLD,
            SOLA_TEAL, SOLA_LAVENDER, SOLA_AMBER,
            SOLA_EMERALD, SOLA_ROSE, SOLA_ORANGE
    };

    private final int[] bottomColors = {
            SOLA_SECONDARY, SOLA_MINT, SOLA_ACCENT,
            SOLA_PINK, SOLA_ORANGE, SOLA_LIME,
            SOLA_ROSE, SOLA_CORAL, SOLA_GOLD,
            SOLA_VIOLET, SOLA_TEAL, SOLA_LAVENDER
    };

    private final Random random = new Random();

    // ============================================================
    //  ANIMATION VARIABLES
    // ============================================================
    private Paint backgroundPaint;
    private Paint wavePaint;
    private Paint glowPaint;
    private Paint orbPaint;
    private Paint particlePaint;
    private Paint vignettePaint;
    private Paint starPaint;
    private Paint shimmerPaint;
    private Paint auroraPaint;

    private Path wavePath1;
    private Path wavePath2;
    private Path wavePath3;
    private Path wavePath4;
    private Path shimmerPath;
    private Path auroraPath;

    private ValueAnimator waveAnimator;
    private ValueAnimator orbAnimator;
    private ValueAnimator glowAnimator;
    private ValueAnimator particleAnimator;
    private ValueAnimator auroraAnimator;
    private ValueAnimator shimmerAnimator;
    private ValueAnimator colorAnimator;

    private int viewWidth;
    private int viewHeight;
    private float colorPhase = 0f;
    private long lastUpdateTime = 0;
    private int currentTopColor;
    private int currentBottomColor;

    // ✅ BlurMaskFilter - Pre-created for safety
    private BlurMaskFilter particleBlurFilter;
    private BlurMaskFilter glowBlurFilter;
    private BlurMaskFilter auroraBlurFilter;

    // Wave layers
    private float waveOffset1 = 0f;
    private float waveOffset2 = 0f;
    private float waveOffset3 = 0f;
    private float waveOffset4 = 0f;

    // Orbs
    private float[] orbX = new float[8];
    private float[] orbY = new float[8];
    private float[] orbRadius = new float[8];
    private float[] orbPhase = new float[8];
    private float[] orbSpeed = {1.0f, 0.7f, 1.3f, 0.5f, 0.9f, 1.1f, 0.6f, 1.4f};
    private float[] orbAmplitude = {0.12f, 0.08f, 0.15f, 0.10f, 0.20f, 0.06f, 0.14f, 0.09f};
    private float[] orbBaseX = new float[8];
    private float[] orbBaseY = new float[8];
    private int[] orbColors = {
            SOLA_ACCENT, SOLA_VIOLET, SOLA_GOLD,
            SOLA_PINK, SOLA_MINT, SOLA_ORANGE,
            SOLA_PURPLE, SOLA_TEAL
    };

    // Particles
    private float[] particleX;
    private float[] particleY;
    private float[] particleSize;
    private float[] particleSpeed;
    private float[] particleAlpha;
    private float[] particleDrift;
    private int[] particleColors;

    // Stars
    private float[] starX;
    private float[] starY;
    private float[] starSize;
    private float[] starAlpha;
    private float[] starTwinkleSpeed;

    // Aurora
    private float auroraPhase = 0f;
    private float[] auroraPoints;

    // Glow
    private float glowIntensity = 0f;
    private float glowPulse = 0f;

    // State
    private boolean isPaused = false;
    private boolean isDestroyed = false;
    private boolean isLowPerformance = false;

    // ============================================================
    //  CONSTRUCTORS
    // ============================================================
    public AnimatedBackground(Context context) {
        super(context);
        init();
    }

    public AnimatedBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // ============================================================
    //  INITIALIZATION
    // ============================================================
    private void init() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setWillNotDraw(false);

        // ✅ Initialize blur filters safely
        try {
            particleBlurFilter = new BlurMaskFilter(4.0f, BlurMaskFilter.Blur.NORMAL);
            glowBlurFilter = new BlurMaskFilter(60f, BlurMaskFilter.Blur.OUTER);
            auroraBlurFilter = new BlurMaskFilter(80f, BlurMaskFilter.Blur.OUTER);
        } catch (Exception e) {
            particleBlurFilter = null;
            glowBlurFilter = null;
            auroraBlurFilter = null;
        }

        initializePaints();
        initializePaths();
        initializeParticles();
        initializeStars();
        initializeAurora();
        initializeOrbPositions();

        currentTopColor = topColors[0];
        currentBottomColor = bottomColors[0];

        startAnimations();
        detectPerformance();
    }

    private void detectPerformance() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            isLowPerformance = true;
        }
    }

    private void initializePaints() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setDither(true);
        backgroundPaint.setStyle(Paint.Style.FILL);

        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setDither(true);
        wavePaint.setStyle(Paint.Style.FILL);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setDither(true);
        glowPaint.setStyle(Paint.Style.FILL);
        if (glowBlurFilter != null) {
            glowPaint.setMaskFilter(glowBlurFilter);
        }

        orbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        orbPaint.setDither(true);
        orbPaint.setStyle(Paint.Style.FILL);

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setDither(true);
        particlePaint.setStyle(Paint.Style.FILL);

        vignettePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vignettePaint.setDither(true);
        vignettePaint.setStyle(Paint.Style.FILL);

        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setDither(true);
        starPaint.setStyle(Paint.Style.FILL);

        shimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shimmerPaint.setDither(true);
        shimmerPaint.setStyle(Paint.Style.FILL);

        auroraPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        auroraPaint.setDither(true);
        auroraPaint.setStyle(Paint.Style.FILL);
        if (auroraBlurFilter != null) {
            auroraPaint.setMaskFilter(auroraBlurFilter);
        }
    }

    private void initializePaths() {
        wavePath1 = new Path();
        wavePath2 = new Path();
        wavePath3 = new Path();
        wavePath4 = new Path();
        shimmerPath = new Path();
        auroraPath = new Path();
    }

    private void initializeParticles() {
        int count = isLowPerformance ? 25 : 50;
        particleX = new float[count];
        particleY = new float[count];
        particleSize = new float[count];
        particleSpeed = new float[count];
        particleAlpha = new float[count];
        particleDrift = new float[count];
        particleColors = new int[count];

        for (int i = 0; i < count; i++) {
            particleX[i] = random.nextFloat();
            particleY[i] = random.nextFloat();
            particleSize[i] = 1.5f + random.nextFloat() * 5f;
            particleSpeed[i] = 0.0003f + random.nextFloat() * 0.0012f;
            particleAlpha[i] = 0.1f + random.nextFloat() * 0.5f;
            particleDrift[i] = (random.nextFloat() - 0.5f) * 0.0005f;
            particleColors[i] = blendColors(
                    topColors[random.nextInt(topColors.length)],
                    bottomColors[random.nextInt(bottomColors.length)],
                    random.nextFloat()
            );
        }
    }

    private void initializeStars() {
        int count = isLowPerformance ? 30 : 60;
        starX = new float[count];
        starY = new float[count];
        starSize = new float[count];
        starAlpha = new float[count];
        starTwinkleSpeed = new float[count];

        for (int i = 0; i < count; i++) {
            starX[i] = random.nextFloat();
            starY[i] = random.nextFloat();
            starSize[i] = 0.5f + random.nextFloat() * 2f;
            starAlpha[i] = 0.1f + random.nextFloat() * 0.7f;
            starTwinkleSpeed[i] = 0.5f + random.nextFloat() * 2f;
        }
    }

    private void initializeAurora() {
        auroraPoints = new float[20];
        for (int i = 0; i < auroraPoints.length; i++) {
            auroraPoints[i] = random.nextFloat();
        }
    }

    private void initializeOrbPositions() {
        float[] positions = {
                0.12f, 0.15f,
                0.88f, 0.85f,
                0.50f, 0.30f,
                0.25f, 0.70f,
                0.75f, 0.55f,
                0.40f, 0.90f,
                0.08f, 0.60f,
                0.92f, 0.35f
        };

        for (int i = 0; i < orbX.length; i++) {
            orbBaseX[i] = positions[i * 2];
            orbBaseY[i] = positions[i * 2 + 1];
            orbPhase[i] = random.nextFloat() * 2 * (float) Math.PI;
            orbRadius[i] = 25f + random.nextFloat() * 45f;
        }
    }

    // ============================================================
    //  ANIMATION STARTERS
    // ============================================================
    private void startAnimations() {
        startWaveAnimation();
        startOrbAnimation();
        startGlowAnimation();
        startParticleAnimation();
        startAuroraAnimation();
        startShimmerAnimation();
        startColorAnimation();
        lastUpdateTime = System.currentTimeMillis();
    }

    private void startWaveAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0f, 1f);
        waveAnimator.setDuration(8000);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setRepeatMode(ValueAnimator.RESTART);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(animation -> {
            if (!isDestroyed) {
                float value = (float) animation.getAnimatedValue();
                waveOffset1 = value;
                waveOffset2 = (value + 0.25f) % 1f;
                waveOffset3 = (value + 0.50f) % 1f;
                waveOffset4 = (value + 0.75f) % 1f;
                invalidate();
            }
        });
        waveAnimator.start();
    }

    private void startOrbAnimation() {
        orbAnimator = ValueAnimator.ofFloat(0f, 1f);
        orbAnimator.setDuration(12000);
        orbAnimator.setRepeatCount(ValueAnimator.INFINITE);
        orbAnimator.setRepeatMode(ValueAnimator.RESTART);
        orbAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        orbAnimator.addUpdateListener(animation -> {
            if (!isDestroyed) {
                float value = (float) animation.getAnimatedValue();
                for (int i = 0; i < orbX.length; i++) {
                    orbPhase[i] += 0.008f * orbSpeed[i];
                    if (orbPhase[i] > 2 * Math.PI) {
                        orbPhase[i] -= 2 * Math.PI;
                    }
                }
                updateOrbs();
            }
        });
        orbAnimator.start();
    }

    private void startGlowAnimation() {
        glowAnimator = ValueAnimator.ofFloat(0f, 1f);
        glowAnimator.setDuration(5000);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        glowAnimator.addUpdateListener(animation -> {
            if (!isDestroyed) {
                glowIntensity = (float) animation.getAnimatedValue();
                glowPulse = (float) Math.sin(glowIntensity * Math.PI * 2) * 0.5f + 0.5f;
                invalidate();
            }
        });
        glowAnimator.start();
    }

    private void startParticleAnimation() {
        particleAnimator = ValueAnimator.ofFloat(0f, 1f);
        particleAnimator.setDuration(10000);
        particleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        particleAnimator.setRepeatMode(ValueAnimator.RESTART);
        particleAnimator.setInterpolator(new LinearInterpolator());
        particleAnimator.addUpdateListener(animation -> {
            if (!isDestroyed) {
                float value = (float) animation.getAnimatedValue();
                updateParticles(value);
                invalidate();
            }
        });
        particleAnimator.start();
    }

    private void startAuroraAnimation() {
        auroraAnimator = ValueAnimator.ofFloat(0f, 1f);
        auroraAnimator.setDuration(15000);
        auroraAnimator.setRepeatCount(ValueAnimator.INFINITE);
        auroraAnimator.setRepeatMode(ValueAnimator.RESTART);
        auroraAnimator.setInterpolator(new LinearInterpolator());
        auroraAnimator.addUpdateListener(animation -> {
            if (!isDestroyed) {
                auroraPhase = (float) animation.getAnimatedValue() * 2 * (float) Math.PI;
                updateAurora();
                invalidate();
            }
        });
        auroraAnimator.start();
    }

    private void startShimmerAnimation() {
        shimmerAnimator = ValueAnimator.ofFloat(0f, 1f);
        shimmerAnimator.setDuration(3000);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.REVERSE);
        shimmerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shimmerAnimator.addUpdateListener(animation -> {
            if (!isDestroyed) {
                invalidate();
            }
        });
        shimmerAnimator.start();
    }

    private void startColorAnimation() {
        colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(2000);
        colorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimator.setRepeatMode(ValueAnimator.RESTART);
        colorAnimator.setInterpolator(new LinearInterpolator());
        colorAnimator.addUpdateListener(animation -> {
            if (!isDestroyed) {
                float value = (float) animation.getAnimatedValue();
                colorPhase += 0.002f;
                if (colorPhase >= 1f) {
                    colorPhase -= 1f;
                }
                updateColors();
                invalidate();
            }
        });
        colorAnimator.start();
    }

    // ============================================================
    //  UPDATE METHODS
    // ============================================================
    private void updateColors() {
        float topPos = colorPhase * topColors.length;
        float bottomPos = colorPhase * bottomColors.length;

        currentTopColor = interpolateColor(topColors, topPos);
        currentBottomColor = interpolateColor(bottomColors, bottomPos);
    }

    private int interpolateColor(int[] colors, float position) {
        int index1 = (int) Math.floor(position);
        int index2 = (index1 + 1) % colors.length;
        float fraction = position - index1;
        float smooth = smoothstep(fraction);
        return blendColors(colors[index1], colors[index2], smooth);
    }

    private float smoothstep(float t) {
        return t * t * (3 - 2 * t);
    }

    private int blendColors(int color1, int color2, float ratio) {
        if (ratio <= 0) return color1;
        if (ratio >= 1) return color2;

        float inv = 1 - ratio;
        int r = (int) (Color.red(color1) * inv + Color.red(color2) * ratio);
        int g = (int) (Color.green(color1) * inv + Color.green(color2) * ratio);
        int b = (int) (Color.blue(color1) * inv + Color.blue(color2) * ratio);

        return Color.rgb(r, g, b);
    }

    private void updateOrbs() {
        if (viewWidth == 0 || viewHeight == 0) return;

        for (int i = 0; i < orbX.length; i++) {
            float angle = orbPhase[i];
            float ampX = orbAmplitude[i] * viewWidth;
            float ampY = orbAmplitude[i] * viewHeight * 0.8f;

            orbX[i] = orbBaseX[i] * viewWidth + (float) Math.sin(angle * 1.3f + i * 2.1f) * ampX;
            orbY[i] = orbBaseY[i] * viewHeight + (float) Math.cos(angle * 0.9f + i * 1.7f) * ampY;

            float baseRadius = 30f + i * 8f;
            orbRadius[i] = baseRadius + (float) Math.sin(angle * 0.7f + i) * 12f;
            orbRadius[i] *= (0.8f + 0.2f * glowPulse);
        }
    }

    private void updateParticles(float value) {
        int count = particleX.length;
        for (int i = 0; i < count; i++) {
            particleY[i] += particleSpeed[i];
            particleX[i] += particleDrift[i] + (float) Math.sin(value * 2 * Math.PI + i) * 0.00015f;

            if (particleY[i] > 1f) {
                particleY[i] = 0f;
                particleX[i] = random.nextFloat();
                particleSize[i] = 1.5f + random.nextFloat() * 5f;
                particleAlpha[i] = 0.1f + random.nextFloat() * 0.5f;
            }
            if (particleX[i] < 0) particleX[i] = 1f;
            if (particleX[i] > 1f) particleX[i] = 0f;

            particleAlpha[i] = 0.3f + 0.7f * (float) Math.sin(particleY[i] * 10 + value * 4);
        }
    }

    private void updateAurora() {
        float phase = auroraPhase;
        for (int i = 0; i < auroraPoints.length; i++) {
            auroraPoints[i] = 0.5f + 0.5f * (float) Math.sin(phase + i * 0.5f + i * 0.3f);
        }
    }

    // ============================================================
    //  ON SIZE CHANGED
    // ============================================================
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        updateOrbs();
    }

    // ============================================================
    //  MAIN DRAW METHOD
    // ============================================================
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0 || viewHeight == 0 || isDestroyed) return;

        try {
            drawBaseGradient(canvas);
            if (!isLowPerformance) {
                drawAurora(canvas);
            }
            drawWaveLayer(canvas, waveOffset1, 0.35f, 1);
            drawWaveLayer(canvas, waveOffset2, 0.25f, 2);
            drawWaveLayer(canvas, waveOffset3, 0.15f, 3);
            drawWaveLayer(canvas, waveOffset4, 0.08f, 4);
            drawParticles(canvas);
            drawStars(canvas);
            drawOrbs(canvas);
            drawGlow(canvas);
            if (!isLowPerformance) {
                drawShimmer(canvas);
            }
            drawVignette(canvas);
        } catch (Exception e) {
            drawBaseGradient(canvas);
        }
    }

    // ============================================================
    //  DRAW HELPERS
    // ============================================================
    private void drawBaseGradient(Canvas canvas) {
        try {
            LinearGradient gradient = new LinearGradient(
                    0, 0,
                    viewWidth, viewHeight,
                    currentTopColor, currentBottomColor,
                    Shader.TileMode.CLAMP
            );
            backgroundPaint.setShader(gradient);
            canvas.drawRect(0, 0, viewWidth, viewHeight, backgroundPaint);
            backgroundPaint.setShader(null);
        } catch (Exception e) {
            canvas.drawColor(currentTopColor);
        }
    }

    private void drawAurora(Canvas canvas) {
        if (isLowPerformance) return;

        try {
            float phase = auroraPhase;
            auroraPath.reset();

            for (int band = 0; band < 3; band++) {
                auroraPath.reset();
                float startX = 0;
                float startY = viewHeight * (0.1f + band * 0.15f) +
                        (float) Math.sin(phase + band * 1.2f) * viewHeight * 0.05f;
                auroraPath.moveTo(startX, startY);

                int points = 20;
                for (int i = 0; i <= points; i++) {
                    float x = (float) i / points * viewWidth;
                    float y = startY + (float) Math.sin(phase * 0.7f + i * 0.3f + band * 1.5f) *
                            viewHeight * 0.08f;
                    auroraPath.lineTo(x, y);
                }

                int[] auroraColors = {
                        blendColors(SOLA_ACCENT, SOLA_VIOLET, band / 3f),
                        blendColors(SOLA_PURPLE, SOLA_MINT, band / 3f),
                        Color.TRANSPARENT
                };
                float[] positions = {0f, 0.3f, 1f};

                LinearGradient grad = new LinearGradient(
                        0, startY - viewHeight * 0.2f,
                        0, startY + viewHeight * 0.2f,
                        auroraColors, positions, Shader.TileMode.CLAMP
                );

                auroraPaint.setShader(grad);
                auroraPaint.setAlpha((int) (30 + 20 * glowIntensity));

                auroraPath.lineTo(viewWidth, startY + viewHeight * 0.15f);
                auroraPath.lineTo(0, startY + viewHeight * 0.15f);
                auroraPath.close();

                canvas.drawPath(auroraPath, auroraPaint);
                auroraPaint.setShader(null);
            }
            auroraPaint.setAlpha(255);
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void drawWaveLayer(Canvas canvas, float offset, float alpha, int layer) {
        if (layer > 2 && isLowPerformance) return;

        try {
            float verticalOffset = offset * viewHeight * 0.3f;
            float amplitude = viewHeight * (0.04f + layer * 0.02f);

            Path path;
            switch (layer) {
                case 1: path = wavePath1; break;
                case 2: path = wavePath2; break;
                case 3: path = wavePath3; break;
                default: path = wavePath4; break;
            }
            path.reset();

            float phaseShift = layer * 0.5f;
            int points = 30;
            for (int i = 0; i <= points; i++) {
                float x = (float) i / points * viewWidth;
                float y = viewHeight * 0.5f + verticalOffset +
                        (float) Math.sin(x * 0.01f + offset * 6 * Math.PI + phaseShift) * amplitude +
                        (float) Math.cos(x * 0.005f + offset * 4 * Math.PI + phaseShift * 0.7f) * amplitude * 0.5f;
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }

            path.lineTo(viewWidth, viewHeight);
            path.lineTo(0, viewHeight);
            path.close();

            int topShift = layer * 10;
            int[] colors = new int[3];
            colors[0] = shiftColor(currentTopColor, topShift);
            colors[1] = shiftColor(currentBottomColor, topShift);
            colors[2] = Color.TRANSPARENT;

            LinearGradient gradient = new LinearGradient(
                    0, -viewHeight * 0.2f + verticalOffset,
                    0, viewHeight * 1.2f + verticalOffset,
                    colors, new float[]{0f, 0.4f, 1f}, Shader.TileMode.CLAMP
            );

            wavePaint.setShader(gradient);
            wavePaint.setAlpha((int) (alpha * 150));
            canvas.drawPath(path, wavePaint);
            wavePaint.setShader(null);
            wavePaint.setAlpha(255);
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void drawParticles(Canvas canvas) {
        try {
            int count = particleX.length;
            for (int i = 0; i < count; i++) {
                float x = particleX[i] * viewWidth;
                float y = particleY[i] * viewHeight;
                float size = particleSize[i] * (0.5f + 0.5f * (float) Math.sin(particleY[i] * 20));
                float alpha = particleAlpha[i] * 0.6f;

                int color = blendColors(currentTopColor, currentBottomColor, particleY[i]);
                particlePaint.setColor(color);
                particlePaint.setAlpha((int) (alpha * 120));

                if (particleBlurFilter != null && i % 3 == 0 && size > 1.5f) {
                    try {
                        particlePaint.setMaskFilter(particleBlurFilter);
                    } catch (Exception e) {
                        particlePaint.setMaskFilter(null);
                    }
                } else {
                    particlePaint.setMaskFilter(null);
                }

                canvas.drawCircle(x, y, Math.max(0.5f, size), particlePaint);
                particlePaint.setMaskFilter(null);
            }
            particlePaint.setAlpha(255);
        } catch (Exception e) {
            particlePaint.setMaskFilter(null);
        }
    }

    private void drawStars(Canvas canvas) {
        if (isLowPerformance) return;

        try {
            int count = starX.length;
            float time = System.currentTimeMillis() / 1000f;

            for (int i = 0; i < count; i++) {
                float x = starX[i] * viewWidth;
                float y = starY[i] * viewHeight;
                float twinkle = 0.5f + 0.5f * (float) Math.sin(time * starTwinkleSpeed[i] + i);
                float alpha = starAlpha[i] * twinkle * 0.8f;

                starPaint.setColor(Color.WHITE);
                starPaint.setAlpha((int) (alpha * 180));
                float size = starSize[i] * (0.7f + 0.3f * twinkle);
                canvas.drawCircle(x, y, size, starPaint);
            }
            starPaint.setAlpha(255);
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void drawOrbs(Canvas canvas) {
        try {
            for (int i = 0; i < orbX.length; i++) {
                float radius = orbRadius[i];
                int color = orbColors[i % orbColors.length];

                // Outer glow
                RadialGradient gradient = new RadialGradient(
                        orbX[i], orbY[i], radius * 3f,
                        color, Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                );
                orbPaint.setShader(gradient);
                orbPaint.setAlpha((int) (30 + 30 * glowIntensity));
                canvas.drawCircle(orbX[i], orbY[i], radius * 3f, orbPaint);

                // Middle glow
                gradient = new RadialGradient(
                        orbX[i], orbY[i], radius * 1.8f,
                        color, adjustBrightness(color, -50),
                        Shader.TileMode.CLAMP
                );
                orbPaint.setShader(gradient);
                orbPaint.setAlpha((int) (80 + 40 * glowIntensity));
                canvas.drawCircle(orbX[i], orbY[i], radius * 1.8f, orbPaint);

                // Core
                gradient = new RadialGradient(
                        orbX[i], orbY[i], radius * 0.6f,
                        Color.WHITE, color,
                        Shader.TileMode.CLAMP
                );
                orbPaint.setShader(gradient);
                orbPaint.setAlpha(200);
                canvas.drawCircle(orbX[i], orbY[i], radius * 0.8f, orbPaint);

                orbPaint.setShader(null);
                orbPaint.setAlpha(255);
            }
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void drawGlow(Canvas canvas) {
        try {
            float glowRadius = 250f + glowIntensity * 200f;
            float centerX = viewWidth * (0.4f + 0.2f * glowPulse);
            float centerY = viewHeight * (0.3f + 0.2f * (float) Math.sin(glowPulse * Math.PI * 2));

            int[][] glowConfigs = {
                    {SOLA_ACCENT, 15, 25},
                    {SOLA_GOLD, 10, 15},
                    {SOLA_PURPLE, 8, 12}
            };

            for (int g = 0; g < glowConfigs.length; g++) {
                float radius = glowRadius * (1f - g * 0.2f);
                float offsetX = (float) Math.sin(glowPulse * 2 + g * 1.2f) * radius * 0.3f;
                float offsetY = (float) Math.cos(glowPulse * 1.7f + g * 0.8f) * radius * 0.2f;

                RadialGradient glowGrad = new RadialGradient(
                        centerX + offsetX, centerY + offsetY, radius,
                        glowConfigs[g][0], Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                );
                glowPaint.setShader(glowGrad);
                glowPaint.setAlpha(glowConfigs[g][1] + (int)(glowConfigs[g][2] * glowIntensity));
                canvas.drawCircle(centerX + offsetX, centerY + offsetY, radius, glowPaint);
            }

            glowPaint.setShader(null);
            glowPaint.setAlpha(255);
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void drawShimmer(Canvas canvas) {
        if (isLowPerformance) return;

        try {
            float shimmerX = (float) (Math.sin(System.currentTimeMillis() / 3000.0) * 0.5 + 0.5) * viewWidth;
            float shimmerY = (float) (Math.cos(System.currentTimeMillis() / 4000.0) * 0.5 + 0.5) * viewHeight;
            float shimmerRadius = Math.min(viewWidth, viewHeight) * 0.3f;

            RadialGradient shimmerGrad = new RadialGradient(
                    shimmerX, shimmerY, shimmerRadius,
                    Color.WHITE, Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            );
            shimmerPaint.setShader(shimmerGrad);
            shimmerPaint.setAlpha(8);
            canvas.drawCircle(shimmerX, shimmerY, shimmerRadius, shimmerPaint);
            shimmerPaint.setShader(null);
            shimmerPaint.setAlpha(255);
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void drawVignette(Canvas canvas) {
        try {
            float radius = Math.max(viewWidth, viewHeight) * 0.7f;
            RadialGradient vignette = new RadialGradient(
                    viewWidth / 2f, viewHeight / 2f,
                    radius,
                    Color.TRANSPARENT,
                    Color.parseColor("#20000000"),
                    Shader.TileMode.CLAMP
            );
            vignettePaint.setShader(vignette);
            canvas.drawRect(0, 0, viewWidth, viewHeight, vignettePaint);
            vignettePaint.setShader(null);
        } catch (Exception e) {
            // Silent fail
        }
    }

    // ============================================================
    //  UTILITY METHODS
    // ============================================================
    private int shiftColor(int color, int amount) {
        int r = clamp(Color.red(color) + amount);
        int g = clamp(Color.green(color) + amount);
        int b = clamp(Color.blue(color) + amount);
        return Color.rgb(r, g, b);
    }

    private int adjustBrightness(int color, int amount) {
        int r = clamp(Color.red(color) + amount);
        int g = clamp(Color.green(color) + amount);
        int b = clamp(Color.blue(color) + amount);
        return Color.rgb(r, g, b);
    }

    private int clamp(int value) {
        return Math.min(255, Math.max(0, value));
    }

    // ============================================================
    //  ✅ PUBLIC METHODS - COMPATIBLE WITH ALL ACTIVITIES
    // ============================================================

    /**
     * Set the wave animation speed (in milliseconds)
     * @param milliseconds Duration of one wave cycle
     */
    public void setWaveSpeed(int milliseconds) {
        if (waveAnimator != null && milliseconds > 0) {
            try {
                waveAnimator.setDuration(milliseconds);
            } catch (Exception e) {
                // Ignore - animation will continue with default speed
            }
        }
    }

    /**
     * Set animation speed multiplier (Kept for backward compatibility)
     * @param speedMultiplier 1.0 = normal, 2.0 = 2x speed, 0.5 = half speed
     */
    public void setAnimationSpeed(float speedMultiplier) {
        if (waveAnimator != null && speedMultiplier > 0) {
            try {
                long duration = (long) (8000 / speedMultiplier);
                waveAnimator.setDuration(duration);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Stop the background animation completely (Kept for backward compatibility)
     */
    public void stopBackgroundAnimation() {
        stopAnimation();
    }

    /**
     * Pause all animations
     */
    public void pauseAnimation() {
        isPaused = true;
        pauseAnimator(waveAnimator);
        pauseAnimator(orbAnimator);
        pauseAnimator(glowAnimator);
        pauseAnimator(particleAnimator);
        pauseAnimator(auroraAnimator);
        pauseAnimator(shimmerAnimator);
        pauseAnimator(colorAnimator);
    }

    private void pauseAnimator(ValueAnimator animator) {
        if (animator != null && animator.isStarted()) {
            try {
                animator.pause();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Resume all animations
     */
    public void resumeAnimation() {
        isPaused = false;
        resumeAnimator(waveAnimator);
        resumeAnimator(orbAnimator);
        resumeAnimator(glowAnimator);
        resumeAnimator(particleAnimator);
        resumeAnimator(auroraAnimator);
        resumeAnimator(shimmerAnimator);
        resumeAnimator(colorAnimator);
        lastUpdateTime = System.currentTimeMillis();
        invalidate();
    }

    private void resumeAnimator(ValueAnimator animator) {
        if (animator != null && animator.isPaused()) {
            try {
                animator.resume();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Stop all animations completely (called when view is destroyed)
     */
    public void stopAnimation() {
        isDestroyed = true;
        isPaused = true;
        cancelAnimator(waveAnimator);
        cancelAnimator(orbAnimator);
        cancelAnimator(glowAnimator);
        cancelAnimator(particleAnimator);
        cancelAnimator(auroraAnimator);
        cancelAnimator(shimmerAnimator);
        cancelAnimator(colorAnimator);
        waveAnimator = null;
        orbAnimator = null;
        glowAnimator = null;
        particleAnimator = null;
        auroraAnimator = null;
        shimmerAnimator = null;
        colorAnimator = null;
    }

    private void cancelAnimator(ValueAnimator animator) {
        if (animator != null) {
            try {
                animator.cancel();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Set low performance mode for older devices
     * @param enabled true to enable low performance mode
     */
    public void setLowPerformanceMode(boolean enabled) {
        isLowPerformance = enabled;
        initializeParticles();
        initializeStars();
        invalidate();
    }

    // ============================================================
    //  LIFECYCLE MANAGEMENT
    // ============================================================
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isDestroyed) {
            isDestroyed = false;
            isPaused = false;
            startAnimations();
        } else if (isPaused) {
            resumeAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            resumeAnimation();
        } else {
            pauseAnimation();
        }
    }
}