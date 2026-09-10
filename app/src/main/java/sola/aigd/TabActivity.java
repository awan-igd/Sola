package sola.aigd;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import sola.aigd.R;

public class TabActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private RecyclerView recyclerView;
    private TextView tabsCount, adLabel;
    private CardView backButton, newTabButton, clearAllButton, openNewTabButton;
    private LinearLayout emptyState, bottomAdContainer;
    private Banner bannerAd;
    private ProgressBar adLoader;
    private View topBar;
    private DataManager dataManager;
    private List<DataManager.TabItem> tabs;
    private TabAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);
        setupWindow();
        dataManager = DataManager.getInstance(this);
        initViews();
        setupInsets();
        setupRecycler();
        loadTabs();
        setupClicks();
        loadAd();
        startEntry();
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
        newTabButton = findViewById(R.id.newTabButton);
        tabsCount = findViewById(R.id.tabsCount);
        clearAllButton = findViewById(R.id.clearAllButton);
        recyclerView = findViewById(R.id.tabsRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        openNewTabButton = findViewById(R.id.openNewTabButton);
        bottomAdContainer = findViewById(R.id.bottomAdContainer);
        bannerAd = findViewById(R.id.bannerAd);
        adLoader = findViewById(R.id.adLoader);
        adLabel = findViewById(R.id.adLabel);
    }

    private void setupRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView r, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder t) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                if (pos >= 0 && pos < tabs.size()) deleteTab(pos);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void loadTabs() {
        tabs = dataManager.getAllTabs();
        if (tabs == null) tabs = new ArrayList<>();
        if (adapter == null) {
            adapter = new TabAdapter();
            recyclerView.setAdapter(adapter);
        } else adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        int c = tabs.size();
        tabsCount.setText(c + (c == 1 ? " tab" : " tabs"));
        if (c == 0) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            clearAllButton.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            clearAllButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupClicks() {
        backButton.setOnClickListener(v -> {
            anim(v);
            goBack();
        });
        newTabButton.setOnClickListener(v -> {
            anim(v);
            createNewTab();
        });
        openNewTabButton.setOnClickListener(v -> {
            anim(v);
            createNewTab();
        });
        clearAllButton.setOnClickListener(v -> {
            anim(v);
            dataManager.clearAllTabs();
            tabs.clear();
            adapter.notifyDataSetChanged();
            updateUI();
            Toast.makeText(this, "All tabs cleared", Toast.LENGTH_SHORT).show();
        });
    }

    // FIXED: Direct open SearchActivity - no setResult
    private void createNewTab() {
        int id = dataManager.createNewTab("New Tab", "https://www.google.com");
        DataManager.TabItem t = dataManager.getTabById(id);
        if (t == null) return;
        Intent i = new Intent(TabActivity.this, SearchActivity.class);
        i.putExtra("tab_id", t.getId());
        i.putExtra("tab_url", t.getUrl());
        i.putExtra("tab_title", t.getTitle());
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // FIXED: Direct open SearchActivity - no setResult
    private void openTab(DataManager.TabItem tab) {
        Intent i = new Intent(TabActivity.this, SearchActivity.class);
        i.putExtra("tab_id", tab.getId());
        i.putExtra("tab_url", tab.getUrl());
        i.putExtra("tab_title", tab.getTitle());
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void deleteTab(int pos) {
        if (pos < 0 || pos >= tabs.size()) return;
        DataManager.TabItem tab = tabs.get(pos);
        dataManager.deleteTab(tab.getId());
        tabs.remove(pos);
        adapter.notifyItemRemoved(pos);
        updateUI();
    }

    private void goBack() {
        Intent i = new Intent(TabActivity.this, SearchActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // if tabs empty, SearchActivity will handle creating default tab
        if (!tabs.isEmpty()) {
            DataManager.TabItem tab = tabs.get(0);
            i.putExtra("tab_id", tab.getId());
            i.putExtra("tab_url", tab.getUrl());
            i.putExtra("tab_title", tab.getTitle());
        }
        startActivity(i);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void startEntry() {
        topBar.setAlpha(0f);
        topBar.setTranslationY(-30f);
        topBar.animate().alpha(1f).translationY(0f).setDuration(350).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        recyclerView.setAlpha(0f);
        recyclerView.setTranslationY(20f);
        recyclerView.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(120).start();
    }

    private void anim(View v) {
        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
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
        goBack();
    }

    class TabAdapter extends RecyclerView.Adapter<TabAdapter.VH> {
        @Override
        public VH onCreateViewHolder(android.view.ViewGroup p, int v) {
            return new VH(getLayoutInflater().inflate(R.layout.item_tab, p, false));
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            DataManager.TabItem tab = tabs.get(pos);
            h.title.setText(tab.getTitle() == null || tab.getTitle().isEmpty() ? "New Tab" : tab.getTitle());
            h.url.setText(tab.getUrl());
            h.itemView.setAlpha(0f);
            h.itemView.setTranslationX(40f);
            h.itemView.animate().alpha(1f).translationX(0f).setDuration(300).setStartDelay(pos * 40L).setInterpolator(new OvershootInterpolator(0.8f)).start();

            h.itemView.setOnClickListener(v -> {
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()).start();
                openTab(tab);
            });
            h.close.setOnClickListener(v -> {
                v.animate().rotation(90f).scaleX(0.7f).scaleY(0.7f).setDuration(150).withEndAction(() -> {
                    int p = h.getBindingAdapterPosition();
                    if (p != RecyclerView.NO_POSITION) deleteTab(p);
                }).start();
            });
        }

        @Override
        public int getItemCount() {
            return tabs.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView title, url;
            CardView close;

            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tabTitle);
                url = v.findViewById(R.id.tabUrl);
                close = v.findViewById(R.id.closeButton);
            }
        }
    }
}