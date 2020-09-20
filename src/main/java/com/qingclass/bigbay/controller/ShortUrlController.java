package com.qingclass.bigbay.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qingclass.bigbay.mapper.zebra.UrlMappingMapper;
import com.qingclass.bigbay.tool.Tools;

@RestController
public class ShortUrlController {

	private static Logger log = LoggerFactory.getLogger(ShortUrlController.class);
	
	@Autowired
	private UrlMappingMapper urlMappingMapper;
	@Value("${audition.qrcode.domain}")
    private String auditionQrcodeDomain;

	
	@PostMapping("/long-url-to-short-url")
	public Map<String, Object> longUrlToShortUrl(
			@RequestParam("url") String url,HttpServletRequest request) {
		log.info("longUrl:"+url);
		Map<String, Object> map =new HashMap<>();
		map.put("url", url);
		urlMappingMapper.insert(map);
		String shortUrl=auditionQrcodeDomain+"short/"+map.get("id");
		map.clear();
		map.put("shortUrl", shortUrl);
		return Tools.s(map);
	}
	
	
	
	@RequestMapping("/short/{id}")
	public void shortUrl(@PathVariable("id") Integer id,HttpServletRequest request,HttpServletResponse httpServletResponse) throws IOException {
		Map<String, Object> map = urlMappingMapper.getById(id);
		String url =(String) map.get("url");
		log.info("url:"+url+",id:"+id);
		httpServletResponse.sendRedirect(url);
	}
	
	
}
