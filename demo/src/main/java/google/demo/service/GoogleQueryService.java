package google.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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

    public GoogleQueryService() {
        // 空的建構函式
    }

    private String fetchPageContent(String url) {
        try {
            // 使用 Jsoup 抓取網頁
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get();

            // 提取主要內容
            System.out.println("[DEBUG] Fetched URL: " + url);
            System.out.println("[DEBUG] Page Title: " + doc.title());
            return doc.body().text(); // 抓取網頁正文文字
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to fetch content for URL: " + url);
            return "";
        }
    }

    public LinkedHashMap<String, Object> search(String searchKeyword) throws IOException {
        this.searchKeyword = searchKeyword;
        try {
            String encodeKeyword = URLEncoder.encode(searchKeyword, "utf-8");
            this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=20";
            System.out.println("[DEBUG] Generated URL: " + url);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to encode keyword: " + e.getMessage());
        }

        content = fetchContent();

        // 定義關鍵字權重
        HashMap<String, Integer> weights = new HashMap<>();
        weights.put("白飯", 5);
        weights.put("性價比", 3);
        weights.put("不限時", 2);

        // 保存結果與分數
        List<SearchResult> scoredResults = new ArrayList<>();
        Document doc = Jsoup.parse(content);
        Elements lis = doc.select("div.g");

        for (Element li : lis) {
            try {
                String citeUrl = li.select("a").attr("href").replace("/url?q=", "").split("&")[0];
                String title = li.select("a > h3").text();

                if (title.equals("")) {
                    continue;
                }

                String pageContent = fetchPageContent(citeUrl);

                // 計算分數（包括標題和內容）
                int titleScore = calculateScore(title, weights);
                int contentScore = calculateScore(pageContent, weights);
                int totalScore = titleScore + contentScore;

                scoredResults.add(new SearchResult(title, citeUrl, totalScore));
                System.err.println("[DEBUG] totalScore: " + totalScore);
            } catch (Exception e) {
                System.out.println("[ERROR] Failed to process element: " + e.getMessage());
            }
        }

        // 提取推薦的關鍵字
        List<String> relatedKeywords = extractRelatedKeywords(doc);

        // 排序結果（根據分數降序排列）
        scoredResults.sort((a, b) -> Integer.compare(b.score, a.score));

        // 保存排序後的結果
        LinkedHashMap<String, String> sortedResults = new LinkedHashMap<>();
        for (SearchResult result : scoredResults) {
            sortedResults.put(result.title, result.url);
        }

        System.err.println("[DEBUG] Results:" + sortedResults);

        // 返回結果
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("results", sortedResults);
        response.put("relatedKeywords", relatedKeywords);

        return response;
    }

    private List<String> extractRelatedKeywords(Document doc) {
        List<String> relatedKeywords = new ArrayList<>();
        
        // 嘗試調整選擇器來匹配推薦關鍵字
        Elements relatedElements = doc.select("div.mtv5bd"); // Google 搜索推薦關鍵字的 CSS 類
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
            System.out.println("[ERROR] Failed to fetch content: " + e.getMessage());
        }

        return retVal.toString();
    }

    private int calculateScore(String content, HashMap<String, Integer> weights) {
        int score = 0;
        for (String keyword : weights.keySet()) {
            if (content.contains(keyword)) {
                score += weights.get(keyword);
            }
        }
        return score;
    }

    // SearchResult 內部類別
    public static class SearchResult {
        private String title;
        private String url;
        private int score;

        // 構造函數
        public SearchResult(String title, String url, int score) {
            this.title = title;
            this.url = url;
            this.score = score;
        }

        // Getter 和 Setter
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
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
