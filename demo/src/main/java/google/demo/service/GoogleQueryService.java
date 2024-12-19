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
        // 空的構造函數
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

    public LinkedHashMap<String, String> search(String searchKeyword) throws IOException {
    this.searchKeyword = searchKeyword;
    try {
        String encodeKeyword = URLEncoder.encode(searchKeyword, "utf-8");
        this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=20";
        System.out.println("[DEBUG] Generated URL: " + url);
    } catch (Exception e) {
        System.out.println("[ERROR] Failed to encode keyword: " + e.getMessage());
    }

    content = fetchContent();

    // 定义关键词权重
    HashMap<String, Integer> weights = new HashMap<>();
    weights.put("白飯", 5);
    weights.put("性價比", 3);
    weights.put("不限時", 2);

    // 保存结果与分数
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

            // 计算分数（包括标题和内容）
            int titleScore = calculateScore(title, weights);
            int contentScore = calculateScore(pageContent, weights);
            int totalScore = titleScore + contentScore;

            scoredResults.add(new SearchResult(title, citeUrl, totalScore));
            System.err.println("[DEBUG] totalScore: " + totalScore);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to process element: " + e.getMessage());
        }
    }

    // 排序结果（根据分数降序排列）
    scoredResults.sort((a, b) -> Integer.compare(b.score, a.score));

    // 返回排序后的结果
    LinkedHashMap<String, String> sortedResults = new LinkedHashMap<>();
    for (SearchResult result : scoredResults) {
        sortedResults.put(result.title, result.url);
    }
    System.err.println("[DEBUG]Results:" + sortedResults);
    return sortedResults;
}

    private String fetchContent() throws IOException {
        StringBuilder retVal = new StringBuilder();

        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
            InputStream in = conn.getInputStream();

            InputStreamReader inReader = new InputStreamReader(in, "utf-8");
            BufferedReader bufReader = new BufferedReader(inReader);
            String line;

            while ((line = bufReader.readLine()) != null) {
                retVal.append(line);
            }
            bufReader.close();
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch content: " + e.getMessage());
        }

        return retVal.toString();
    }

    private int calculateScore(String text, HashMap<String, Integer> weights) {
        int score = 0;
        for (String keyword : weights.keySet()) {
            // 計算每個關鍵字出現的次數
            int occurrences = text.split(keyword, -1).length - 1;
            score += occurrences * weights.get(keyword); // 加上權重
        }
        return score;
    }

    // 搜尋結果類（用於儲存標題、URL 和分數）
    class SearchResult {
        String title;
        String url;
        int score;

        public SearchResult(String title, String url, int score) {
            this.title = title;
            this.url = url;
            this.score = score;
        }
    }
}
