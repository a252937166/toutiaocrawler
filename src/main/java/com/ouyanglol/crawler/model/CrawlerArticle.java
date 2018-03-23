package com.ouyanglol.crawler.model;

import java.util.Date;

public class CrawlerArticle {
    /**   id **/
    private Integer id;

    /**   title **/
    private String title;

    /**   create_date **/
    private Date createDate;

    /** 原文地址  source_url **/
    private String sourceUrl;

    /** 发布时间  publish_date **/
    private Date publishDate;

    /** 作者  author **/
    private String author;

    /**   content **/
    private String content;

    /**     id   **/
    public Integer getId() {
        return id;
    }

    /**     id   **/
    public void setId(Integer id) {
        this.id = id;
    }

    /**     title   **/
    public String getTitle() {
        return title;
    }

    /**     title   **/
    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    /**     create_date   **/
    public Date getCreateDate() {
        return createDate;
    }

    /**     create_date   **/
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**   原文地址  source_url   **/
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**   原文地址  source_url   **/
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl == null ? null : sourceUrl.trim();
    }

    /**   发布时间  publish_date   **/
    public Date getPublishDate() {
        return publishDate;
    }

    /**   发布时间  publish_date   **/
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    /**   作者  author   **/
    public String getAuthor() {
        return author;
    }

    /**   作者  author   **/
    public void setAuthor(String author) {
        this.author = author == null ? null : author.trim();
    }

    /**     content   **/
    public String getContent() {
        return content;
    }

    /**     content   **/
    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }
}