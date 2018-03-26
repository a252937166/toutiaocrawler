package com.ouyanglol.crawler.service;

import com.ouyanglol.crawler.mapper.CrawlerArticleMapper;
import com.ouyanglol.crawler.model.CrawlerArticle;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import java.util.List;
import java.util.Objects;

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
            Document document = Jsoup.parse(Objects.requireNonNull(html));
            //农历时间
            String time = document.getElementsByTag("time").first().text();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date publishDate = new Date();
            try {
                publishDate = sdf.parse(time);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            String author = document.getElementsByClass("subtitle").text().split(" ")[0];
            String title = document.getElementsByTag("header").first().getElementsByTag("h1").text();
            String content = document.getElementsByTag("article").text();
            CrawlerArticle article = new CrawlerArticle();
            article.setAuthor(author);
            article.setContent(content);
            article.setTitle(title);
            article.setPublishDate(publishDate);
            article.setSourceUrl(url);
            article.setCreateDate(new Date());
            crawlerArticleMapper.insert(article);
        } catch (Exception e) {
            logger.error("crawler ERROR---->"+url);
            e.printStackTrace();
        }
    }

    /*
     * 爬取网页信息
     */
    public static String pickData(String url) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
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
