package com.qingclass.bigbay.controller;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	
	private static Logger log = LoggerFactory.getLogger(PrepayController.class);
	
	@GetMapping("/health")
	public String health(HttpServletResponse response) {
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
		response.setDateHeader("Expires", 0);
		return "i'm still alive :)";
	}
	
	@PostMapping("/mock-qingapp-callback")
	public String mock(@RequestBody String xml) {
		log.info("qingApp received:");
		log.info(xml);
		return "<xml>\n" + "  <return_code><![CDATA[SUCCESS]]></return_code>\n"
				+ "  <return_msg><![CDATA[OK]]></return_msg>\n" + "</xml>";
	}

	@PostMapping("/mock-qingapp-callback-json")
	public String mockJson(@RequestBody String requestBody) {
		log.info("qingApp received(mock-json):");
		log.info(requestBody);
		return "{\"success\": true}";
	}

}
