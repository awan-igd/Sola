package sola.aigd;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;

/**
 * SOLA Browser - Tabs Manager Activity
 * Manage all open tabs with premium UI
 * Created by Awan IGD
 */
public class TabActivity extends AppCompatActivity {

    // ============================================================
    //  CONSTANTS
    // ============================================================
    private static final int TAB_MANAGER_REQUEST = 2001;

    // ============================================================
    //  HANDLER
    // ============================================================
    private final Handler handler = new Handler(Looper.getMainLooper());

    // ============================================================
    //  VIEWS
    // ============================================================
    private RecyclerView tabsRecyclerView;
    private TextView tabsCountText;
    private LinearLayout backButton, clearAllButton, newTabButton;
    private LinearLayout emptyState, openNewTabButton;
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
    private List<DataManager.TabItem> tabsList;
    private TabAdapter adapter;
    private ItemTouchHelper itemTouchHelper;

    // ============================================================
    //  LIFECYCLE
    // ============================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        setupFullScreenWindow();
        initializeCore();
        initViews();
        setupWindowInsets();
        setupAnimatedBackground();
        loadTabs();
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
        topBar = findViewById(R.id.topBar);
        backButton = findViewById(R.id.backButton);
        newTabButton = findViewById(R.id.newTabButton);

        statsBar = findViewById(R.id.statsBar);
        tabsCountText = findViewById(R.id.tabsCount);
        clearAllButton = findViewById(R.id.clearAllButton);

        divider = findViewById(R.id.divider);

        tabsRecyclerView = findViewById(R.id.tabsRecyclerView);
        tabsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tabsRecyclerView.setItemViewCacheSize(20);
        tabsRecyclerView.setHasFixedSize(true);
        tabsRecyclerView.setNestedScrollingEnabled(false);

        emptyState = findViewById(R.id.emptyState);
        openNewTabButton = findViewById(R.id.openNewTabButton);

        bottomAdContainer = findViewById(R.id.bottomAdContainer);
        bannerAd = findViewById(R.id.bannerAd);
        adLoader = findViewById(R.id.adLoader);
        adLabel = findViewById(R.id.adLabel);

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
    //  LOAD TABS - Using DataManager
    // ============================================================
    private void loadTabs() {
        tabsList = dataManager.getAllTabs();
        if (tabsList == null) {
            tabsList = new ArrayList<>();
        }

        updateUI();

        if (adapter == null) {
            adapter = new TabAdapter(tabsList);
            tabsRecyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(tabsList);
        }
    }

    private void updateUI() {
        int count = tabsList != null ? tabsList.size() : 0;

        if (tabsCountText != null) {
            tabsCountText.setText(count + " tab" + (count != 1 ? "s" : ""));
        }

        if (count == 0) {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (tabsRecyclerView != null) tabsRecyclerView.setVisibility(View.GONE);
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (tabsRecyclerView != null) {
                tabsRecyclerView.setVisibility(View.VISIBLE);
                animateRecyclerViewItems();
            }
        }
    }

    private void animateRecyclerViewItems() {
        if (tabsRecyclerView != null) {
            tabsRecyclerView.setAlpha(0f);
            tabsRecyclerView.animate()
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
                if (position >= 0 && position < tabsList.size()) {
                    DataManager.TabItem tab = tabsList.get(position);
                    deleteTab(position, tab);
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
        itemTouchHelper.attachToRecyclerView(tabsRecyclerView);
    }

    // ============================================================
    //  CLICK LISTENERS
    // ============================================================
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            animateButtonClick(backButton);
            goBackToPreviousActivity();
        });

        newTabButton.setOnClickListener(v -> {
            animateButtonClick(newTabButton);
            openNewTab();
        });

        openNewTabButton.setOnClickListener(v -> {
            animateButtonClick(openNewTabButton);
            openNewTab();
        });

        clearAllButton.setOnClickListener(v -> {
            animateButtonClick(clearAllButton);
            if (tabsList == null || tabsList.isEmpty()) {
                showToast("No tabs to clear");
                return;
            }
            showClearAllDialog();
        });
    }

    // ============================================================
    //  BACK NAVIGATION - Return to previous activity
    // ============================================================
    private void goBackToPreviousActivity() {
        if (tabsList != null && !tabsList.isEmpty()) {
            // Return the first tab (most recent)
            DataManager.TabItem tab = tabsList.get(0);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("tab_id", tab.getId());
            resultIntent.putExtra("tab_url", tab.getUrl());
            resultIntent.putExtra("tab_title", tab.getTitle());
            resultIntent.putExtra("open_tab", true);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED);
        }
        animateExitAndFinish();
    }

    // ============================================================
    //  OPEN NEW TAB - Save and create
    // ============================================================
    private void openNewTab() {
        String homeUrl = "https://www.google.com";
        int newTabId = dataManager.createNewTab("New Tab", homeUrl);

        // Reload tabs
        loadTabs();

        // Return the new tab
        Intent resultIntent = new Intent();
        DataManager.TabItem newTab = dataManager.getTabById(newTabId);
        if (newTab != null) {
            resultIntent.putExtra("tab_id", newTab.getId());
            resultIntent.putExtra("tab_url", newTab.getUrl());
            resultIntent.putExtra("tab_title", newTab.getTitle());
            resultIntent.putExtra("open_new_tab", true);
            setResult(RESULT_OK, resultIntent);
        }

        showToast("New tab opened");
        animateExitAndFinish();
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

        if (title != null) title.setText("Clear All Tabs");
        if (message != null) {
            int count = tabsList != null ? tabsList.size() : 0;
            message.setText("Are you sure you want to close all " + count + " tabs?");
        }

        positiveBtn.setOnClickListener(v -> {
            animateButtonClick(positiveBtn);
            if (dataManager != null) {
                dataManager.clearAllTabs();
            }
            if (tabsList != null) {
                tabsList.clear();
            }
            if (adapter != null) adapter.notifyDataSetChanged();
            updateUI();
            showToast("All tabs closed");
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
    //  TAB OPERATIONS
    // ============================================================
    private void openTab(DataManager.TabItem tab) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("tab_id", tab.getId());
        resultIntent.putExtra("tab_url", tab.getUrl());
        resultIntent.putExtra("tab_title", tab.getTitle());
        resultIntent.putExtra("open_tab", true);
        setResult(RESULT_OK, resultIntent);
        animateExitAndFinish();
    }

    private void deleteTab(int position, DataManager.TabItem tab) {
        animateItemRemoval(position, tab);
    }

    private void animateItemRemoval(int position, DataManager.TabItem tab) {
        View view = tabsRecyclerView.findViewHolderForAdapterPosition(position) != null ?
                tabsRecyclerView.findViewHolderForAdapterPosition(position).itemView : null;

        if (view != null) {
            view.animate()
                    .alpha(0f)
                    .translationX(-100f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(250)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        if (dataManager != null) {
                            dataManager.deleteTab(tab.getId());
                        }
                        if (tabsList != null) {
                            tabsList.remove(position);
                        }
                        if (adapter != null) adapter.notifyItemRemoved(position);
                        updateUI();
                        showToast("Tab closed");
                    })
                    .start();
        } else {
            if (dataManager != null) {
                dataManager.deleteTab(tab.getId());
            }
            if (tabsList != null) {
                tabsList.remove(position);
            }
            if (adapter != null) adapter.notifyItemRemoved(position);
            updateUI();
            showToast("Tab closed");
        }
    }

    // ============================================================
    //  ANIMATIONS
    // ============================================================
    private void startEntryAnimations() {
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

    // ============================================================
    //  LIFECYCLE
    // ============================================================
    @Override
    protected void onResume() {
        super.onResume();
        loadTabs();
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
        goBackToPreviousActivity();
    }

    // ============================================================
    //  ADAPTER
    // ============================================================
    private class TabAdapter extends RecyclerView.Adapter<TabAdapter.TabViewHolder> {

        private List<DataManager.TabItem> tabs;
        private int lastAnimatedPosition = -1;

        public TabAdapter(List<DataManager.TabItem> tabs) {
            this.tabs = tabs;
        }

        public void updateData(List<DataManager.TabItem> newTabs) {
            this.tabs = newTabs;
            this.lastAnimatedPosition = -1;
            notifyDataSetChanged();
        }

        @Override
        public TabViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_tab, parent, false);
            return new TabViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TabViewHolder holder, int position) {
            if (tabs != null && position < tabs.size()) {
                DataManager.TabItem tab = tabs.get(position);
                holder.bind(tab, position);

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
            return tabs != null ? tabs.size() : 0;
        }

        // ============================================================
        //  VIEW HOLDER
        // ============================================================
        class TabViewHolder extends RecyclerView.ViewHolder {

            private final CardView cardView;
            private final ImageView tabIcon;
            private final TextView tabTitle, tabUrl;
            private final LinearLayout closeButton;

            public TabViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView;
                tabIcon = itemView.findViewById(R.id.tabIcon);
                tabTitle = itemView.findViewById(R.id.tabTitle);
                tabUrl = itemView.findViewById(R.id.tabUrl);
                closeButton = itemView.findViewById(R.id.closeButton);
            }

            public void bind(DataManager.TabItem tab, int position) {
                if (tab == null) return;

                String title = tab.getTitle();
                if (title == null || title.isEmpty()) {
                    title = "New Tab";
                }
                tabTitle.setText(title);

                String url = tab.getUrl();
                if (url == null || url.isEmpty()) {
                    url = "about:blank";
                }
                tabUrl.setText(url);

                setTabIcon(url);

                cardView.setOnClickListener(v -> {
                    animateCardClick(cardView);
                    openTab(tab);
                });

                closeButton.setOnClickListener(v -> {
                    animateCloseButton(closeButton);
                    int pos = getAdapterPosition();
                    if (pos >= 0 && pos < tabs.size()) {
                        deleteTab(pos, tab);
                    }
                });
            }

            private void setTabIcon(String url) {
                if (url == null) {
                    tabIcon.setImageResource(R.drawable.ic_web);
                    tabIcon.setColorFilter(
                            Color.parseColor("#6200EE"),
                            android.graphics.PorterDuff.Mode.SRC_IN
                    );
                    return;
                }

                String lowerUrl = url.toLowerCase();
                if (lowerUrl.contains("google")) {
                    tabIcon.setImageResource(R.drawable.ic_google);
                    tabIcon.setColorFilter(null);
                } else if (lowerUrl.contains("bing")) {
                    tabIcon.setImageResource(R.drawable.ic_bing);
                    tabIcon.setColorFilter(null);
                } else if (lowerUrl.contains("duckduckgo")) {
                    tabIcon.setImageResource(R.drawable.ic_duckduckgo);
                    tabIcon.setColorFilter(null);
                } else if (lowerUrl.contains("yahoo")) {
                    tabIcon.setImageResource(R.drawable.ic_yahoo);
                    tabIcon.setColorFilter(null);
                } else if (lowerUrl.contains("brave")) {
                    tabIcon.setImageResource(R.drawable.ic_brave);
                    tabIcon.setColorFilter(null);
                } else if (lowerUrl.contains("wikipedia")) {
                    tabIcon.setImageResource(R.drawable.ic_wikipedia);
                    tabIcon.setColorFilter(null);
                } else if (lowerUrl.contains("youtube")) {
                    tabIcon.setImageResource(R.drawable.ic_youtube);
                    tabIcon.setColorFilter(null);
                } else {
                    tabIcon.setImageResource(R.drawable.ic_web);
                    tabIcon.setColorFilter(
                            Color.parseColor("#6200EE"),
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