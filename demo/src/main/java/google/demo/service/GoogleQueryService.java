package google.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class GoogleQueryService {
    private String searchKeyword;
    private String url;
    private String content;

    // 關鍵字權重（可加速查找）
    private static final Map<String, Integer> WEIGHTS = new HashMap<>();

    static {
        WEIGHTS.put("白飯", 2);
        WEIGHTS.put("性價比", 3);
        WEIGHTS.put("不限時", 2);
        WEIGHTS.put("平價", 4);
        WEIGHTS.put("學生", 3);
        WEIGHTS.put("美食", 5);
        WEIGHTS.put("聚餐", 4);
        WEIGHTS.put("宵夜", 4);
    }

    public GoogleQueryService() {
        // 空的建構函式
    }

    private String fetchPageContent(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .timeout(5000) // 設置超時
                    .get();
            System.out.println("[DEBUG] Fetched URL: " + url);
            System.out.println("[DEBUG] Page Title: " + doc.title());
            return doc.body().text();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to fetch content for URL: " + url);
            return "";
        }
    }

    public LinkedHashMap<String, Object> search(String searchKeyword) throws IOException {
        // 自動加上 "美食" 關鍵字
        this.searchKeyword = searchKeyword + " 美食";
    
        // 生成 Google 搜索 URL
        try {
            String encodeKeyword = URLEncoder.encode(this.searchKeyword, "utf-8");
            this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=20";
            System.out.println("[DEBUG] Generated URL: " + url);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to encode keyword: " + e.getMessage());
        }
    
        content = fetchContent();
        if (content == null || content.isEmpty()) {
            System.err.println("[ERROR] Google search returned empty content.");
            return new LinkedHashMap<>();
        }
    
        // 解析搜索結果頁面
        Document doc = Jsoup.parse(content);
        Elements lis = doc.select("div.g");
        System.out.println("[DEBUG] Found search results: " + lis.size());
    
        if (lis.isEmpty()) {
            System.err.println("[ERROR] No search results found.");
            return new LinkedHashMap<>();
        }

        // 並行處理結果並計算分數
        ExecutorService executor = Executors.newFixedThreadPool(70);
        List<Future<SearchResult>> futureResults = new ArrayList<>();

        for (Element li : lis) {
            futureResults.add(executor.submit(() -> processSearchResult(li)));
        }

        // 收集結果
        List<SearchResult> scoredResults = new ArrayList<>();
        for (Future<SearchResult> future : futureResults) {
            try {
                SearchResult result = future.get();
                if (result != null) {
                    scoredResults.add(result);
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to process search result: " + e.getMessage());
            }
        }
        executor.shutdown();

        // 排序結果（使用優化排序算法）
        scoredResults.sort(Comparator.comparingInt(SearchResult::getScore).reversed());

        // 提取推薦的關鍵字
        List<String> relatedKeywords = extractRelatedKeywords(doc);

        // 組裝返回結果
        LinkedHashMap<String, String> sortedResults = new LinkedHashMap<>();
        for (SearchResult result : scoredResults) {
            sortedResults.put(result.title, result.url);
        }

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("results", sortedResults);
        response.put("relatedKeywords", relatedKeywords);

        return response;
    }

    private SearchResult processSearchResult(Element li) {
        try {
            String citeUrl = li.select("a").attr("href").replace("/url?q=", "").split("&")[0];
            String title = li.select("a > h3").text();

            if (title.isEmpty()) {
                return null;
            }

            // 頁面內容（延遲加載避免阻塞主線程）
            String pageContent = fetchPageContent(citeUrl);

            // 使用正規表達式和高效字典計算分數
            int titleScore = calculateScore(title);
            int contentScore = calculateScore(pageContent);
            int totalScore = titleScore + contentScore;

            return new SearchResult(title, citeUrl, totalScore);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to process result: " + e.getMessage());
            return null;
        }
    }

    private List<String> extractRelatedKeywords(Document doc) {
        List<String> relatedKeywords = new ArrayList<>();
        Elements relatedElements = doc.select("div.mtv5bd");
        for (Element element : relatedElements) {
            String keyword = element.text();
            if (!keyword.isEmpty()) {
                relatedKeywords.add(keyword);
            }
        }
        return relatedKeywords;
    }

    private String fetchContent() throws IOException {
        StringBuilder retVal = new StringBuilder();

        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
            InputStream in = conn.getInputStream();

            InputStreamReader inReader = new InputStreamReader(in, "utf-8");
            BufferedReader reader = new BufferedReader(inReader);
            String line;
            while ((line = reader.readLine()) != null) {
                retVal.append(line);
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to fetch content: " + e.getMessage());
        }

        return retVal.toString();
    }

    private int calculateScore(String content) {
        int score = 0;
        for (Map.Entry<String, Integer> entry : WEIGHTS.entrySet()) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(entry.getKey()) + "\\b", Pattern.CASE_INSENSITIVE);
            int matches = (int) pattern.matcher(content).results().count();
            score += matches * entry.getValue();
        }
        return score;
    }

    // SearchResult 內部類別
    public static class SearchResult {
        private String title;
        private String url;
        private int score;

        public SearchResult(String title, String url, int score) {
            this.title = title;
            this.url = url;
            this.score = score;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public int getScore() {
            return score;
        }

        @Override
        public String toString() {
            return "SearchResult{" +
                    "title='" + title + '\'' +
                    ", url='" + url + '\'' +
                    ", score=" + score +
                    '}';
        }
    }
}
