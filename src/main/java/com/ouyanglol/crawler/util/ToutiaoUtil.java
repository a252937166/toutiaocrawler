package com.ouyanglol.crawler.util;

import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Package: com.fishsaying.ce.util
 *
 * @Author: Ouyang
 * @Date: 2018/3/13
 */
public class ToutiaoUtil {
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
}
