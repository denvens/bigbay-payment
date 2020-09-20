package com.qingclass.bigbay.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.qingclass.bigbay.common.Prepay;
import com.qingclass.bigbay.exception.NoLoginException;

import lombok.extern.slf4j.Slf4j;
import static com.qingclass.bigbay.common.Constant.*;

@Slf4j
public class PrepayInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		if (handler instanceof HandlerMethod) {
			log.info("====== PrepayInterceptor ===========");
			Prepay prepay = ((HandlerMethod) handler).getMethodAnnotation(Prepay.class);
			// 拒绝假的openid和unionid
			if (null != prepay && prepay.needLogin()) {// 需要微信授权，获取真的openid
				log.info("======== start openId and unionId valid。。。。");
				
				String openId = request.getParameter("openId");
				String unionId = request.getParameter("unionId");

				// 未登陆
				if (StringUtils.isBlank(openId) || StringUtils.isBlank(unionId) || USER_NOT_SIGNIN.equals(openId)
						|| USER_NOT_SIGNIN.equals(unionId)) {
					log.warn("sorry, 当前用户没有登陆, 不能调起支付。请授权微信登陆！");
					throw new NoLoginException("sorry, 当前用户没有登陆, 不能调起支付。请授权微信登陆！");
				}

			}

		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
	}

}
