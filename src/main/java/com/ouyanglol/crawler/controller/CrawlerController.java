package com.ouyanglol.crawler.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ouyanglol.crawler.model.CrawlerArticle;
import com.ouyanglol.crawler.service.CrawlerService;
import com.ouyanglol.crawler.util.Http;
import com.ouyanglol.crawler.util.ThreadUtil;
import com.ouyanglol.crawler.util.ToutiaoUtil;
import com.ouyanglol.crawler.web.HttpResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Package: com.fishsaying.ce.controller
 *
 * @Author: Ouyang
 * @Date: 2018/3/12
 */
@Api(value = "爬虫管理",tags = "爬虫管理")
@RestController
@RequestMapping("ce/crawler")
public class CrawlerController {
    private BlockingQueue<String> urlQueue = new LinkedBlockingDeque<>();
    private volatile boolean start = false;
    private Logger logger = LoggerFactory.getLogger(CrawlerController.class.getName());
    @Autowired
    CrawlerService crawlerService;

    @ApiOperation("新增今日头条爬虫任务")
    @PostMapping("/toutiao/add")
    private HttpResult add(@RequestParam @ApiParam(value = "uid",required = true) String uid, @RequestParam(defaultValue = "") @ApiParam(value = "max_behot_time") String maxBehotTime) {
        HttpResult httpResult = new HttpResult();
        try {
            ThreadUtil.getLongTimeOutThread(() -> {
                String id = new String(uid);
                Map<String,String> ascp = ToutiaoUtil.getAsCp();
                String as = ascp.get("as");
                String cp = ascp.get("cp");
                String url = "https://www.toutiao.com/pgc/ma/?page_type=1&max_behot_time="+maxBehotTime+"&uid="+id+"&media_id="+id+"&output=json&is_json=1&count=20&from=user_profile_app&version=2&as="+as+"&cp="+cp;
                Http http = new Http(url);
                Http.HttpResult result = http.doGet();
                JSONObject ret = result.toJsonObject();
                String mediaId = ret.getString("media_id");
                //判断uid是否有效
                if (mediaId!=null&&mediaId.equals(uid)) {
                    JSONArray articles = ret.getJSONArray("data");
                    for (int i = 0; i < articles.size();i++) {
                        String seoUrl = articles.getJSONObject(i).getString("article_url");
                        urlQueue.offer(seoUrl);
                    }
                }
                if (ret.getInteger("has_more")==1) {
                    String newMaxBehotTime = ret.getJSONObject("next").getString("max_behot_time");
                    add(id,newMaxBehotTime);
                }
            });
        } catch (Exception e) {
            logger.error("/toutiao/add-->{}",e.getMessage());
            httpResult.setSuccess(false);
            httpResult.setMsg(e.getMessage());
            return httpResult;
        }
        return httpResult;
    }

    @ApiOperation("开始爬虫任务")
    @PostMapping("/toutiao/start")
    private HttpResult<Boolean> start() {
        HttpResult<Boolean> httpResult = new HttpResult<>();
        //判断当前是否已经是爬取状态
        if (!start) {
            start = true;
            //判断当前有无任务
            if (!urlQueue.isEmpty()) {
                try {
                    ThreadUtil.getLongTimeOutThread(() -> {
                        while (start) {
                            {
                                String url = urlQueue.poll();
                                crawlerService.toutiaoCrawler(url);
                                //判断还有无任务
                                if (urlQueue.isEmpty()) {
                                    start = false;
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                start = false;
                httpResult.setSuccess(false);
                httpResult.setMsg("无任务可执行！");
            }
        } else {
            httpResult.setSuccess(false);
            httpResult.setMsg("当前任务进行中！");
        }
        return httpResult;
    }

    @ApiOperation("停止爬虫")
    @PostMapping("/toutiao/stop")
    private HttpResult stop() {
        logger.info("停止");
        HttpResult httpResult = new HttpResult();
        start = false;
        return httpResult;
    }

    @ApiOperation("爬虫状态")
    @GetMapping("/toutiao/status")
    private HttpResult<Boolean> status() {
        HttpResult<Boolean> httpResult = new HttpResult<>();
        httpResult.setResult(start);
        return httpResult;
    }

    @ApiOperation("爬虫文章列表")
    @GetMapping("/toutiao/articleList")
    private HttpResult<Object> getArticleList(@RequestParam @ApiParam(required = true) int pageSize, @RequestParam @ApiParam(required = true) int pageNo, @RequestParam(defaultValue = "") @ApiParam() String keyWord) {
        HttpResult<Object> httpResult = new HttpResult<>();
        Integer count = crawlerService.getCount(keyWord);
        List<CrawlerArticle> articleList = crawlerService.getArticleList(pageNo,pageSize,keyWord);
        Map<String,Object> map = new HashMap<>();
        map.put("count",count);
        map.put("list",articleList);
        httpResult.setResult(map);
        return httpResult;
    }


}
