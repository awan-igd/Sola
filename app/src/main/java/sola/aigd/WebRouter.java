package sola.aigd;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * SOLA Browser - Web Router
 * Handles all navigation and URL routing
 * Created by Awan IGD
 */
public class WebRouter {

    // ========== INTENT KEYS ==========
    private static final String KEY_URL = "router_url";
    private static final String KEY_QUERY = "router_query";
    private static final String KEY_SEARCH_URL = "router_search_url";
    private static final String KEY_INCOGNITO = "router_incognito";
    // ========== SINGLETON ==========
    private static volatile WebRouter instance;
    private final Context context;

    // ========== CONSTRUCTOR ==========
    private WebRouter(Context context) {
        this.context = context.getApplicationContext();
    }

    public static WebRouter getInstance(Context context) {
        if (instance == null) {
            synchronized (WebRouter.class) {
                if (instance == null) {
                    instance = new WebRouter(context);
                }
            }
        }
        return instance;
    }

    // =============================================================
    // OPEN METHODS
    // =============================================================

    /**
     * Extract routing data from intent
     */
    public static RouterData receive(Context context, Intent intent) {
        RouterData data = new RouterData();

        if (context == null) {
            data.type = RouterData.TYPE_HOME;
            data.url = SEManager.getInstance(context).getCurrentEngineHomeUrl();
            return data;
        }

        if (intent == null) {
            data.type = RouterData.TYPE_HOME;
            data.url = SEManager.getInstance(context).getCurrentEngineHomeUrl();
            return data;
        }

        // 1. Check for URL
        String url = intent.getStringExtra(KEY_URL);
        if (!TextUtils.isEmpty(url)) {
            data.type = RouterData.TYPE_URL;
            data.url = url;
            data.incognito = intent.getBooleanExtra(KEY_INCOGNITO, false);
            return data;
        }

        // 2. Check for Search
        String query = intent.getStringExtra(KEY_QUERY);
        if (!TextUtils.isEmpty(query)) {
            data.type = RouterData.TYPE_SEARCH;
            data.query = query;
            data.incognito = intent.getBooleanExtra(KEY_INCOGNITO, false);

            String searchUrl = intent.getStringExtra(KEY_SEARCH_URL);
            if (TextUtils.isEmpty(searchUrl)) {
                searchUrl = SEManager.getInstance(context).buildSearchUrl(query);
            }
            data.url = searchUrl;
            return data;
        }

        // 3. Check for ACTION_VIEW
        if (Intent.ACTION_VIEW.equals(intent.getAction())
                && intent.getData() != null) {
            data.type = RouterData.TYPE_URL;
            data.url = intent.getData().toString();
            data.incognito = intent.getBooleanExtra(KEY_INCOGNITO, false);
            return data;
        }

        // 4. Check incognito home
        if (intent.getBooleanExtra(KEY_INCOGNITO, false)) {
            data.type = RouterData.TYPE_HOME;
            data.url = SEManager.getInstance(context).getCurrentEngineHomeUrl();
            data.incognito = true;
            return data;
        }

        // 5. Default Home
        data.type = RouterData.TYPE_HOME;
        data.url = SEManager.getInstance(context).getCurrentEngineHomeUrl();
        data.incognito = false;

        return data;
    }

    /**
     * Check if a string is a valid URL
     */
    public static boolean isValidUrl(String input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }

        try {
            Uri uri = Uri.parse(input);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
                return true;
            }

            if (host != null && host.contains(".") && host.length() > 3) {
                return true;
            }
        } catch (Exception e) {
            // Not a valid URL
        }

        // Check for common TLDs
        return input.contains(".") &&
                !input.contains(" ") &&
                (input.endsWith(".com") || input.endsWith(".org") ||
                        input.endsWith(".net") || input.endsWith(".edu") ||
                        input.endsWith(".gov") || input.endsWith(".io") ||
                        input.endsWith(".co") || input.endsWith(".xyz") ||
                        input.endsWith(".info") || input.endsWith(".me") ||
                        input.endsWith(".tv") || input.endsWith(".app") ||
                        input.endsWith(".uk") || input.endsWith(".de") ||
                        input.endsWith(".fr") || input.endsWith(".jp") ||
                        input.endsWith(".in") || input.endsWith(".au") ||
                        input.endsWith(".ca") || input.endsWith(".br") ||
                        input.endsWith(".mx") || input.endsWith(".ru") ||
                        input.endsWith(".za") || input.endsWith(".ng") ||
                        input.endsWith(".eg") || input.endsWith(".sa") ||
                        input.endsWith(".ae") || input.endsWith(".pk"));
    }

    /**
     * Format URL with proper scheme
     */
    public static String formatUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        url = url.trim();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    /**
     * Extract domain from URL
     */
    public static String extractDomain(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host != null) {
                // Remove www. prefix
                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }
                return host;
            }
        } catch (Exception e) {
            // Ignore
        }

        return "";
    }

    /**
     * Check if URL is a search engine URL
     */
    public static boolean isSearchEngineUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        String[] searchDomains = {
                "google.com", "duckduckgo.com", "bing.com",
                "yahoo.com", "brave.com", "search.yahoo.com",
                "startpage.com", "qwant.com", "ecosia.org"
        };

        String domain = extractDomain(url);
        if (TextUtils.isEmpty(domain)) {
            return false;
        }

        for (String searchDomain : searchDomains) {
            if (domain.contains(searchDomain)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Open a URL directly
     */
    public void open(String url) {
        if (TextUtils.isEmpty(url)) {
            url = SEManager.getInstance(context).getCurrentEngineHomeUrl();
        }

        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // =============================================================
    // RECEIVE DATA (For SearchActivity)
    // =============================================================

    /**
     * Open a URL with custom flags
     */
    public void open(String url, int flags) {
        if (TextUtils.isEmpty(url)) {
            url = SEManager.getInstance(context).getCurrentEngineHomeUrl();
        }

        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.addFlags(flags);
        context.startActivity(intent);
    }

    // =============================================================
    // UTILITY METHODS
    // =============================================================

    /**
     * Open a search query
     */
    public void search(String query) {
        if (TextUtils.isEmpty(query)) {
            open((String) null);
            return;
        }

        String searchUrl = SEManager.getInstance(context).buildSearchUrl(query);

        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(KEY_QUERY, query);
        intent.putExtra(KEY_SEARCH_URL, searchUrl);
        intent.putExtra(KEY_URL, searchUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Open incognito/private mode
     */
    public void openIncognito() {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(KEY_INCOGNITO, true);
        intent.putExtra(KEY_URL, SEManager.getInstance(context).getCurrentEngineHomeUrl());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Open incognito with URL
     */
    public void openIncognito(String url) {
        if (TextUtils.isEmpty(url)) {
            url = SEManager.getInstance(context).getCurrentEngineHomeUrl();
        }

        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(KEY_INCOGNITO, true);
        intent.putExtra(KEY_URL, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Open from external intent (ACTION_VIEW)
     */
    public void open(Intent externalIntent) {
        if (externalIntent == null) {
            open((String) null);
            return;
        }

        String url = null;
        String query = null;
        String searchUrl = null;

        // 1. Check for ACTION_VIEW with data
        if (Intent.ACTION_VIEW.equals(externalIntent.getAction())
                && externalIntent.getData() != null) {
            url = externalIntent.getData().toString();
        }

        // 2. Check for router URL
        if (TextUtils.isEmpty(url)) {
            url = externalIntent.getStringExtra(KEY_URL);
        }

        // 3. Check for search
        if (TextUtils.isEmpty(url)) {
            query = externalIntent.getStringExtra(KEY_QUERY);
            searchUrl = externalIntent.getStringExtra(KEY_SEARCH_URL);

            if (!TextUtils.isEmpty(query) && TextUtils.isEmpty(searchUrl)) {
                searchUrl = SEManager.getInstance(context).buildSearchUrl(query);
            }

            if (!TextUtils.isEmpty(searchUrl)) {
                url = searchUrl;
            }
        }

        // 4. Check incognito
        boolean incognito = externalIntent.getBooleanExtra(KEY_INCOGNITO, false);

        // 5. Fallback to home
        if (TextUtils.isEmpty(url)) {
            url = SEManager.getInstance(context).getCurrentEngineHomeUrl();
        }

        // Build intent
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(KEY_URL, url);

        if (!TextUtils.isEmpty(query)) {
            intent.putExtra(KEY_QUERY, query);
        }

        if (incognito) {
            intent.putExtra(KEY_INCOGNITO, true);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // =============================================================
    // DATA MODEL
    // =============================================================

    public static class RouterData {
        public static final int TYPE_URL = 1;
        public static final int TYPE_SEARCH = 2;
        public static final int TYPE_HOME = 3;

        public int type = TYPE_HOME;
        public String url;
        public String query;
        public boolean incognito = false;

        public boolean isUrl() {
            return type == TYPE_URL;
        }

        public boolean isSearch() {
            return type == TYPE_SEARCH;
        }

        public boolean isHome() {
            return type == TYPE_HOME;
        }

        public boolean hasData() {
            return type != -1 && !TextUtils.isEmpty(url);
        }

        public boolean isIncognito() {
            return incognito;
        }

        @Override
        public String toString() {
            return "RouterData{" +
                    "type=" + type +
                    ", url='" + url + '\'' +
                    ", query='" + query + '\'' +
                    ", incognito=" + incognito +
                    '}';
        }
    }
}