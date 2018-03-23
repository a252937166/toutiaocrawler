package com.ouyanglol.crawler.mapper;


import com.ouyanglol.crawler.model.CrawlerArticle;

public interface CrawlerArticleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CrawlerArticle record);

    int insertSelective(CrawlerArticle record);

    CrawlerArticle selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CrawlerArticle record);

    int updateByPrimaryKeyWithBLOBs(CrawlerArticle record);

    int updateByPrimaryKey(CrawlerArticle record);
}