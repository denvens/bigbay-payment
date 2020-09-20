package com.qingclass.bigbay.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.mapper.config.BigbayAppMapper;
import com.qingclass.bigbay.tool.Tools;

/**
 * @author lijiecai
 * @description: 签名校验切面
 * @date 2019-09-06 15:56
 */
@Component
@Aspect
@EnableAspectJAutoProxy
public class SignatureVerifyAspect {
    private static final Logger logger = LoggerFactory.getLogger(SignatureVerifyAspect.class);

    @Autowired
    private BigbayAppMapper bigbayAppMapper;

    @Around("@annotation(com.qingclass.bigbay.annotation.SignatureVerify)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{

        Object[] params = joinPoint.getArgs();
        String bigbayAppId = (String)params[0];
        String content = (String)params[1];
        String random = (String)params[2];
        String timestamp = (String)params[3];
        String signature = (String)params[4];

        logger.info("=======外部调用海湾接口时签名校验...bigbayAppId={}",bigbayAppId);
        BigbayApp bigbayApp = bigbayAppMapper.getById(Long.parseLong(bigbayAppId));

        if(bigbayApp == null){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success",false);
            jsonObject.put("message","bigbayAppId不存在");
            return jsonObject.toJSONString();
        }

        //获取SignKey
        String bigbaySignKey = bigbayApp.getBigbaySignKey();

        //签名校验
        StringBuffer sb = new StringBuffer();
        sb.append("bigbayAppId="+bigbayAppId).
                append("&").
                append("content="+content).
                append("&").
                append("random="+random).
                append("&").
                append("timestamp="+timestamp).
                append("&").
                append("key="+bigbaySignKey);


        String crypt = Tools.md5(sb.toString()).toUpperCase();
        logger.info("crypt:" + crypt);
        if(crypt.equalsIgnoreCase(signature)){
            logger.info("===bigbayAppId={},外部调用海湾接口时签名校验[签名校验成功...]",bigbayAppId);
            return  joinPoint.proceed();
        }else{
            logger.error("===bigbayAppId={},外部调用海湾接口时签名校验[签名校验失败...]",bigbayAppId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success",false);
            jsonObject.put("message","签名校验失败");
            return jsonObject.toJSONString();
        }

    }

}
