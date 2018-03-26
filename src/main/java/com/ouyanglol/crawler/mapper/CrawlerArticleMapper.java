package com.ouyanglol.crawler.mapper;


import com.ouyanglol.crawler.model.CrawlerArticle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CrawlerArticleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CrawlerArticle record);

    int insertSelective(CrawlerArticle record);

    CrawlerArticle selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CrawlerArticle record);

    int updateByPrimaryKeyWithBLOBs(CrawlerArticle record);

    int updateByPrimaryKey(CrawlerArticle record);

    Integer getCount(@Param(value = "keyWord") String keyWord);

    List<CrawlerArticle> getList(@Param("start") int start, @Param("pageSize") int pageSize, @Param("keyWord") String keyWord);
}