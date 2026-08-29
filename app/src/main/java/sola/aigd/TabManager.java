package sola.aigd;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabManager {

    private static final String PREF_NAME = "sola_tabs";
    private static final String KEY_TAB_COUNT = "tab_count";
    private static final String KEY_TAB_PREFIX = "tab_";
    private static final String KEY_TAB_TITLE = "_title";
    private static final String KEY_TAB_URL = "_url";
    private static final String KEY_TAB_TIMESTAMP = "_timestamp";
    private static final String KEY_ACTIVE_TAB_ID = "active_tab_id";

    private static TabManager instance;
    private final Context context;
    private final SharedPreferences preferences;
    private List<TabClass> tabs;
    private int activeTabId = -1;
    private int nextId = 1;

    private TabManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.tabs = new ArrayList<>();
        loadTabs();
    }

    public static synchronized TabManager getInstance(Context context) {
        if (instance == null) {
            instance = new TabManager(context);
        }
        return instance;
    }

    private void loadTabs() {
        tabs.clear();
        int count = preferences.getInt(KEY_TAB_COUNT, 0);

        for (int i = 0; i < count; i++) {
            String prefix = KEY_TAB_PREFIX + i + "_";
            String title = preferences.getString(prefix + KEY_TAB_TITLE, "New Tab");
            String url = preferences.getString(prefix + KEY_TAB_URL, "about:blank");
            long timestamp = preferences.getLong(prefix + KEY_TAB_TIMESTAMP, System.currentTimeMillis());
            int id = preferences.getInt(prefix + "id", i + 1);

            TabClass tab = new TabClass(id, title, url, timestamp);
            tabs.add(tab);

            if (id >= nextId) {
                nextId = id + 1;
            }
        }

        sortTabs();
        activeTabId = preferences.getInt(KEY_ACTIVE_TAB_ID, -1);

        // If no tabs exist, create a default tab
        if (tabs.isEmpty()) {
            createNewTab("New Tab", "https://www.google.com");
        }
    }

    private void saveTabs() {
        SharedPreferences.Editor editor = preferences.edit();

        int oldCount = preferences.getInt(KEY_TAB_COUNT, 0);
        for (int i = 0; i < oldCount; i++) {
            String prefix = KEY_TAB_PREFIX + i + "_";
            editor.remove(prefix + KEY_TAB_TITLE);
            editor.remove(prefix + KEY_TAB_URL);
            editor.remove(prefix + KEY_TAB_TIMESTAMP);
            editor.remove(prefix + "id");
        }

        int count = tabs.size();
        editor.putInt(KEY_TAB_COUNT, count);

        for (int i = 0; i < count; i++) {
            TabClass tab = tabs.get(i);
            String prefix = KEY_TAB_PREFIX + i + "_";
            editor.putString(prefix + KEY_TAB_TITLE, tab.getTitle());
            editor.putString(prefix + KEY_TAB_URL, tab.getUrl());
            editor.putLong(prefix + KEY_TAB_TIMESTAMP, tab.getTimestamp());
            editor.putInt(prefix + "id", tab.getId());
        }

        editor.putInt(KEY_ACTIVE_TAB_ID, activeTabId);
        editor.apply();
    }

    private void sortTabs() {
        Collections.sort(tabs, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
    }

    public TabClass createNewTab() {
        return createNewTab("New Tab", "about:blank");
    }

    public TabClass createNewTab(String title, String url) {
        int id = nextId++;
        TabClass tab = new TabClass(id, title, url);
        tabs.add(0, tab);
        activeTabId = id;
        saveTabs();
        return tab;
    }

    public List<TabClass> getAllTabs() {
        return new ArrayList<>(tabs);
    }

    public TabClass getTabById(int id) {
        for (TabClass tab : tabs) {
            if (tab.getId() == id) {
                return tab;
            }
        }
        return null;
    }

    public TabClass getActiveTab() {
        if (activeTabId == -1) {
            if (!tabs.isEmpty()) {
                activeTabId = tabs.get(0).getId();
                saveTabs();
                return tabs.get(0);
            }
            return null;
        }
        return getTabById(activeTabId);
    }

    public int getActiveTabId() {
        return activeTabId;
    }

    public void setActiveTabId(int id) {
        this.activeTabId = id;
        saveTabs();
    }

    public void updateTab(int id, String title, String url) {
        TabClass tab = getTabById(id);
        if (tab != null) {
            tab.setTitle(title);
            tab.setUrl(url);
            tab.setTimestamp(System.currentTimeMillis());
            sortTabs();
            saveTabs();
        }
    }

    public void updateTabTitle(int id, String title) {
        TabClass tab = getTabById(id);
        if (tab != null) {
            tab.setTitle(title);
            saveTabs();
        }
    }

    public void updateTabUrl(int id, String url) {
        TabClass tab = getTabById(id);
        if (tab != null) {
            tab.setUrl(url);
            tab.setTimestamp(System.currentTimeMillis());
            sortTabs();
            saveTabs();
        }
    }

    public void deleteTab(int id) {
        TabClass tabToRemove = null;
        for (TabClass tab : tabs) {
            if (tab.getId() == id) {
                tabToRemove = tab;
                break;
            }
        }

        if (tabToRemove != null) {
            tabs.remove(tabToRemove);

            if (activeTabId == id) {
                if (!tabs.isEmpty()) {
                    activeTabId = tabs.get(0).getId();
                } else {
                    activeTabId = -1;
                }
            }

            saveTabs();
        }
    }

    public void deleteTabAtPosition(int position) {
        if (position >= 0 && position < tabs.size()) {
            TabClass tab = tabs.get(position);
            deleteTab(tab.getId());
        }
    }

    public void clearAllTabs() {
        tabs.clear();
        activeTabId = -1;
        saveTabs();
    }

    public int getTabCount() {
        return tabs.size();
    }

    public boolean hasTabs() {
        return !tabs.isEmpty();
    }

    public boolean tabExists(int id) {
        return getTabById(id) != null;
    }

    public TabClass getLastUsedTab() {
        if (tabs.isEmpty()) {
            return null;
        }
        sortTabs();
        return tabs.get(0);
    }

    public TabClass findTabByUrl(String url) {
        if (url == null) return null;
        for (TabClass tab : tabs) {
            if (url.equals(tab.getUrl())) {
                return tab;
            }
        }
        return null;
    }

    public int getTabIndexById(int id) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public void clearAllData() {
        tabs.clear();
        activeTabId = -1;
        preferences.edit().clear().apply();
    }

    public int getNextId() {
        return nextId;
    }

    public void resetToDefault() {
        clearAllData();
        createNewTab("Welcome to SOLA", "https://www.google.com");
    }

    public static class TabClass {
        private final int id;
        private String title;
        private String url;
        private long timestamp;
        private Bitmap favicon;

        public TabClass(int id, String title, String url) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.timestamp = System.currentTimeMillis();
        }

        public TabClass(int id, String title, String url, long timestamp) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title != null ? title : "New Tab";
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url != null ? url : "about:blank";
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public Bitmap getFavicon() {
            return favicon;
        }

        public void setFavicon(Bitmap favicon) {
            this.favicon = favicon;
        }

        @Override
        public String toString() {
            return "TabClass{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", url='" + url + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}