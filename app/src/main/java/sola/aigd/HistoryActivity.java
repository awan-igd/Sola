package sola.aigd;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sola.aigd.R;

public class HistoryActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private RecyclerView recyclerView;
    private TextView countText, adLabel;
    private CardView backButton, clearAllButton, openBrowserButton;
    private LinearLayout emptyState, bottomAdContainer;
    private Banner bannerAd;
    private ProgressBar adLoader;
    private View topBar;

    private DataManager dataManager;
    private List<DataManager.HistoryItem> list;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setupWindow();
        dataManager = DataManager.getInstance(this);
        initViews();
        setupInsets();
        setupRecycler();
        loadData();
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
        clearAllButton = findViewById(R.id.clearAllButton);
        countText = findViewById(R.id.historyCount);
        recyclerView = findViewById(R.id.historyRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        openBrowserButton = findViewById(R.id.openBrowserButton);
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
                int pos = vh.getAdapterPosition();
                if (pos >= 0 && pos < list.size()) delete(pos);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void loadData() {
        list = dataManager.getHistory();
        if (list == null) list = new ArrayList<>();
        if (adapter == null) {
            adapter = new HistoryAdapter();
            recyclerView.setAdapter(adapter);
        } else adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        int c = list.size();
        countText.setText(c + (c == 1 ? " item" : " items"));
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
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        openBrowserButton.setOnClickListener(v -> {
            anim(v);
            startActivity(new Intent(this, SearchActivity.class));
            finish();
        });
        clearAllButton.setOnClickListener(v -> {
            anim(v);
            dataManager.clearHistory();
            list.clear();
            adapter.notifyDataSetChanged();
            updateUI();
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void delete(int pos) {
        DataManager.HistoryItem item = list.get(pos);
        dataManager.removeHistoryItem(item.getUrl());
        list.remove(pos);
        adapter.notifyItemRemoved(pos);
        updateUI();
    }

    private void openItem(DataManager.HistoryItem item) {
        Intent i = new Intent(this, SearchActivity.class);
        i.setAction(Intent.ACTION_VIEW);
        i.setData(Uri.parse(item.getUrl()));
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private String formatTime(long ts) {
        try {
            return new SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault()).format(new Date(ts));
        } catch (Exception e) {
            return "Recently";
        }
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
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        @Override
        public VH onCreateViewHolder(android.view.ViewGroup p, int v) {
            return new VH(getLayoutInflater().inflate(R.layout.item_history, p, false));
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            DataManager.HistoryItem item = list.get(pos);
            h.title.setText(item.getTitle() == null || item.getTitle().isEmpty() ? "Untitled" : item.getTitle());
            h.url.setText(item.getUrl());
            h.time.setText(formatTime(item.getTimestamp()));
            h.itemView.setAlpha(0f);
            h.itemView.setTranslationX(40f);
            h.itemView.animate().alpha(1f).translationX(0f).setDuration(300).setStartDelay(pos * 35L).setInterpolator(new OvershootInterpolator(0.8f)).start();
            h.itemView.setOnClickListener(v -> {
                h.itemView.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction(() -> {
                    h.itemView.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    openItem(item);
                }).start();
            });
            h.delete.setOnClickListener(v -> {
                v.animate().rotation(90f).scaleX(0.7f).scaleY(0.7f).setDuration(150).withEndAction(() -> delete(h.getAdapterPosition())).start();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView title, url, time;
            CardView delete;

            VH(View v) {
                super(v);
                title = v.findViewById(R.id.historyTitle);
                url = v.findViewById(R.id.historyUrl);
                time = v.findViewById(R.id.historyTime);
                delete = v.findViewById(R.id.deleteButton);
            }
        }
    }
}