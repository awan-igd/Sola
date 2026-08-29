package sola.aigd;

import android.content.Context;
import android.content.SharedPreferences;

public class SEManager {
    private static final String PREF_NAME = "cipher_prefs";
    private static final String KEY_SEARCH_ENGINE = "search_engine";

    private static SEManager instance;
    private final Context context;
    private String[] engineNames;
    private String[] engineUrls;
    private String[] engineHomeUrls;
    private int currentIndex;

    private SEManager(Context context) {
        this.context = context.getApplicationContext();
        initializeEngines();
        loadSavedEngine();
    }

    public static synchronized SEManager getInstance(Context context) {
        if (instance == null) {
            instance = new SEManager(context);
        }
        return instance;
    }

    private void initializeEngines() {
        engineNames = new String[]{
                "Google", "Bing", "DuckDuckGo", "Yahoo", "Brave", "Ecosia",
                "Startpage", "Qwant", "Yandex", "Baidu", "Perplexity", "You.com",
                "Kagi", "Mojeek", "Swisscows", "MetaGer", "Neeva", "Gibiru",
                "Dogpile", "AOL", "Ask", "Lycos", "WebCrawler", "Excite",

                // === 18+ Adult Search Engines ===
                "Pornhub", "XVideos", "XNXX", "YouPorn", "RedTube", "xHamster",
                "PornTrex", "SpankBang", "Eporner", "PornDude", "PornGalaxy",
                "Sex.com", "Tube8", "Porn00", "AdultSearch", "PornSearchEngine",
                "LustySearch", "AdultWeb", "Bing NSFW", "Google NSFW Mode",
                "DuckDuckGo NSFW", "ThotHub", "OnlyFans Search", "Fansly Search",
                "ManyVids", "PornWhite", "HQPorner", "PornKai", "PornTastic"
        };

        engineUrls = new String[]{
                "https://www.google.com/search?q=",
                "https://www.bing.com/search?q=",
                "https://duckduckgo.com/?q=",
                "https://search.yahoo.com/search?p=",
                "https://search.brave.com/search?q=",
                "https://www.ecosia.org/search?q=",
                "https://www.startpage.com/sp/search?query=",
                "https://www.qwant.com/?q=",
                "https://yandex.com/search/?text=",
                "https://www.baidu.com/s?wd=",
                "https://www.perplexity.ai/search?q=",
                "https://you.com/search?q=",
                "https://kagi.com/search?q=",
                "https://www.mojeek.com/search?q=",
                "https://swisscows.com/web?query=",
                "https://metager.org/meta/meta.ger3?eingabe=",
                "https://neeva.com/search?q=",
                "https://gibiru.com/results.html?q=",
                "https://www.dogpile.com/search/web?q=",
                "https://search.aol.com/aol/search?q=",
                "https://www.ask.com/web?q=",
                "https://search.lycos.com/web?q=",
                "https://www.webcrawler.com/search/web?q=",
                "https://www.excite.com/search?q=",

                // === 18+ Adult Search URLs ===
                "https://www.pornhub.com/video/search?search=",
                "https://www.xvideos.com/?k=",
                "https://www.xnxx.com/search/",
                "https://www.youporn.com/search/?query=",
                "https://www.redtube.com/results?search=",
                "https://xhamster.com/search/",
                "https://www.porntrex.com/search/",
                "https://spankbang.com/s/",
                "https://www.eporner.com/search/",
                "https://theporndude.com/search?q=",
                "https://www.porngalaxy.com/search/",
                "https://www.sex.com/search/videos/",
                "https://www.tube8.com/search.php?q=",
                "https://porn00.org/search/",
                "https://adultsearch.com/",
                "https://www.pornsearchengine.com/search?q=",
                "https://lustysearch.com/search?q=",
                "https://www.adultweb.com/search/",
                "https://www.bing.com/search?q=",
                "https://www.google.com/search?q=",
                "https://duckduckgo.com/?q=",
                "https://thothub.tv/search/",
                "https://onlyfans.com/search?q=",
                "https://fansly.com/search?q=",
                "https://www.manyvids.com/search?q=",
                "https://pornwhite.com/search/",
                "https://hqporner.com/search/",
                "https://www.pornkai.com/search/",
                "https://porntastic.com/search/"
        };

        engineHomeUrls = new String[]{
                "https://www.google.com",
                "https://www.bing.com",
                "https://duckduckgo.com",
                "https://www.yahoo.com",
                "https://search.brave.com",
                "https://www.ecosia.org",
                "https://www.startpage.com",
                "https://www.qwant.com",
                "https://yandex.com",
                "https://www.baidu.com",
                "https://www.perplexity.ai",
                "https://you.com",
                "https://kagi.com",
                "https://www.mojeek.com",
                "https://swisscows.com",
                "https://metager.org",
                "https://neeva.com",
                "https://gibiru.com",
                "https://www.dogpile.com",
                "https://search.aol.com",
                "https://www.ask.com",
                "https://search.lycos.com",
                "https://www.webcrawler.com",
                "https://www.excite.com",

                // === 18+ Adult Home URLs ===
                "https://www.pornhub.com",
                "https://www.xvideos.com",
                "https://www.xnxx.com",
                "https://www.youporn.com",
                "https://www.redtube.com",
                "https://xhamster.com",
                "https://www.porntrex.com",
                "https://spankbang.com",
                "https://www.eporner.com",
                "https://theporndude.com",
                "https://www.porngalaxy.com",
                "https://www.sex.com",
                "https://www.tube8.com",
                "https://porn00.org",
                "https://adultsearch.com",
                "https://www.pornsearchengine.com",
                "https://lustysearch.com",
                "https://www.adultweb.com",
                "https://www.bing.com",
                "https://www.google.com",
                "https://duckduckgo.com",
                "https://thothub.tv",
                "https://onlyfans.com",
                "https://fansly.com",
                "https://www.manyvids.com",
                "https://pornwhite.com",
                "https://hqporner.com",
                "https://www.pornkai.com",
                "https://porntastic.com"
        };
    }

    private void loadSavedEngine() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentIndex = prefs.getInt(KEY_SEARCH_ENGINE, 0);
    }

    public String[] getEngineNames() {
        return engineNames;
    }

    public String[] getEngineUrls() {
        return engineUrls;
    }

    public String getCurrentEngineName() {
        return engineNames[currentIndex];
    }

    public String getCurrentEngineUrl() {
        return engineUrls[currentIndex];
    }

    public String getCurrentEngineHomeUrl() {
        return engineHomeUrls[currentIndex];
    }

    public int getCurrentEngineIndex() {
        return currentIndex;
    }

    public void setCurrentEngineIndex(int index) {
        if (index >= 0 && index < engineNames.length) {
            currentIndex = index;
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putInt(KEY_SEARCH_ENGINE, index).apply();
        }
    }

    public String buildSearchUrl(String query) {
        return engineUrls[currentIndex] + android.net.Uri.encode(query);
    }

    public String getEngineHomeUrl(int index) {
        if (index >= 0 && index < engineHomeUrls.length) {
            return engineHomeUrls[index];
        }
        return engineHomeUrls[0];
    }

    public String getEngineUrl(int index) {
        if (index >= 0 && index < engineUrls.length) {
            return engineUrls[index];
        }
        return engineUrls[0];
    }

    public String getEngineName(int index) {
        if (index >= 0 && index < engineNames.length) {
            return engineNames[index];
        }
        return engineNames[0];
    }
}