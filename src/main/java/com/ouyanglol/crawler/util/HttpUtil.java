package com.ouyanglol.crawler.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpUtil {
    /*请求url获取返回的内容*/
    public static String getReturn(String url) {
        URL serverUrl = null;
        HttpURLConnection conn = null;
        StringBuilder buffer = new StringBuilder();
        try {
            serverUrl = new URL(url);
            conn = (HttpURLConnection) serverUrl.openConnection();
            assert conn != null;
            conn.setRequestMethod("GET");

            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //必须设置false，否则会自动redirect到重定向后的地址
            conn.setInstanceFollowRedirects(false);
            conn.connect();

            //将返回的输入流转换成字符串
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert conn != null;
            conn.disconnect();
        }
        return buffer.toString();
    }
}
