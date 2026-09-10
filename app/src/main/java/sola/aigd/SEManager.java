package sola.aigd;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class SEManager {
    private static final String PREF_NAME = "cipher_prefs";
    private static final String KEY_SEARCH_ENGINE = "search_engine";
    private static final String KEY_SHOW_ADULT = "show_adult";

    private static SEManager instance;
    private final Context context;
    private final List<Engine> allEngines = new ArrayList<>();
    private int currentIndex;

    private SEManager(Context context) {
        this.context = context.getApplicationContext();
        initializeEngines();
        loadSavedEngine();
    }

    public static synchronized SEManager getInstance(Context context) {
        if (instance == null) instance = new SEManager(context);
        return instance;
    }

    private void add(String name, String search, String home, String cat, boolean adult) {
        allEngines.add(new Engine(name, search, home, cat, adult));
    }

    private void initializeEngines() {
        allEngines.clear();

        // ===== GENERAL =====
        add("Google", "https://www.google.com/search?q=", "https://www.google.com", "general", false);
        add("Bing", "https://www.bing.com/search?q=", "https://www.bing.com", "general", false);
        add("Yahoo", "https://search.yahoo.com/search?p=", "https://www.yahoo.com", "general", false);
        add("Yandex", "https://yandex.com/search/?text=", "https://yandex.com", "general", false);
        add("Baidu", "https://www.baidu.com/s?wd=", "https://www.baidu.com", "general", false);
        add("AOL", "https://search.aol.com/aol/search?q=", "https://search.aol.com", "general", false);
        add("Ask", "https://www.ask.com/web?q=", "https://www.ask.com", "general", false);
        add("Ecosia", "https://www.ecosia.org/search?q=", "https://www.ecosia.org", "general", false);
        add("Lycos", "https://search.lycos.com/web?q=", "https://www.lycos.com", "general", false);
        add("Dogpile", "https://www.dogpile.com/search/web?q=", "https://www.dogpile.com", "general", false);
        add("WebCrawler", "https://www.webcrawler.com/search/web?q=", "https://www.webcrawler.com", "general", false);
        add("Excite", "https://www.excite.com/search?q=", "https://www.excite.com", "general", false);

        // ===== PRIVACY =====
        add("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com", "privacy", false);
        add("Brave", "https://search.brave.com/search?q=", "https://search.brave.com", "privacy", false);
        add("Startpage", "https://www.startpage.com/sp/search?query=", "https://www.startpage.com", "privacy", false);
        add("Qwant", "https://www.qwant.com/?q=", "https://www.qwant.com", "privacy", false);
        add("Mojeek", "https://www.mojeek.com/search?q=", "https://www.mojeek.com", "privacy", false);
        add("Swisscows", "https://swisscows.com/web?query=", "https://swisscows.com", "privacy", false);
        add("MetaGer", "https://metager.org/meta/meta.ger3?eingabe=", "https://metager.org", "privacy", false);
        add("Gibiru", "https://gibiru.com/results.html?q=", "https://gibiru.com", "privacy", false);

        // ===== AI & NEW =====
        add("Perplexity", "https://www.perplexity.ai/search?q=", "https://www.perplexity.ai", "ai", false);
        add("You.com", "https://you.com/search?q=", "https://you.com", "ai", false);
        add("Andi", "https://andisearch.com/search?q=", "https://andisearch.com", "ai", false);
        add("Phind", "https://www.phind.com/search?q=", "https://www.phind.com", "ai", false);
        add("Kagi", "https://kagi.com/search?q=", "https://kagi.com", "ai", false);
        add("Neeva", "https://neeva.com/search?q=", "https://neeva.com", "ai", false);
        add("Arc Search", "https://arc.net/search?q=", "https://arc.net", "ai", false);

        // ===== TECH / SPECIAL =====
        add("Wikipedia", "https://en.wikipedia.org/wiki/Special:Search?search=", "https://wikipedia.org", "tech", false);
        add("GitHub", "https://github.com/search?q=", "https://github.com", "tech", false);
        add("StackOverflow", "https://stackoverflow.com/search?q=", "https://stackoverflow.com", "tech", false);
        add("YouTube", "https://www.youtube.com/results?search_query=", "https://youtube.com", "tech", false);
        add("Reddit", "https://www.reddit.com/search/?q=", "https://www.reddit.com", "tech", false);
        add("Wolfram", "https://www.wolframalpha.com/input?i=", "https://www.wolframalpha.com", "tech", false);
        add("Archive.org", "https://archive.org/search?query=", "https://archive.org", "tech", false);

        // ===== ADULT - 30+ =====
        add("Pornhub", "https://www.pornhub.com/video/search?search=", "https://www.pornhub.com", "adult", true);
        add("XVideos", "https://www.xvideos.com/?k=", "https://www.xvideos.com", "adult", true);
        add("XNXX", "https://www.xnxx.com/search/", "https://www.xnxx.com", "adult", true);
        add("YouPorn", "https://www.youporn.com/search/?query=", "https://www.youporn.com", "adult", true);
        add("RedTube", "https://www.redtube.com/results?search=", "https://www.redtube.com", "adult", true);
        add("xHamster", "https://xhamster.com/search/", "https://xhamster.com", "adult", true);
        add("SpankBang", "https://spankbang.com/s/", "https://spankbang.com", "adult", true);
        add("Eporner", "https://www.eporner.com/search/", "https://www.eporner.com", "adult", true);
        add("HQPorner", "https://hqporner.com/search/", "https://hqporner.com", "adult", true);
        add("Porn00", "https://porn00.org/search/", "https://porn00.org", "adult", true);
        add("Tube8", "https://www.tube8.com/search.php?q=", "https://www.tube8.com", "adult", true);
        add("PornKai", "https://www.pornkai.com/search/", "https://www.pornkai.com", "adult", true);
        add("PornTastic", "https://porntastic.com/search/", "https://porntastic.com", "adult", true);
        add("PornTrex", "https://www.porntrex.com/search/", "https://www.porntrex.com", "adult", true);
        add("Sex.com", "https://www.sex.com/search/videos/", "https://www.sex.com", "adult", true);
        add("OnlyFans Search", "https://onlyfans.com/search?q=", "https://onlyfans.com", "adult", true);
        add("Fansly Search", "https://fansly.com/search?q=", "https://fansly.com", "adult", true);
        add("ManyVids", "https://www.manyvids.com/search?q=", "https://www.manyvids.com", "adult", true);
        add("ThotHub", "https://thothub.tv/search/", "https://thothub.tv", "adult", true);
        add("PornGalaxy", "https://www.porngalaxy.com/search/", "https://www.porngalaxy.com", "adult", true);
        add("ThePornDude", "https://theporndude.com/search?q=", "https://theporndude.com", "adult", true);
        add("SpankWire", "https://www.spankwire.com/search/", "https://www.spankwire.com", "adult", true);
        add("DrTuber", "https://www.drtuber.com/search/videos/", "https://www.drtuber.com", "adult", true);
        add("NudeVista", "https://www.nudevista.com/search?q=", "https://www.nudevista.com", "adult", true);
        add("PornWhite", "https://pornwhite.com/search/", "https://pornwhite.com", "adult", true);
        add("SunPorno", "https://www.sunporno.com/search/", "https://www.sunporno.com", "adult", true);
        add("PornBadoo", "https://www.pornbadoo.com/search/", "https://www.pornbadoo.com", "adult", true);
    }

    // ===== OLD API - FOR YOUR EXISTING CODE =====
    public String[] getEngineNames() {
        List<Engine> list = getFilteredEngines();
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i).name;
        return arr;
    }

    public String[] getEngineUrls() {
        List<Engine> list = getFilteredEngines();
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i).searchUrl;
        return arr;
    }

    public String getCurrentEngineName() {
        return getFilteredEngines().get(currentIndex).name;
    }

    public String getCurrentEngineUrl() {
        return getFilteredEngines().get(currentIndex).searchUrl;
    }

    public String getCurrentEngineHomeUrl() {
        return getFilteredEngines().get(currentIndex).homeUrl;
    }

    public int getCurrentEngineIndex() {
        return currentIndex;
    }

    public void setCurrentEngineIndex(int index) {
        List<Engine> list = getFilteredEngines();
        if (index >= 0 && index < list.size()) {
            currentIndex = index;
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putInt(KEY_SEARCH_ENGINE, index).apply();
        }
    }

    public String buildSearchUrl(String query) {
        return getFilteredEngines().get(currentIndex).searchUrl + Uri.encode(query);
    }

    public String getEngineHomeUrl(int index) {
        return getFilteredEngines().get(index).homeUrl;
    }

    public String getEngineUrl(int index) {
        return getFilteredEngines().get(index).searchUrl;
    }

    public String getEngineName(int index) {
        return getFilteredEngines().get(index).name;
    }

    // ===== NEW API - MORE POWERFUL =====
    public List<Engine> getAllEngines() {
        return allEngines;
    }

    public List<Engine> getFilteredEngines() {
        boolean showAdult = isAdultEnabled();
        if (showAdult) return allEngines;
        List<Engine> filtered = new ArrayList<>();
        for (Engine e : allEngines) if (!e.isAdult) filtered.add(e);
        return filtered;
    }

    public List<Engine> getEnginesByCategory(String category) {
        List<Engine> out = new ArrayList<>();
        for (Engine e : getFilteredEngines()) if (e.category.equalsIgnoreCase(category)) out.add(e);
        return out;
    }

    public Engine getEngine(int index) {
        return getFilteredEngines().get(index);
    }

    public boolean isAdultEnabled() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SHOW_ADULT, true); // true by default
    }

    public void setAdultEnabled(boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SHOW_ADULT, enabled).apply();
        if (!enabled && getFilteredEngines().get(currentIndex).isAdult) {
            currentIndex = 0;
            prefs.edit().putInt(KEY_SEARCH_ENGINE, 0).apply();
        }
    }

    private void loadSavedEngine() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentIndex = prefs.getInt(KEY_SEARCH_ENGINE, 0);
        if (currentIndex >= getFilteredEngines().size()) currentIndex = 0;
    }

    public static class Engine {
        public String name;
        public String searchUrl;
        public String homeUrl;
        public String category; // general, privacy, ai, adult, tech
        public boolean isAdult;

        public Engine(String name, String searchUrl, String homeUrl, String category, boolean isAdult) {
            this.name = name;
            this.searchUrl = searchUrl;
            this.homeUrl = homeUrl;
            this.category = category;
            this.isAdult = isAdult;
        }
    }
}