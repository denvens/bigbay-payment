package com.qingclass.bigbay.mail;

import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;



@Service
public class EmailServiceImpl implements EmailService {
	
	private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
	
    @Autowired
    public JavaMailSender emailSender;
    @Autowired
    private MailContentBuilder mailContentBuilder;
    
    

    @Override
    @Async
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("bigbay@qingclass.com");
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        } catch (MailException exception) {
        	log.error("sendSimpleMessage error...", exception);
            log.error(ExceptionUtils.getMessage(exception));
        }
    }
    
    @Override
    @Async
    public void prepareAndSend(String to, String[] cc, String subject, Map<String,Object> info) {
    	
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("bigbay@qingclass.com");
            messageHelper.setTo(to);
            messageHelper.setCc(cc);
            messageHelper.setSubject(subject);
            String content = mailContentBuilder.build(info);
            messageHelper.setText(content,true);
        };
        try {
            emailSender.send(messagePreparator);
        } catch (MailException e) {
        	log.error("prepareAndSend error...", e);
        	log.error(ExceptionUtils.getStackTrace(e));
            
        }
    }
}
