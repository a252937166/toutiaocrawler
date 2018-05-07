package com.ouyanglol.crawler.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ouyanglol.crawler.controller.CrawlerController;
import com.sun.imageio.plugins.bmp.BMPImageReader;
import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.jpeg.JPEGImageReader;
import com.sun.imageio.plugins.png.PNGImageReader;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author yue han jiang
 * @date 2015年3月12日
 * @file_name HttpClient.java
 */
public class Http {

	private static Logger logger = LoggerFactory.getLogger(CrawlerController.class.getName());
	
	private final String url;
	
	private HttpRequestBase requestBase;
	
	private RequestConfig config;

	private final Map<String, String> headers = new LinkedHashMap<String, String>();
	
	private final Map<String, Object> params = new LinkedHashMap<String, Object>();
	
	private StatusLine statusLine;
	
	private Header[] headerAll;
			
	private CloseableHttpClient httpClient;

	public Http(String url) {
		if(url.startsWith("https")){
			SSLContext sslContext = null;
			try {
				sslContext = new SSLContextBuilder().loadTrustMaterial(null,new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
						return true;
					}
				}).
				build();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			this.httpClient = HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)).build();
		}else{
			this.httpClient = HttpClients.createDefault();
		}
		this.url = url;
	}
	
	
	public Map<String, Object> getParams(){
		return this.params;
	}
	
	public Http addParam(String name,Object value){
		this.params.put(name, value);
		return this;
	}
	
	public Http addHeader(String name,String value){
		this.headers.put(name, value);
		return this;
	}

	public void setConfig(RequestConfig config) {
		this.config = config;
	}
	
	private HttpResult doExecute(){
		try {
			setConfig();
			addHeader();
			HttpResponse httpResponse = httpClient.execute(this.requestBase);
			this.headerAll = httpResponse.getAllHeaders();
			this.statusLine = httpResponse.getStatusLine();
			return new HttpResult(EntityUtils.toByteArray(httpResponse.getEntity()));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}finally{
			doClose();
		}
		return null;
	}
	private void setConfig() {
		if(config != null)
			requestBase.setConfig(config);
	}
	
	public HttpResult doGet(){
		try {
			requestBase = new HttpGet(getParamsURI());
			return doExecute();
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public HttpResult doPost(HttpEntity entity){
		try {
			requestBase = new HttpPost(url);
			HttpPost post = (HttpPost) this.requestBase;
			post.setEntity(entity);
			return doExecute();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public HttpResult doPost(){
		try {
			return doPost(new UrlEncodedFormEntity(addNameValuePairs(), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public HttpResult doPut(){
		try {
			return doPut(new UrlEncodedFormEntity(addNameValuePairs(),"utf-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	private List<NameValuePair> addNameValuePairs(){
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for (Entry<String, Object> en : this.params.entrySet())
			list.add(new BasicNameValuePair(en.getKey(), en.getValue().toString()));
		return list;
	}
	
	public HttpResult doPut(HttpEntity entity){
		try {
			requestBase = new HttpPut(url);
			HttpPut httpPut = (HttpPut) this.requestBase;
			httpPut.setEntity(entity);
			return doExecute();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public HttpResult doDelete(){
		try {
			requestBase = new HttpDelete(getParamsURI());
			return doExecute();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	private void addHeader(){
		headers.put("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		for (Entry<String, String> header : headers.entrySet())
			requestBase.addHeader(header.getKey(), header.getValue());
	}
	
	private void doClose(){
		try {
			if(this.requestBase != null)
				this.requestBase.abort();
			if(this.httpClient != null)
				this.httpClient.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private URI getParamsURI() throws URISyntaxException {
		final StringBuilder urls = new StringBuilder(this.url);
		if(!this.params.isEmpty()){
			if(url.indexOf("?") == -1)
				urls.append("?");
			else
				urls.append("&");
			int size = 0;
			for (Entry<String, Object> en : this.params.entrySet()){
			    if(size == 0)
			    	urls.append(en.getKey()+"="+en.getValue());
			    else
			    	urls.append("&"+en.getKey()+"="+en.getValue());
			    size++;
			}
		}
		return new URI(urls.toString());
	}
	


	public StatusLine getStatusLine() {
		return statusLine;
	}

	public Header[] getHeaderAll() {
		return headerAll;
	}
	
	public int getStatusCode(){
		return getStatusLine().getStatusCode();
	}

	public static byte[] getImageBytes(String imgUrl) {
		return getImageBytes(imgUrl,null);
	}

	public static byte[] getImageBytes(String imgUrl,String referer) {
		byte[] data = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(imgUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			if (!StringUtils.isEmpty(referer)) {
				conn.setRequestProperty("referer", "https://manhua.dmzj.com/yiquanchaoren/"); //这是破解防盗链添加的参数
			}
			// conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(6000);
			is = conn.getInputStream();
			if (conn.getResponseCode() == 200) {
				data = readInputStream(is);
			} else{
				data=null;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			conn.disconnect();
		}
		return data;
	}

	public static byte[] readInputStream(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = -1;
		try {
			while ((length = is.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}
			baos.flush();
		} catch (IOException e) {
			logger.info("getImageBytes::error"+e.getMessage());
			e.printStackTrace();
		}
		byte[] data = baos.toByteArray();
		try {
			is.close();
			baos.close();
		} catch (IOException e) {
			logger.info("getImageBytes::error"+e.getMessage());
			e.printStackTrace();
		}
		return data;
	}

    public static String getImgeType(byte[] mapObj) throws IOException
    {
        String type = "";
        ByteArrayInputStream bais = null;
        MemoryCacheImageInputStream mcis = null;
        try {
            bais = new ByteArrayInputStream(mapObj);
            mcis = new MemoryCacheImageInputStream(bais);
            Iterator itr = ImageIO.getImageReaders(mcis);
            while (itr.hasNext()) {
                ImageReader reader = (ImageReader)itr.next();
                if (reader instanceof GIFImageReader)
                    type = "gif";
                else if (reader instanceof JPEGImageReader)
                    type = "jpg";
                else if (reader instanceof PNGImageReader)
                    type = "png";
                else if (reader instanceof BMPImageReader)
                    type = "bmp";
            }
        }
        finally {
            if (bais != null)
                try {
                    bais.close();
                }
                catch (IOException ioe)
                {
                }
            if (mcis != null)
                try {
                    mcis.close();
                }
                catch (IOException ioe)
                {
                }
        }
        return type;
    }

	public class HttpResult{
		
		private Integer resultCode;
		
		private byte[] resultBytes;
		
		
		/**
		 * 把结果集装换成 map 对象
		 * @auther Y.hj
		 * @return
		 */
		public Map<String, Object> toMap(){
			return toJsonObject(Map.class);
		}
		
		
		/**
		 * 结果返回 bytes
		 * @return
		 */
		public byte[] toBytes(){
			return this.resultBytes;
		}
		
		/**
		 * 返回 String
		 * @return
		 */
		public String toString(){
			try {
				return new String(this.resultBytes, "utf-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e);
			}
			return null;
		}
		
		public JSONObject toJsonObject(){
			return JSON.parseObject(toString());
		}
		
		@SuppressWarnings("unchecked")
		public <T> T toJsonObject(Class<?> clazz){
			try {
				return (T) JSON.parseObject(toString(), clazz);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T toObject(){
			try {
				return (T) SerializationUtils.deserialize(toBytes());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			return null;
		}

		public HttpResult(byte[] resultBytes) {
			super();
			this.resultBytes = resultBytes;
		}

		public Integer getResultCode() {
			return resultCode;
		}

		public void setResultCode(Integer resultCode) {
			this.resultCode = resultCode;
		}
		
		
		
	}
}
