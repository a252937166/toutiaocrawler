CREATE TABLE crawler_article
(
  id           INT AUTO_INCREMENT
    PRIMARY KEY,
  title        VARCHAR(100) NULL,
  content      TEXT         NULL,
  create_date  DATETIME     NULL,
  source_url   VARCHAR(200) NULL
  COMMENT '原文地址',
  publish_date DATETIME     NULL
  COMMENT '发布时间',
  author       VARCHAR(50)  NULL
  COMMENT '作者'
)
  COMMENT '爬取文章'
  ENGINE = InnoDB;