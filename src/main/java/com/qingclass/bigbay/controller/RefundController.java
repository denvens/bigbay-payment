package com.qingclass.bigbay.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qingclass.bigbay.service.RefundService;
import com.qingclass.bigbay.tool.Tools;

/**
 * 退款业务类
 * @author sss
 * @date 2019年11月7日 下午6:44:22
 */
@RestController
@RequestMapping("/refund")
public class RefundController {
	private static final Logger logger = LoggerFactory.getLogger(RefundController.class);
	
    @Autowired
    private RefundService refundService;
    

    /**
     * 拼团失败进行退款
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/changeAssembleStatus")
    public Map<String,Object> changeAssembleStatus(HttpServletRequest request) throws Exception{
    	String signature = request.getParameter("signature");
    	String timestamp = request.getParameter("timestamp");
    	StringBuffer sb = new StringBuffer();
        sb.append("source=bigbay").
                append("&").
                append("timestamp="+timestamp).
                append("&").
                append("key=099390fe422c4643bbb0a8766a60102f");
        
        String crypt = Tools.md5(sb.toString()).toUpperCase();
        logger.info("crypt:" + crypt);
        if(!crypt.equalsIgnoreCase(signature)){
            return Tools.f("不是来自海湾的请求！");
        }
        return refundService.changeAssembleStatus();
    }
    
    /**
     * 拼团失败进行退款
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/assemble")
    public Map<String,Object> assemble(HttpServletRequest request) throws Exception{
    	String signature = request.getParameter("signature");
    	String timestamp = request.getParameter("timestamp");
    	StringBuffer sb = new StringBuffer();
        sb.append("source=bigbay").
                append("&").
                append("timestamp="+timestamp).
                append("&").
                append("key=099390fe422c4643bbb0a8766a60102e");
        
        String crypt = Tools.md5(sb.toString()).toUpperCase();
        logger.info("assemble automatic refund start.");
        if(!crypt.equalsIgnoreCase(signature)){
            return Tools.f("不是来自海湾的请求！");
        }
        return refundService.refund();
    }

}
