package com.qingclass.bigbay.mail;

import java.util.Map;

public interface EmailService {
	/**
	 * 发送简单文本内容
	 * 
	 * @param to      发件人
	 * @param subject 主题
	 * @param text    内容
	 */
	void sendSimpleMessage(String to, String subject, String text);
	
	
	/**
     * 传递多个变量，用于动态更换页面模版内容
     * @param emailInfoMap
     */
    void prepareAndSend(String to, String[] cc,String subject, Map<String,Object> emailInfoMap);
}
