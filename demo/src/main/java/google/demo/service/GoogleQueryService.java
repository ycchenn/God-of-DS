package google.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

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

    public HashMap<String, String> search(String searchKeyword) throws IOException {
        this.searchKeyword = searchKeyword;
        try {
            String encodeKeyword = URLEncoder.encode(searchKeyword, "utf-8");
            this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=20";
            // 如果不需要特殊處理中文，可以使用以下方式
            // this.url = "https://www.google.com/search?q=" + searchKeyword + "&oe=utf8&num=20";
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (content == null) {
            content = fetchContent();
        }

        HashMap<String, String> retVal = new HashMap<>();

        // 使用 Jsoup 分析 HTML 字符串
        Document doc = Jsoup.parse(content);

        // 選擇特定元素
        Elements lis = doc.select("div.kCrYT");

        for (Element li : lis) {
            try {
                String citeUrl = li.select("a").attr("href").replace("/url?q=", "").split("&")[0];
                String title = li.select("a").select(".vvjwJb").text();

                if (title.equals("")) {
                    continue;
                }

                System.out.println("Title: " + title + " , url: " + citeUrl);

                // 將標題和 URL 放入 HashMap
                retVal.put(title, citeUrl);

            } catch (IndexOutOfBoundsException e) {
                // 忽略例外
            }
        }

        return retVal;
    }

    private String fetchContent() throws IOException {
        StringBuilder retVal = new StringBuilder();

        URL u = new URL(url);
        URLConnection conn = u.openConnection();
        // 設置 HTTP 標頭
        conn.setRequestProperty("User-agent", "Chrome/107.0.5304.107");
        InputStream in = conn.getInputStream();

        InputStreamReader inReader = new InputStreamReader(in, "utf-8");
        BufferedReader bufReader = new BufferedReader(inReader);
        String line;

        while ((line = bufReader.readLine()) != null) {
            retVal.append(line);
        }
        bufReader.close();
        return retVal.toString();
    }
}
