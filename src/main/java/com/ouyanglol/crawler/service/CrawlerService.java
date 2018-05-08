package com.ouyanglol.crawler.service;

import com.alibaba.fastjson.JSONObject;
import com.ouyanglol.crawler.mapper.CrawlerArticleMapper;
import com.ouyanglol.crawler.model.CrawlerArticle;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Package: com.ouyanglol.crawler.service
 *
 * @Author: Ouyang
 * @Date: 2018/3/23
 */
@Service
public class CrawlerService {
    @Autowired
    CrawlerArticleMapper crawlerArticleMapper;

    private Logger logger = LoggerFactory.getLogger(CrawlerService.class.getName());

    public void toutiaoCrawler(String url){
        try {
            String html = pickData(url);
            Pattern pattern = Pattern.compile("articleInfo: (\\u007B[\\s\\S]*}),\\n\\s*commentInfo");
            Matcher matcher = pattern.matcher(html);

            while (matcher.find()) {
                String articleStr = matcher.group(1);
                JSONObject article = JSONObject.parseObject(articleStr);
                //农历时间
                String time = article.getJSONObject("subInfo").getString("time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date publishDate = new Date();
                try {
                    publishDate = sdf.parse(time);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                String author = article.getJSONObject("subInfo").getString("source");
                String title = article.getString("title");
                String content = article.getString("content");
                CrawlerArticle crawlerArticle = new CrawlerArticle();
                crawlerArticle.setAuthor(author);
                crawlerArticle.setContent(content);
                crawlerArticle.setTitle(title);
                crawlerArticle.setPublishDate(publishDate);
                crawlerArticle.setSourceUrl(url);
                crawlerArticle.setCreateDate(new Date());
                crawlerArticleMapper.insert(crawlerArticle);
            }
        } catch (Exception e) {
            logger.error("crawler ERROR---->"+url);
            e.printStackTrace();
        }
    }

    /*
     * 爬取网页信息
     */
    public static String pickData(String url) {
        HashSet<BasicHeader> headers = new HashSet<>();
        headers.add(new BasicHeader("Host","www.toutiao.com"));
        headers.add(new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
        headers.add(new BasicHeader("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8"));
        headers.add(new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36"));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultHeaders(headers).build();
        try {
            HttpGet httpget = new HttpGet(url);
            try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                // 打印响应状态
                if (entity != null) {
                    return EntityUtils.toString(entity,"utf-8");
                }
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public Integer getCount(String keyWord) {
        return crawlerArticleMapper.getCount(keyWord);
    }

    public List<CrawlerArticle> getArticleList(int pageNo, int pageSize, String keyWord) {
        Integer start = (pageNo-1)*pageSize;
        return crawlerArticleMapper.getList(start,pageSize,keyWord);
    }
}
