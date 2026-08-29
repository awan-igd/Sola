package sola.aigd;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * SOLA Browser - Data Manager
 * Handles all data persistence (Tabs, History, Bookmarks, Settings)
 * Created by Awan IGD
 */
public class DataManager {

    // ============================================================
    //  CONSTANTS
    // ============================================================
    private static final String PREF_NAME = "sola_data";

    // Keys
    private static final String KEY_TABS = "tabs";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_BOOKMARKS = "bookmarks";
    private static final String KEY_ACTIVE_TAB = "active_tab";
    private static final String KEY_SETTINGS = "settings";
    private static final String KEY_DOWNLOADS = "downloads";
    private static final String KEY_COOKIES = "cookies";
    private static final String KEY_SEARCH_ENGINE = "search_engine";
    private static final String KEY_DESKTOP_MODE = "desktop_mode";
    private static final String KEY_JAVASCRIPT = "javascript";
    private static final String KEY_INCOGNITO = "incognito";

    // ============================================================
    //  SINGLETON
    // ============================================================
    private static DataManager instance;
    private final Context context;
    private final SharedPreferences preferences;
    private final Gson gson;

    // In-memory data
    private List<TabItem> tabs;
    private List<HistoryItem> history;
    private List<BookmarkItem> bookmarks;
    private int nextId = 1;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================
    private DataManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new GsonBuilder().setLenient().create();
        this.tabs = new ArrayList<>();
        this.history = new ArrayList<>();
        this.bookmarks = new ArrayList<>();
        loadAllData();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    // ============================================================
    //  LOAD ALL DATA
    // ============================================================
    private void loadAllData() {
        loadTabsFromPrefs();
        loadHistoryFromPrefs();
        loadBookmarksFromPrefs();
    }

    // ============================================================
    //  TAB OPERATIONS
    // ============================================================

    /**
     * Create a new tab
     */
    public int createNewTab(String title, String url) {
        int id = nextId++;
        TabItem tab = new TabItem(id, title, url);
        tabs.add(0, tab);
        saveTabsToPrefs();
        return id;
    }

    /**
     * Get all tabs
     */
    public List<TabItem> getAllTabs() {
        return new ArrayList<>(tabs);
    }

    /**
     * Get tab at position
     */
    public TabItem getTabAt(int position) {
        if (position >= 0 && position < tabs.size()) {
            return tabs.get(position);
        }
        return null;
    }

    /**
     * Get tab by ID
     */
    public TabItem getTabById(int id) {
        for (TabItem tab : tabs) {
            if (tab.getId() == id) {
                return tab;
            }
        }
        return null;
    }

    /**
     * Update tab
     */
    public void updateTab(int id, String title, String url) {
        TabItem tab = getTabById(id);
        if (tab != null) {
            tab.setTitle(title);
            tab.setUrl(url);
            saveTabsToPrefs();
        }
    }

    /**
     * Delete tab
     */
    public void deleteTab(int id) {
        TabItem toRemove = null;
        for (TabItem tab : tabs) {
            if (tab.getId() == id) {
                toRemove = tab;
                break;
            }
        }
        if (toRemove != null) {
            tabs.remove(toRemove);
            saveTabsToPrefs();
        }
    }

    /**
     * Get tab count
     */
    public int getTabCount() {
        return tabs.size();
    }

    /**
     * Clear all tabs
     */
    public void clearAllTabs() {
        tabs.clear();
        saveTabsToPrefs();
    }

    // ============================================================
    //  HISTORY OPERATIONS
    // ============================================================

    /**
     * Add to history
     */
    public void addToHistory(String title, String url) {
        // Remove if exists
        HistoryItem existing = null;
        for (HistoryItem item : history) {
            if (item.getUrl().equals(url)) {
                existing = item;
                break;
            }
        }
        if (existing != null) {
            history.remove(existing);
        }

        // Add new
        history.add(0, new HistoryItem(title, url, System.currentTimeMillis()));

        // Limit history size
        if (history.size() > 100) {
            history = history.subList(0, 100);
        }

        saveHistoryToPrefs();
    }

    /**
     * Get all history
     */
    public List<HistoryItem> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Remove a history item by URL
     */
    public void removeHistoryItem(String url) {
        HistoryItem toRemove = null;
        for (HistoryItem item : history) {
            if (item.getUrl().equals(url)) {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null) {
            history.remove(toRemove);
            saveHistoryToPrefs();
        }
    }

    /**
     * Clear history
     */
    public void clearHistory() {
        history.clear();
        saveHistoryToPrefs();
    }

    /**
     * Get history count
     */
    public int getHistoryCount() {
        return history.size();
    }

    // ============================================================
    //  BOOKMARK OPERATIONS
    // ============================================================

    /**
     * Add bookmark
     */
    public void addBookmark(String title, String url) {
        // Check if exists
        for (BookmarkItem item : bookmarks) {
            if (item.getUrl().equals(url)) {
                return;
            }
        }
        bookmarks.add(0, new BookmarkItem(title, url, System.currentTimeMillis()));
        saveBookmarksToPrefs();
    }

    /**
     * Remove bookmark
     */
    public void removeBookmark(String url) {
        BookmarkItem toRemove = null;
        for (BookmarkItem item : bookmarks) {
            if (item.getUrl().equals(url)) {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null) {
            bookmarks.remove(toRemove);
            saveBookmarksToPrefs();
        }
    }

    /**
     * Check if bookmarked
     */
    public boolean isBookmarked(String url) {
        for (BookmarkItem item : bookmarks) {
            if (item.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all bookmarks
     */
    public List<BookmarkItem> getBookmarks() {
        return new ArrayList<>(bookmarks);
    }

    /**
     * Get bookmark count
     */
    public int getBookmarkCount() {
        return bookmarks.size();
    }

    /**
     * Clear bookmarks
     */
    public void clearBookmarks() {
        bookmarks.clear();
        saveBookmarksToPrefs();
    }

    // ============================================================
    //  SETTINGS OPERATIONS
    // ============================================================

    /**
     * Get desktop mode
     */
    public boolean isDesktopMode() {
        return preferences.getBoolean(KEY_DESKTOP_MODE, false);
    }

    /**
     * Set desktop mode
     */
    public void setDesktopMode(boolean enabled) {
        preferences.edit().putBoolean(KEY_DESKTOP_MODE, enabled).apply();
    }

    /**
     * Get JavaScript enabled
     */
    public boolean isJavaScriptEnabled() {
        return preferences.getBoolean(KEY_JAVASCRIPT, true);
    }

    /**
     * Set JavaScript enabled
     */
    public void setJavaScriptEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_JAVASCRIPT, enabled).apply();
    }

    /**
     * Get incognito mode
     */
    public boolean isIncognitoMode() {
        return preferences.getBoolean(KEY_INCOGNITO, false);
    }

    /**
     * Set incognito mode
     */
    public void setIncognitoMode(boolean enabled) {
        preferences.edit().putBoolean(KEY_INCOGNITO, enabled).apply();
    }

    /**
     * Get search engine
     */
    public String getSearchEngine() {
        return preferences.getString(KEY_SEARCH_ENGINE, "Google");
    }

    /**
     * Set search engine
     */
    public void setSearchEngine(String engine) {
        preferences.edit().putString(KEY_SEARCH_ENGINE, engine).apply();
    }

    // ============================================================
    //  SAVE TO PREFS
    // ============================================================

    private void saveTabsToPrefs() {
        String json = gson.toJson(tabs);
        preferences.edit().putString(KEY_TABS, json).apply();
    }

    private void loadTabsFromPrefs() {
        String json = preferences.getString(KEY_TABS, "");
        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<TabItem>>() {}.getType();
                List<TabItem> loaded = gson.fromJson(json, type);
                if (loaded != null) {
                    tabs = loaded;
                    // Find next ID
                    for (TabItem tab : tabs) {
                        if (tab.getId() >= nextId) {
                            nextId = tab.getId() + 1;
                        }
                    }
                }
            } catch (Exception e) {
                tabs = new ArrayList<>();
            }
        } else {
            tabs = new ArrayList<>();
        }
    }

    private void saveHistoryToPrefs() {
        String json = gson.toJson(history);
        preferences.edit().putString(KEY_HISTORY, json).apply();
    }

    private void loadHistoryFromPrefs() {
        String json = preferences.getString(KEY_HISTORY, "");
        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<HistoryItem>>() {}.getType();
                List<HistoryItem> loaded = gson.fromJson(json, type);
                if (loaded != null) {
                    history = loaded;
                }
            } catch (Exception e) {
                history = new ArrayList<>();
            }
        } else {
            history = new ArrayList<>();
        }
    }

    private void saveBookmarksToPrefs() {
        String json = gson.toJson(bookmarks);
        preferences.edit().putString(KEY_BOOKMARKS, json).apply();
    }

    private void loadBookmarksFromPrefs() {
        String json = preferences.getString(KEY_BOOKMARKS, "");
        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<BookmarkItem>>() {}.getType();
                List<BookmarkItem> loaded = gson.fromJson(json, type);
                if (loaded != null) {
                    bookmarks = loaded;
                }
            } catch (Exception e) {
                bookmarks = new ArrayList<>();
            }
        } else {
            bookmarks = new ArrayList<>();
        }
    }

    // ============================================================
    //  CLEAR DATA
    // ============================================================

    /**
     * Clear all data
     */
    public void clearAllData() {
        tabs.clear();
        history.clear();
        bookmarks.clear();
        preferences.edit().clear().apply();
    }

    /**
     * Clear tabs data
     */
    public void clearTabs() {
        tabs.clear();
        saveTabsToPrefs();
    }

    /**
     * Clear history data
     */
    public void clearHistoryData() {
        history.clear();
        saveHistoryToPrefs();
    }

    /**
     * Clear bookmarks data
     */
    public void clearBookmarksData() {
        bookmarks.clear();
        saveBookmarksToPrefs();
    }

    // ============================================================
    //  INNER CLASSES - TabItem
    // ============================================================
    public static class TabItem {
        private int id;
        private String title;
        private String url;
        private long timestamp;

        public TabItem() {
            // Default constructor
        }

        public TabItem(int id, String title, String url) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.timestamp = System.currentTimeMillis();
        }

        public TabItem(int id, String title, String url, long timestamp) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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
    }

    // ============================================================
    //  INNER CLASSES - HistoryItem
    // ============================================================
    public static class HistoryItem {
        private String title;
        private String url;
        private long timestamp;

        public HistoryItem() {
            // Default constructor
        }

        public HistoryItem(String title, String url, long timestamp) {
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
        }

        public String getTitle() {
            return title != null ? title : "Untitled";
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url != null ? url : "";
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
    }

    // ============================================================
    //  INNER CLASSES - BookmarkItem
    // ============================================================
    public static class BookmarkItem {
        private String title;
        private String url;
        private long timestamp;

        public BookmarkItem() {
            // Default constructor
        }

        public BookmarkItem(String title, String url, long timestamp) {
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
        }

        public String getTitle() {
            return title != null ? title : "Bookmark";
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url != null ? url : "";
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
    }

    // ============================================================
    //  INNER CLASSES - DownloadItem
    // ============================================================
    public static class DownloadItem {
        private String fileName;
        private String url;
        private String filePath;
        private long size;
        private long timestamp;
        private int status; // 0=pending, 1=downloading, 2=complete, 3=failed

        public DownloadItem() {
            // Default constructor
        }

        public DownloadItem(String fileName, String url, String filePath, long size) {
            this.fileName = fileName;
            this.url = url;
            this.filePath = filePath;
            this.size = size;
            this.timestamp = System.currentTimeMillis();
            this.status = 0;
        }

        public String getFileName() {
            return fileName != null ? fileName : "Download";
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getUrl() {
            return url != null ? url : "";
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFilePath() {
            return filePath != null ? filePath : "";
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    // ============================================================
    //  INNER CLASSES - Settings
    // ============================================================
    public static class Settings {
        private boolean darkMode;
        private boolean incognitoMode;
        private boolean saveHistory;
        private boolean saveCookies;
        private boolean blockAds;
        private boolean blockTrackers;
        private String homePage;
        private String searchEngine;
        private int fontSize;

        public Settings() {
            this.darkMode = false;
            this.incognitoMode = false;
            this.saveHistory = true;
            this.saveCookies = true;
            this.blockAds = false;
            this.blockTrackers = false;
            this.homePage = "https://www.google.com";
            this.searchEngine = "Google";
            this.fontSize = 16;
        }

        public boolean isDarkMode() {
            return darkMode;
        }

        public void setDarkMode(boolean darkMode) {
            this.darkMode = darkMode;
        }

        public boolean isIncognitoMode() {
            return incognitoMode;
        }

        public void setIncognitoMode(boolean incognitoMode) {
            this.incognitoMode = incognitoMode;
        }

        public boolean isSaveHistory() {
            return saveHistory;
        }

        public void setSaveHistory(boolean saveHistory) {
            this.saveHistory = saveHistory;
        }

        public boolean isSaveCookies() {
            return saveCookies;
        }

        public void setSaveCookies(boolean saveCookies) {
            this.saveCookies = saveCookies;
        }

        public boolean isBlockAds() {
            return blockAds;
        }

        public void setBlockAds(boolean blockAds) {
            this.blockAds = blockAds;
        }

        public boolean isBlockTrackers() {
            return blockTrackers;
        }

        public void setBlockTrackers(boolean blockTrackers) {
            this.blockTrackers = blockTrackers;
        }

        public String getHomePage() {
            return homePage != null ? homePage : "https://www.google.com";
        }

        public void setHomePage(String homePage) {
            this.homePage = homePage;
        }

        public String getSearchEngine() {
            return searchEngine != null ? searchEngine : "Google";
        }

        public void setSearchEngine(String searchEngine) {
            this.searchEngine = searchEngine;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }
    }
}