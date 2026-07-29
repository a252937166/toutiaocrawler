# Project Source Code

https://github.com/a252937166/toutiaocrawler.git

# Crawler Goal

Crawl all articles under a specific Toutiao account.

# Crawler Design Thinking

## Crawling Methods

### Dynamic Page Parsing
I have previously mentioned using `webdriver` to crawl web content. The advantage is very obvious: you only need to consider how to parse the `element` tags of the page. However, the downside is also clear: the efficiency is low.

### Interface (API) Parsing
Unless I encounter a website with an impossibly advanced anti-crawler system, I generally do not recommend using `webdriver`. As a technical professional, project performance should always be the top priority. Therefore, for this project, I chose to crack the Toutiao API to retrieve their articles.

## Parsing Logic

### Entry Point
Taking Taihai Net as an example, most large platforms have both PC and H5 versions of their pages.
PC: https://www.toutiao.com/c/user/50502347096/#mid=50502347096

![Here write image description](https://qiniu.ouyanglol.com/blog/java%E7%88%AC%E8%99%AB%E7%B3%BB%E5%88%97%EF%BC%88%E4%BA%94%EF%BC%89%E2%80%94%E2%80%94%E4%BB%8A%E6%97%A5%E5%A4%B4%E6%9D%A1%E6%96%87%E7%AB%A0%E7%88%AC%E8%99%AB%E5%AE%9E%E6%88%981.png-blog)

Figure (1)

H5: http://m.toutiao.com/profile/50502347096/#mid=50502347096 (Switch to the <font color='red'>Articles</font> TAB)

![Here write image description](https://qiniu.ouyanglol.com/blog/java%E7%88%AC%E8%99%AB%E7%B3%BB%E5%88%97%EF%BC%88%E4%BA%94%EF%BC%89%E2%80%94%E2%80%94%E4%BB%8A%E6%97%A5%E5%A4%B4%E6%9D%A1%E6%96%87%E7%AB%A0%E7%88%AC%E8%99%AB%E5%AE%9E%E6%88%982.png-blog)

Figure (2)

From the `Network` information, two article interfaces can be found:
PC:
https://www.toutiao.com/c/user/article/?page_type=1&user_id=50502347096&max_behot_time=0&count=20&as=A1B57ACB48A9D4F&cp=5AB8F92DD4CFBE1&_signature=NVHtvxAab.D7OmttJlHb-zVR7a

H5:
https://www.toutiao.com/pgc/ma/?page_type=1&max_behot_time=&uid=50502347096&media_id=50502347096&output=json&is_json=1&count=20&from=user_profile_app&version=2&as=A125DA2BD89A381&cp=5AB81AE3C8116E1&callback=jsonp3

### Interface Comparison

Clearly, the PC interface has an additional `_signature` parameter compared to the H5 interface. I tried to crack the `_signature` generation method privately and found it to be extremely complex—far beyond my frontend skill level. The method is `window.TAC.sign`; interested students can try it themselves.

![Here write image description](https://qiniu.ouyanglol.com/blog/java%E7%88%AC%E8%99%AB%E7%B3%BB%E5%88%97%EF%BC%88%E4%BA%94%EF%BC%89%E2%80%94%E2%80%94%E4%BB%8A%E6%97%A5%E5%A4%B4%E6%9D%A1%E6%96%87%E7%AB%A0%E7%88%AC%E8%99%AB%E5%AE%9E%E6%88%983.png-blog)

Figure (3)

Out of necessity, I had to choose the H5 interface. Now, I only need to crack the `as` and `cp` parameters.

### Cracking Encrypted Parameters

#### Parameter Generation Method

The first step is to find how the parameters are generated. Unfortunately, there is no shortcut here; I had to copy `as` and `cp` and search for them in every `js` file. It requires some patience.

![Here write image description](https://qiniu.ouyanglol.com/blog/java%E7%88%AC%E8%99%AB%E7%B3%BB%E5%88%97%EF%BC%88%E4%BA%94%EF%BC%89%E2%80%94%E2%80%94%E4%BB%8A%E6%97%A5%E5%A4%B4%E6%9D%A1%E6%96%87%E7%AB%A0%E7%88%AC%E8%99%AB%E5%AE%9E%E6%88%984.png-blog)

Figure (4)

#### JS Analysis

Formatting the method:

```
!function(t) {
    var i = {};
    i.getHoney = function() {
        var t = Math.floor((new Date).getTime() / 1e3)
          , i = t.toString(16).toUpperCase()
          , e = md5(t).toString().toUpperCase();
        if (8 != i.length)
            return {
                as: "479BB4B7254C150",
                cp: "7E0AC8874BB0985"
            };
        for (var s = e.slice(0, 5), o = e.slice(-5), n = "", a = 0; 5 > a; a++)
            n += s[a] + i[a];
        for (var l = "", r = 0; 5 > r; r++)
            l += i[r + 3] + o[r];
        return {
            as: "A1" + n + i.slice(-3),
            cp: i.slice(0, 3) + l + "E1"
        }
    }
    ,
    t.ascp = i
}(window, document)
```

It's not too difficult—just a simple `MD5` encryption method. Converting it to a `java` method is also straightforward:

```
    public static Map<String,String> getAsCp(){
        String as = "479BB4B7254C150";
        String cp = "7E0AC8874BB0985";
        int t = (int) (new Date().getTime()/1000);
        String e = Integer.toHexString(t).toUpperCase();
        String i = DigestUtils.md5DigestAsHex(String.valueOf(t).getBytes()).toUpperCase();
        if (e.length()==8) {
            char[] n = i.substring(0,5).toCharArray();
            char[] a = i.substring(i.length()-5).toCharArray();
            StringBuilder s = new StringBuilder();
            StringBuilder r = new StringBuilder();
            for (int o = 0; o < 5; o++) {
                s.append(n[o]).append(e.substring(o,o+1));
                r.append(e.substring(o+3,o+4)).append(a[o]);
            }
            as = "A1" + s + e.substring(e.length()-3);
            cp = e.substring(0,3) + r + "E1";
        }
        Map<String,String> map = new HashMap<>();
        map.put("as",as);
        map.put("cp",cp);
        return map;
    }
```

### Analyzing Interface Return Values

`media_id`: The ID of the media entity.
`message`: Whether the request was successful.
`next.max_behot_time`: Request parameter for the next page.
`has_more`: Whether there is a next page.
`data.article_url`: The HTML address of the article.

Other parameters are unimportant. The interface does not return the article content directly, so the next step is to crawl the content from the original article URL.

### Parsing the Original Address

It is basically a static page, so the content can be extracted directly from the tags.

# Java Project Implementation

## Basic Functionality

For convenience, I used the Spring Boot framework and designed it as a web project, allowing the crawler to be started or stopped via API calls.

## Queues and Thread Pools

Since the crawler is started via an interface, it's impossible to wait for over 100,000 crawling tasks to finish before returning a success response. Therefore, tasks must be executed asynchronously, requiring a thread pool.

A thread pool alone is not enough; dumping hundreds of thousands of tasks into it is not a good choice. This is where a `java` `Queue` comes in. I chose `LinkedBlockingDeque`, although the double-ended feature wasn't eventually used, so `LinkedBlockingQueue` would work just as well. All tasks to be crawled are first placed in the `Queue`, and addresses are retrieved from there as the crawl progresses. This simply solves high-concurrency issues.

If the task volume is exceptionally large and detailed logging is required, `kafka` could be used instead.

## Operation Interface — Swagger2

Sending requests via `postman` is slightly inconvenient as it requires filling in addresses manually. I thought having a frontend interface would be ideal, but since my frontend skills are limited and I didn't want to spend too much time on it, I integrated `swagger2`.
Access at: http://127.0.0.1:9091/swagger-ui.html#/

![Here write image description](https://qiniu.ouyanglol.com/blog/java%E7%88%AC%E8%99%AB%E7%B3%BB%E5%88%97%EF%BC%88%E4%BA%94%EF%BC%89%E2%80%94%E2%80%94%E4%BB%8A%E6%97%A5%E5%A4%B4%E6%9D%A1%E6%96%87%E7%AB%A0%E7%88%AC%E8%99%AB%E5%AE%9E%E6%88%985.png-blog)
Figure (5)

![Here write image description](https://qiniu.ouyanglol.com/blog/java%E7%88%AC%E8%99%AB%E7%B3%BB%E5%88%97%EF%BC%88%E4%BA%94%EF%BC%89%E2%80%94%E2%80%94%E4%BB%8A%E6%97%A5%E5%A4%B4%E6%9D%A1%E6%96%87%E7%AB%A0%E7%88%AC%E8%99%AB%E5%AE%9E%E6%88%986.png-blog)
Figure (6)

It's intuitive how to use it, so I won't elaborate further. Interested students can build a frontend interface based on these five APIs to complete a Toutiao account crawler.

![Here write image description](https://qiniu.ouyanglol.com/blog/java%E7%88%AC%E8%99%AB%E7%B3%BB%E5%88%97%EF%BC%88%E4%BA%94%EF%BC%89%E2%80%94%E2%80%94%E4%BB%8A%E6%97%A5%E5%A4%B4%E6%9D%A1%E6%96%87%E7%AB%A0%E7%88%AC%E8%99%AB%E5%AE%9E%E6%88%987.png-blog)
Figure (7)

# Summary

The hardest part of crawling is not actually the code, but the analysis of the target page—such as determining the crawling method and breaking through anti-crawler measures. This requires patience and analytical skills. Practice makes perfect; the key is to try more and accumulate experience.

# Supplement

Following tips from netizens, I discovered that the structure of content pages varies slightly for different `mid` values. The `mid` used in this article uses static pages, so `Jsoup` was used for parsing. However, some `mid` values, such as `1558737777313793` (AI Financial News), use dynamic pages. For those, I used regular expression matching to obtain the data. Examples can be found on the `dev1.0` branch. You can refer to those to master both parsing methods; parsing other `mid` values is generally similar.
