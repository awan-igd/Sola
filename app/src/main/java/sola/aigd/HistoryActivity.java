package sola.aigd;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SOLA Browser - History Activity
 * View and manage browsing history
 * Created by Awan IGD
 */
public class HistoryActivity extends AppCompatActivity {

    // ============================================================
    //  CONSTANTS
    // ============================================================
    private static final long ANIMATION_DURATION = 300;

    // ============================================================
    //  HANDLER
    // ============================================================
    private final Handler handler = new Handler(Looper.getMainLooper());

    // ============================================================
    //  VIEWS
    // ============================================================
    private RecyclerView historyRecyclerView;
    private TextView historyCountText;
    private LinearLayout backButton, clearAllButton;
    private LinearLayout emptyState, openBrowserButton;
    private View topBar, statsBar, divider;
    private AnimatedBackground animatedBackground;

    // ============================================================
    //  AD VIEWS
    // ============================================================
    private LinearLayout bottomAdContainer;
    private Banner bannerAd;
    private ProgressBar adLoader;
    private TextView adLabel;

    // ============================================================
    //  DATA
    // ============================================================
    private DataManager dataManager;
    private List<DataManager.HistoryItem> historyList;
    private HistoryAdapter adapter;
    private ItemTouchHelper itemTouchHelper;

    // ============================================================
    //  LIFECYCLE
    // ============================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        setupFullScreenWindow();
        initializeCore();
        initViews();
        setupWindowInsets();
        setupAnimatedBackground();
        loadHistory();
        setupClickListeners();
        setupSwipeToDelete();
        startEntryAnimations();
        loadBannerAd();
    }

    // ============================================================
    //  INITIALIZATION
    // ============================================================
    private void initializeCore() {
        dataManager = DataManager.getInstance(this);
    }

    private void initViews() {
        // Top bar
        topBar = findViewById(R.id.topBar);
        backButton = findViewById(R.id.backButton);
        clearAllButton = findViewById(R.id.clearAllButton);

        // Stats
        statsBar = findViewById(R.id.statsBar);
        historyCountText = findViewById(R.id.historyCount);

        // Divider
        divider = findViewById(R.id.divider);

        // RecyclerView
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setItemViewCacheSize(20);
        historyRecyclerView.setHasFixedSize(true);
        historyRecyclerView.setNestedScrollingEnabled(false);

        // Empty state
        emptyState = findViewById(R.id.emptyState);
        openBrowserButton = findViewById(R.id.openBrowserButton);

        // Ad
        bottomAdContainer = findViewById(R.id.bottomAdContainer);
        bannerAd = findViewById(R.id.bannerAd);
        adLoader = findViewById(R.id.adLoader);
        adLabel = findViewById(R.id.adLabel);

        // Background
        animatedBackground = findViewById(R.id.animatedBackground);
    }

    // ============================================================
    //  FULL SCREEN SETUP
    // ============================================================
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
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomAdContainer, (v, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), navBarHeight);
            return insets;
        });
    }

    // ============================================================
    //  BACKGROUND
    // ============================================================
    private void setupAnimatedBackground() {
        if (animatedBackground != null) {
            animatedBackground.setWaveSpeed(5000);
            animatedBackground.resumeAnimation();
        }
    }

    // ============================================================
    //  BANNER AD
    // ============================================================
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

    // ============================================================
    //  LOAD HISTORY
    // ============================================================
    private void loadHistory() {
        historyList = dataManager.getHistory();
        if (historyList == null) {
            historyList = new ArrayList<>();
        }

        updateUI();

        if (adapter == null) {
            adapter = new HistoryAdapter(historyList);
            historyRecyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(historyList);
        }
    }

    private void updateUI() {
        int count = historyList != null ? historyList.size() : 0;

        if (historyCountText != null) {
            historyCountText.setText(count + " item" + (count != 1 ? "s" : ""));
        }

        if (count == 0) {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (historyRecyclerView != null) historyRecyclerView.setVisibility(View.GONE);
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (historyRecyclerView != null) {
                historyRecyclerView.setVisibility(View.VISIBLE);
                animateRecyclerViewItems();
            }
        }
    }

    private void animateRecyclerViewItems() {
        if (historyRecyclerView != null) {
            historyRecyclerView.setAlpha(0f);
            historyRecyclerView.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .setStartDelay(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    // ============================================================
    //  SWIPE TO DELETE
    // ============================================================
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < historyList.size()) {
                    DataManager.HistoryItem item = historyList.get(position);
                    deleteHistoryItem(position, item);
                }
            }

            @Override
            public void onChildDraw(android.graphics.Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                float alpha = 1f - Math.abs(dX) / itemView.getWidth();
                itemView.setAlpha(alpha);
                itemView.setTranslationX(dX);

                if (dX > 50) {
                    itemView.setBackgroundColor(Color.parseColor("#30FF6B6B"));
                } else {
                    itemView.setBackgroundColor(Color.TRANSPARENT);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setAlpha(1f);
                viewHolder.itemView.setTranslationX(0f);
                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        };

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(historyRecyclerView);
    }

    // ============================================================
    //  CLICK LISTENERS
    // ============================================================
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> {
            animateButtonClick(backButton);
            finishWithAnimation();
        });

        // Clear all
        clearAllButton.setOnClickListener(v -> {
            animateButtonClick(clearAllButton);
            if (historyList == null || historyList.isEmpty()) {
                showToast("No history to clear");
                return;
            }
            showClearAllDialog();
        });

        // Open browser
        openBrowserButton.setOnClickListener(v -> {
            animateButtonClick(openBrowserButton);
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            finishWithAnimation();
        });
    }

    // ============================================================
    //  DIALOGS
    // ============================================================
    private void showClearAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_clear_tabs, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView message = dialogView.findViewById(R.id.dialogMessage);
        TextView positiveBtn = dialogView.findViewById(R.id.positiveBtn);
        TextView negativeBtn = dialogView.findViewById(R.id.negativeBtn);

        if (title != null) title.setText("Clear History");
        if (message != null) {
            int count = historyList != null ? historyList.size() : 0;
            message.setText("Are you sure you want to clear all " + count + " history items?");
        }

        positiveBtn.setOnClickListener(v -> {
            animateButtonClick(positiveBtn);
            if (dataManager != null) {
                dataManager.clearHistory();
            }
            if (historyList != null) {
                historyList.clear();
            }
            if (adapter != null) adapter.notifyDataSetChanged();
            updateUI();
            showToast("History cleared");
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
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        dialog.show();
    }

    // ============================================================
    //  HISTORY OPERATIONS - FIXED!
    // ============================================================
    private void openHistoryItem(DataManager.HistoryItem item) {
        String url = item.getUrl();
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(this, SearchActivity.class);
            // Use ACTION_VIEW with data URI for proper handling
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finishWithAnimation();
        }
    }

    private void deleteHistoryItem(int position, DataManager.HistoryItem item) {
        animateItemRemoval(position, item);
    }

    private void animateItemRemoval(int position, DataManager.HistoryItem item) {
        View view = historyRecyclerView.findViewHolderForAdapterPosition(position) != null ?
                historyRecyclerView.findViewHolderForAdapterPosition(position).itemView : null;

        if (view != null) {
            view.animate()
                    .alpha(0f)
                    .translationX(-100f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(250)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        // Remove from DataManager
                        if (dataManager != null) {
                            // Remove by URL
                            dataManager.removeHistoryItem(item.getUrl());
                        }
                        if (historyList != null) {
                            historyList.remove(position);
                        }
                        if (adapter != null) adapter.notifyItemRemoved(position);
                        updateUI();
                        showToast("History item removed");
                    })
                    .start();
        } else {
            if (dataManager != null) {
                dataManager.removeHistoryItem(item.getUrl());
            }
            if (historyList != null) {
                historyList.remove(position);
            }
            if (adapter != null) adapter.notifyItemRemoved(position);
            updateUI();
            showToast("History item removed");
        }
    }

    // ============================================================
    //  ANIMATIONS
    // ============================================================
    private void startEntryAnimations() {
        // Top bar
        if (topBar != null) {
            topBar.setAlpha(0f);
            topBar.setTranslationY(-50f);
            topBar.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // Stats bar
        if (statsBar != null) {
            statsBar.setAlpha(0f);
            statsBar.setTranslationY(-20f);
            statsBar.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(100)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // Divider
        if (divider != null) {
            divider.setAlpha(0f);
            divider.setScaleX(0f);
            divider.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setDuration(300)
                    .setStartDelay(200)
                    .start();
        }

        // Empty state
        if (emptyState != null && emptyState.getVisibility() == View.VISIBLE) {
            emptyState.setAlpha(0f);
            emptyState.setScaleX(0.9f);
            emptyState.setScaleY(0.9f);
            emptyState.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setStartDelay(300)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
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

    private void animateExitAndFinish() {
        View root = findViewById(android.R.id.content);
        if (root != null) {
            root.animate()
                    .alpha(0f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        finish();
                        overridePendingTransition(0, 0);
                    })
                    .start();
        } else {
            finishWithAnimation();
        }
    }

    private void finishWithAnimation() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // ============================================================
    //  UTILITY
    // ============================================================
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // ============================================================
    //  LIFECYCLE
    // ============================================================
    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
        setupAnimatedBackground();
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

    // ============================================================
    //  ADAPTER
    // ============================================================
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

        private List<DataManager.HistoryItem> items;
        private int lastAnimatedPosition = -1;

        public HistoryAdapter(List<DataManager.HistoryItem> items) {
            this.items = items;
        }

        public void updateData(List<DataManager.HistoryItem> newItems) {
            this.items = newItems;
            this.lastAnimatedPosition = -1;
            notifyDataSetChanged();
        }

        @Override
        public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryViewHolder holder, int position) {
            if (items != null && position < items.size()) {
                DataManager.HistoryItem item = items.get(position);
                holder.bind(item, position);

                if (position > lastAnimatedPosition) {
                    animateItem(holder.itemView, position);
                    lastAnimatedPosition = position;
                }
            }
        }

        private void animateItem(View view, int position) {
            view.setAlpha(0f);
            view.setTranslationX(100f);
            view.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(300)
                    .setStartDelay(Math.min(position * 50L, 500L))
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        // ============================================================
        //  VIEW HOLDER
        // ============================================================
        class HistoryViewHolder extends RecyclerView.ViewHolder {

            private final CardView cardView;
            private final ImageView icon;
            private final TextView titleText;
            private final TextView urlText;
            private final TextView timeText;
            private final LinearLayout closeButton;

            public HistoryViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView;
                icon = itemView.findViewById(R.id.historyIcon);
                titleText = itemView.findViewById(R.id.historyTitle);
                urlText = itemView.findViewById(R.id.historyUrl);
                timeText = itemView.findViewById(R.id.historyTime);
                closeButton = itemView.findViewById(R.id.closeButton);
            }

            public void bind(DataManager.HistoryItem item, int position) {
                if (item == null) return;

                // Title
                String title = item.getTitle();
                if (title == null || title.isEmpty()) {
                    title = "Untitled";
                }
                titleText.setText(title);

                // URL
                String url = item.getUrl();
                if (url == null || url.isEmpty()) {
                    url = "about:blank";
                }
                urlText.setText(url);

                // Time
                timeText.setText(formatTimestamp(item.getTimestamp()));

                // Icon based on URL
                setIcon(url);

                // Card click - open item
                cardView.setOnClickListener(v -> {
                    animateCardClick(cardView);
                    openHistoryItem(item);
                });

                // Close button click - delete item
                closeButton.setOnClickListener(v -> {
                    animateCloseButton(closeButton);
                    int pos = getAdapterPosition();
                    if (pos >= 0 && pos < items.size()) {
                        deleteHistoryItem(pos, item);
                    }
                });
            }

            private void setIcon(String url) {
                if (url == null) {
                    icon.setImageResource(R.drawable.ic_history);
                    icon.setColorFilter(
                            Color.parseColor("#FFD700"),
                            android.graphics.PorterDuff.Mode.SRC_IN
                    );
                    return;
                }

                String lowerUrl = url.toLowerCase();
                if (lowerUrl.contains("google")) {
                    icon.setImageResource(R.drawable.ic_google);
                    icon.setColorFilter(null);
                } else if (lowerUrl.contains("bing")) {
                    icon.setImageResource(R.drawable.ic_bing);
                    icon.setColorFilter(null);
                } else if (lowerUrl.contains("duckduckgo")) {
                    icon.setImageResource(R.drawable.ic_duckduckgo);
                    icon.setColorFilter(null);
                } else if (lowerUrl.contains("yahoo")) {
                    icon.setImageResource(R.drawable.ic_yahoo);
                    icon.setColorFilter(null);
                } else if (lowerUrl.contains("brave")) {
                    icon.setImageResource(R.drawable.ic_brave);
                    icon.setColorFilter(null);
                } else if (lowerUrl.contains("wikipedia")) {
                    icon.setImageResource(R.drawable.ic_wikipedia);
                    icon.setColorFilter(null);
                } else if (lowerUrl.contains("youtube")) {
                    icon.setImageResource(R.drawable.ic_youtube);
                    icon.setColorFilter(null);
                } else {
                    icon.setImageResource(R.drawable.ic_history);
                    icon.setColorFilter(
                            Color.parseColor("#FFD700"),
                            android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }
            }

            private void animateCardClick(View view) {
                view.animate()
                        .scaleX(0.98f)
                        .scaleY(0.98f)
                        .setDuration(100)
                        .withEndAction(() -> view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start())
                        .start();
            }

            private void animateCloseButton(View view) {
                view.animate()
                        .scaleX(0.7f)
                        .scaleY(0.7f)
                        .setDuration(100)
                        .withEndAction(() -> view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start())
                        .start();
            }
        }
    }
}