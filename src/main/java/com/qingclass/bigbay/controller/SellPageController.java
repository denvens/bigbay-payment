package com.qingclass.bigbay.controller;

import static com.qingclass.bigbay.common.Constant.USER_NOT_SIGNIN;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qingclass.bigbay.cache.index.*;
import com.qingclass.bigbay.entity.config.*;
import com.qingclass.bigbay.mapper.config.AssembleRuleMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qingclass.bigbay.common.Constant;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.wechatUsers.BigbayFullUsers;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.entity.zebra.ZebraDistributors;
import com.qingclass.bigbay.enums.ChannelTypeEnum;
import com.qingclass.bigbay.mapper.config.BigbayIapItemMapper;
import com.qingclass.bigbay.mapper.config.SellPageItemMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.mapper.sales.BigbayChannelMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayFullUsersMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayGroupBuyUserMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.mapper.zebra.ZebraDistributorsMapper;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.price.SellPageItemPricePipeManager;
import com.qingclass.bigbay.service.BiabaySalesService;
import com.qingclass.bigbay.service.OtherPayService;
import com.qingclass.bigbay.service.SellPageService;
import com.qingclass.bigbay.service.WechatUsersService;
import com.qingclass.bigbay.tool.CommodityTypeEnum;
import com.qingclass.bigbay.tool.Tools;

@Controller
public class SellPageController extends BaseController {
	public SellPageController() {
	}
	private static Logger log = LoggerFactory.getLogger(SellPageController.class);
	

	@Value("${url.sell.page.images.domain}")
	private String httpIp;
	@Autowired
	private SellPageTemplateCacheBySellPageId sellPageTemplateCacheBySellPageId;
	@Autowired
	private SellPageTemplateCacheById sellPageTemplateCacheById;
	@Autowired
	private ZebraDistributorsMapper zebraDistributorsMapper;
	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;
	@Autowired
	private SellPageCacheByPageKey sellPageCacheByPageKey;
	@Autowired
	private SellPageItemCacheBySellPageId sellPageItemCacheBySellPageId;
	
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;

	@Autowired
	private BigbayIapItemMapper bigbayIapItemMapper;
	
	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;

	@Autowired
	private BiabaySalesService biabaySalesService;
	
	@Autowired
	private SellPageService sellPageService;

	@Autowired
	private WechatUsersService wechatUsersService;
	@Autowired
	private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;
	@Autowired
	private BigbayFullUsersMapper bigbayFullUsersMapper;
	@Autowired
	private BigbayChannelMapper bigbayChannelMapper;
	@Autowired
	private OtherPayService otherPayService;
	@Autowired
	private BigbayGroupBuyUserMapper bigbayGroupBuyUserMapper;
	@Autowired
	private HttpClient httpClient;

	@Value("${url.auth2.invoke}")
	private String urlAuth2Invoke;

	@Value("${wechat.third.appId}")
	private String wechatThirdAppId;

	@Value("${url.code.to.openId}")
	private String urlCodeToOpenId;

	@Value("${url.code.to.userInfo}")
	private String urlCodeToUserInfo;

	@Value("${url.generate.js.sdk.signature}")
	private String urlGenerateJsSdkSignature;

	@Value("${url.js.entry}")
	private String urlJsEntry;

	@Value("${audition.qrcode.domain}")
	private String sellPageDomain;

	@Value("${star.payment.sdk.js.url}")
	private String starPaymentSdkJs;

	@Autowired
	private SellPageItemMapper sellPageItemMapper;
	
	@Autowired
	private SellPageItemPricePipeManager sellPageItemPricePipeManager;

	@Autowired
	private AssembleRuleCacheBySellPageItemId assembleRuleCacheBySellPageItemId;


	private void exipreAppIdOpenIdToCookie(HttpServletResponse response) {
		Cookie cookieAppId = new Cookie("appId", "");
		Cookie cookieOpenId = new Cookie("openId", "");
		cookieAppId.setHttpOnly(true);
		cookieOpenId.setHttpOnly(true);
		cookieAppId.setMaxAge(0);
		cookieOpenId.setMaxAge(0);
		response.addCookie(cookieAppId);
		response.addCookie(cookieOpenId);
	}

	private void saveAppIdOpenIdToCookie(String appId, String openId, HttpServletResponse response) {
		if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(openId)) {
			return;
		}
		Cookie cookieAppId = new Cookie("appId", appId);
		Cookie cookieOpenId = new Cookie("openId", openId);
		cookieAppId.setHttpOnly(true);
		cookieOpenId.setHttpOnly(true);
		cookieAppId.setMaxAge(2 * 3600);
		cookieOpenId.setMaxAge(2 * 3600);
		response.addCookie(cookieAppId);
		response.addCookie(cookieOpenId);
	}


	private String getCookie(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * 更新userinfo 记录分销关系
	 * 
	 * @param openId
	 * @param pageKey
	 * @param request
	 * @return
	 */
	@PostMapping("/update-user-info")
	@ResponseBody
	public Map<String, Object> updateUserInfo(@RequestParam("openId") String openId,
			@RequestParam("pageKey") String pageKey, HttpServletRequest request) {
		Integer distributorId = StringUtils.isEmpty(request.getParameter("distributorId")) ? null
				: Integer.valueOf(request.getParameter("distributorId"));
		log.info("distributor-relation=================openId=" + openId + ",distributorId" + distributorId + ",pageKey"
				+ pageKey);
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		Map<String, Object> returnMap = null;
		try {
			if (distributorId != null) {
				ZebraDistributors zebraDistributors = zebraDistributorsMapper.selectByPrimaryKey(distributorId);
				if (zebraDistributors == null) {
					log.info("distributorId:" + distributorId + " is not exists in system======");
					distributorId = null;
				}
			}
			returnMap = biabaySalesService.distributorRelation(openId, sellPage, distributorId);
			log.info("update-user-info returnmap is =====" + JSON.toJSONString(returnMap));
		} catch (Exception e) {
			e.printStackTrace();
			return Tools.f();
		}
		return Tools.s(returnMap);
	}

	/**
	 * 购买页面记录渠道埋点
	 * 
	 * @return
	 */
	@PostMapping("/record-channel-access-log")
	@ResponseBody
	public Map<String, Object> recordChannelAccessLog(@RequestBody Map<String, Object> param, HttpServletRequest request) {
		log.info("=====channelAccessLog======传递的参数" + JSON.toJSONString(param));
		biabaySalesService.recordChannelAccessLog(param);
		return Tools.s("");
	}
	
	/**
	 * 购买页面记录渠道埋点
	 * 
	 * @return
	 */
	@PostMapping("/record-abtest-access-log")
	@ResponseBody
	public Map<String, Object> recordAbTestAccessLog(@RequestBody Map<String, Object> param, HttpServletRequest request) {
		log.info("=====recordAbTestAccessLog======传递的参数" + JSON.toJSONString(param));
		biabaySalesService.recordAbTestAccessLog(param);
		return Tools.s("");
	}
	
	/**
	 * 购买页面记录购买页埋点
	 *
	 * @return
	 */
	@PostMapping("/record-sell-page-access-log")
	@ResponseBody
	public Map<String, Object> recordSellPageAccessLog(@RequestBody Map<String, Object> param, HttpServletRequest request) {
		log.info("=====createSplunch======传递的参数" + JSON.toJSONString(param));
		biabaySalesService.recordSellPageAccessLog(param);
		return Tools.s("");
	}
	
	public  String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}


	@PostMapping("/sign-in-with-openid")
	@ResponseBody
	public Map<String, Object> signInWithOpenId(@RequestParam("openId") String openId,@RequestParam("pageKey") String pageKey,
			HttpServletRequest request,HttpServletResponse httpServletResponse) {
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		String appId = bigbayApp.getWechatAppId();
		saveAppIdOpenIdToCookie(appId, openId, httpServletResponse);

		// 维护微信登录用户信息，分销关系
		Map<String, Object>  userinfoMap= updateLoginUserInfo(openId, pageKey, request);
		userinfoMap.put("openId", openId);
		return Tools.s(userinfoMap);
	}

	@PostMapping("/code-to-openid")
	@ResponseBody
	public Map<String, Object> codeToOpenIdTemp(@RequestParam("pageKey") String pageKey,
			 HttpServletRequest request, HttpServletResponse httpServletResponse) {
		log.info("====codeToOpenIdTemp..start======pageKey=" + pageKey);
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		String appId = bigbayApp.getWechatAppId();
		String code = request.getParameter("code");
		String openId = request.getParameter("openId");
		String unionId = request.getParameter("unionId");
		if(StringUtils.isEmpty(openId)) {
			HttpPost httpPost = null;
			String scope = request.getParameter("scope");
			if (!StringUtils.isEmpty(scope) && "base".equals(scope)) {
				httpPost = new HttpPost(urlCodeToOpenId);
			} else {
				httpPost = new HttpPost(urlCodeToUserInfo);
			}
			Map<String, String> postParams = new HashMap<String, String>();
			postParams.put("appId", appId);
			postParams.put("code", code);
			Tools.setHttpPostParameters(httpPost, postParams);
			HttpResponse response = null;
			String responseBody = null;
			try {

				Date d1 = new Date();
				
				response = httpClient.execute(httpPost);
				responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
				Date d2 = new Date();

				log.info("[it takes " + (d2.getTime() - d1.getTime()) + "] this is the body of code: " + responseBody);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String, Object> ret = Tools.jsonToMap(responseBody);

			@SuppressWarnings("unchecked")
			Map<String, String> retData = (Map<String, String>) ret.get("data");

			// 维护微信用户信息
			wechatUsersService.maintainUserInfo(scope, bigbayApp, retData);
			unionId = retData.get("unionId");
			openId = retData.get("openId");

		}

		saveAppIdOpenIdToCookie(appId, openId, httpServletResponse);
		
		String otherPayId = request.getParameter("otherPayId");
		log.info("=========otherPayId={}", otherPayId);
		if(!StringUtils.isEmpty(otherPayId)) {//代付
			log.info("===============进入代付页面。。。。。。");
			//查询代付的价格和代付相关的信息
			int payId = Integer.parseInt(otherPayId);
			long bigbayAppId = bigbayApp.getId();
			Map<String, Object>  map=  otherPayService.getOtherPayInfo(bigbayAppId, payId, openId);
			return Tools.s(map);
		}

		// 维护微信登录用户信息，分销,渠道关系
		Map<String, Object>  userinfoMap = Maps.newHashMap();
		if(!USER_NOT_SIGNIN.equals(openId)) {
			userinfoMap = updateLoginUserInfo(openId, pageKey, request);
		}
		
		
		//计算价格
		List<Map<String, Object>> prices = getEachItemPrice(request, sellPage, openId, unionId);
						
		
		userinfoMap.put("openId", openId);
		userinfoMap.put("prices", prices);

		return Tools.s(userinfoMap);
	}
	

	private List<Map<String, Object>> getEachItemPrice(HttpServletRequest request, SellPage sellPage, String openId, String unionId) {
		List<Map<String, Object>> list = Lists.newArrayList();
		
		String source = request.getParameter("source");
		String userSelections = request.getParameter("userSelections");
		if (StringUtils.isEmpty(unionId)) {//通过openid获取unionid
			unionId = unionId(openId, sellPage.getBigbayAppId());
			
		}
		
		log.info("========unionId={}", unionId);
		
		long sellPageId = sellPage.getId();

		//潘多拉优惠卷
		if (StringUtils.isEmpty(userSelections)) {
			userSelections = "{}";
		}
		
		Map<String, Object> userSelectionsMap = Tools.jsonToMap(userSelections);
		String pandoraCouponId = String.valueOf(userSelectionsMap.get("pandoraCouponId"));
		if (userSelectionsMap.get("pandoraCouponId")==null) {
			pandoraCouponId = "";
		}
		
		List<SellPageItem> sellPageItems = sellPageItemCacheBySellPageId.getListByKey(String.valueOf(sellPageId));
		for (SellPageItem sellPageItem : sellPageItems) {
			Map<String,Object> map = Maps.newHashMap();
			SellPageItemPricePipeContext context = new SellPageItemPricePipeContext();
			context.setSellPageItemIds(Arrays.asList(sellPageItem.getId() + ""));
			context.setPandoraCouponId(pandoraCouponId);//潘多拉优惠卷
			context.setUnionId(unionId);
			context.setSource(source);//全名分销和阅读邀请有奖/松鼠邀请有奖
			
			boolean	distributionDisabled = request.getParameter("distributionDisabled")==null ? false : Boolean.valueOf(request.getParameter("distributionDisabled"));
			boolean distributionStateValid=sellPageItem.getDistributionState()==1?true:false;
			if(!distributionStateValid) {
				distributionDisabled=true;
			}
			if(!distributionDisabled) {
				Integer distributorId = getDistributorId(unionId, sellPage.getPageKey());
				context.setDistributorId(distributorId == null? "": distributorId.toString());
			}

			
			int price = sellPageItemPricePipeManager.getPriceForItem(context);
			log.info("code-to-open-id ===商品ID:{}===支付的价格是:{}===",sellPageItem.getId(), price);
			map.put("sellPageItemId", sellPageItem.getId());
			map.put("price", price);
			list.add(map);

		}
		return list;
	}


	/**
	 * 更新userinfo 记录分销关系
	 * @param openId
	 * @param pageKey
	 * @param request
	 * @return
	 */
	private Map<String, Object> updateLoginUserInfo(String openId, String pageKey, HttpServletRequest request) {
		Integer distributorId = StringUtils.isEmpty(request.getParameter("distributorId")) ? null : Integer.valueOf(request.getParameter("distributorId"));
		String channelKey = request.getParameter("channelKey");
		log.info("distributor-relation=================openId=" + openId + ",distributorId" + distributorId + ",pageKey"+ pageKey+",channelKey:"+channelKey);
		Map<String, Object> returnMap = new HashMap<>();
		
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		BigbaySimpleUsers bigbaySimpleUsers = bigbaySimpleUsersMapper.getUser(sellPage.getBigbayAppId(), openId);
		boolean flushDistributionExpireFlag = true;//默认重置分销的有效期
		try {

			Integer channelId = null;//默认channel不存在
			if(org.apache.commons.lang3.StringUtils.isNotBlank(channelKey)) {//渠道key存在
				Map<String, Object> channelMap = bigbayChannelMapper.selectByChannelKey(channelKey);
				String channelType = (String) channelMap.get("channelType");//渠道类
				returnMap.put("channelName", channelMap.get("name"));
				if(ChannelTypeEnum.PROMOTE.name().equals(channelType)) {//推广渠道 默认值 不计分销
					log.info("渠道类型为默认的推广渠道，推广渠道默认不计算分销");
					distributorId = null;
					channelId = (Integer) channelMap.get("id");
				}else if(ChannelTypeEnum.STATISTICS.name().equals(channelType)) {//统计渠道 计算分销
					log.info("渠道类型为统计渠道，计算分销");
					channelId = (Integer) channelMap.get("id");
					if(null != distributorId) {
						//校验有效性
						ZebraDistributors zebraDistributors = zebraDistributorsMapper.selectByPrimaryKey(distributorId);
						if (zebraDistributors == null) { //无效
							log.info("distributorId:" + distributorId + " is not exists in system======");

							distributorId = biabaySalesService.getDistributorIfActiveDistribution(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());

							if(distributorId != null){
								flushDistributionExpireFlag = false;
							}

						}
					}else {//distributorId == null 检查是否有存活的分销关系
						// 统计渠道计算分销 

						distributorId = biabaySalesService.getDistributorIfActiveDistribution(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());

						if (distributorId != null){ //不刷新已尽存在的分销关系的有效期
							flushDistributionExpireFlag = false;
						}
						log.info("渠道类型为统计渠道，查询是否存在活跃的渠道关系,distrubutorId={}", distributorId);
						
						
					}
				}
			}else if(null != distributorId) {//channelId=null distributorId!=null
				//校验有效性
				ZebraDistributors zebraDistributors = zebraDistributorsMapper.selectByPrimaryKey(distributorId);
				if (zebraDistributors == null) {
					log.info("distributorId:" + distributorId + " is not exists in system======");
					distributorId = null;
				}
			}else {//channelId=null distributorId=null
				channelId = null;
				distributorId = null;
			}
			
			
			//2.获取用户信息
			String unionId= "",nickName = "",headImgUrl="";
			//SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
			//BigbaySimpleUsers bigbaySimpleUsers = bigbaySimpleUsersMapper.getUser(sellPage.getBigbayAppId(), openId);
			log.info("====openId=" + openId + ",biabayappid :"+sellPage.getBigbayAppId()+" get bigbaySimpleUsers is=" + bigbaySimpleUsers);
			BigbayFullUsers bigbayFullUsers = bigbayFullUsersMapper.selectByPrimaryKey(bigbaySimpleUsers.getBigbayFullUserId());
			if (bigbayFullUsers != null) {
				unionId = bigbayFullUsers.getUnionId();
				nickName = bigbayFullUsers.getNickName();
				headImgUrl = bigbayFullUsers.getHeadImgUrl();
			}
			returnMap.put("unionId", unionId);
			returnMap.put("nickName", nickName);
			returnMap.put("headImgUrl", headImgUrl);
			returnMap.put("bigbaySimpleUserId", bigbaySimpleUsers.getId());
			
		
			//3.维护分销关系、渠道关系
			if(distributorId!=null && channelId == null) {
				log.info("distributorId!=null && channelId == null");
				returnMap = biabaySalesService.distributonRelation(bigbaySimpleUsers, sellPage, distributorId,returnMap);
			}else if(channelId!=null && distributorId == null) {
				log.info("channelId!=null && distributorId == null");
				returnMap = biabaySalesService.channelRelation(bigbaySimpleUsers, sellPage,channelId,returnMap);
			}else if(channelId==null && distributorId==null){
				log.info("channelId==null && distributorId==null");
				returnMap = biabaySalesService.getActiveRelation(bigbaySimpleUsers, sellPage,returnMap);
			} else if(channelId != null && distributorId != null) {//统计渠道计算分销
				log.info("channelId != null && distributorId != null");
				returnMap = biabaySalesService.channelAndDistribution(bigbaySimpleUsers, sellPage, distributorId, channelId, returnMap, flushDistributionExpireFlag);
			}
			log.info("updateLoginUserInfo returnmap is =====" + JSON.toJSONString(returnMap));
		} catch (Exception e) {
			e.printStackTrace();
			return Tools.f();
		}
		return returnMap;
		
	}


	////=====
	@PostMapping("/sign-in-with-open-id")
	@ResponseBody
	public Map<String, Object> signInWithOpenId(
		@RequestParam("openId") String openId,
		@RequestParam("pageKey") String pageKey,
		HttpServletResponse httpServletResponse
	) {
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		String appId = bigbayApp.getWechatAppId();
		saveAppIdOpenIdToCookie(appId, openId, httpServletResponse);
		return Tools.s(Tools.arrayToMap(new String[] { "openId", openId }));
	}
	
	@PostMapping("/code-to-open-id")
	@ResponseBody
	public Map<String, Object> codeToOpenId(@RequestParam("code") String code, @RequestParam("pageKey") String pageKey,
			HttpServletRequest request,HttpServletResponse httpServletResponse) {
		log.info("code to openid: code=" + code);
		log.info("====codeToOpenId..start======pageKey="+pageKey);
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		String appId = bigbayApp.getWechatAppId();
		HttpPost httpPost = null;
		String scope = request.getParameter("scope");
		if(!StringUtils.isEmpty(scope) && "base".equals(scope)) {
			httpPost = new HttpPost(urlCodeToOpenId);
		} else {
			httpPost = new HttpPost(urlCodeToUserInfo);
		}
		Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("appId", appId);
		postParams.put("code", code);
		Tools.setHttpPostParameters(httpPost, postParams);
		HttpResponse response = null;
		String responseBody = null;
		try {
			response = httpClient.execute(httpPost);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
			log.info("code to openid body=" + responseBody);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Object> ret = Tools.jsonToMap(responseBody);

		@SuppressWarnings("unchecked")
		Map<String, String> retData = (Map<String, String>) ret.get("data");

		// 维护微信用户信息
		wechatUsersService.maintainUserInfo(scope, bigbayApp, retData);
		
		String openId = retData.get("openId");

		saveAppIdOpenIdToCookie(appId, openId, httpServletResponse);
		
		
		
		

		return Tools.s(Tools.arrayToMap(new String[] { "openId", openId }));
	}

	

	@GetMapping("/clear-cookie")
	@ResponseBody
	public String clearCookie(HttpServletResponse response,HttpServletRequest request) {
		Cookie[] cookies=request.getCookies();
		for(Cookie cookie : cookies){
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
		}
		return "cookie cleared";
	}

	@PostMapping("/payment-status")
	@ResponseBody
	public Map<String, Object> getPaymentStatus(@RequestParam("prepayId") String prepayId) {
		log.info("payment-status************************************");
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByPrepayId(prepayId);
		long wechatNotifiedAt = 0;
		long qingAppRespondedAt = 0;
		String wechatTransactionId = null;
		String outTradeNo = null;
		Map<String, Object> result = new HashMap<>();

		if (null != paymentTransaction) {
			if (null != paymentTransaction.getWechatNotifiedAt()) {
				wechatNotifiedAt = paymentTransaction.getWechatNotifiedAt().getTime();
			}

			if (null != paymentTransaction.getQingAppRespondedAt()) {
				qingAppRespondedAt = paymentTransaction.getQingAppRespondedAt().getTime();
			}
			
			if (!StringUtils.isEmpty(paymentTransaction.getWechatTransactionId())) {
				wechatTransactionId = paymentTransaction.getWechatTransactionId();
			}
			
			if (!StringUtils.isEmpty(paymentTransaction.getOutTradeNo())) {
				outTradeNo = paymentTransaction.getOutTradeNo();
			}
			
		}

		result.put("outTradeNo", outTradeNo);
		result.put("wechatTransactionId", wechatTransactionId);
		result.put("wechatNotifiedAt", wechatNotifiedAt);
		result.put("qingAppRespondedAt", qingAppRespondedAt);
		return Tools.s(result);
	}
	
	@PostMapping("/payment-status-group-buy")
	@ResponseBody
	public Map<String, Object> getPaymentStatusSecond(@RequestParam("prepayId") String prepayId) {
		
		log.info("payment-status-group-buy************************************");
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByPrepayIdForGroupBuy(prepayId);
		long paymentOrderId = 0;
		long wechatNotifiedAt = 0;
		long qingAppRespondedAt = 0;
        String wechatTransactionId = null;
		String outTradeNo = null;
		Map<String, Object> result = new HashMap<>();

		if (null != paymentTransaction) {
            paymentOrderId = paymentTransaction.getId();
			if (null != paymentTransaction.getWechatNotifiedAt()) {
				wechatNotifiedAt = paymentTransaction.getWechatNotifiedAt().getTime();
			}
			
			if (!StringUtils.isEmpty(paymentTransaction.getOutTradeNo())) {
				outTradeNo = paymentTransaction.getOutTradeNo();
			}

            if (!StringUtils.isEmpty(paymentTransaction.getWechatTransactionId())) {
                wechatTransactionId = paymentTransaction.getWechatTransactionId();
            }
		}
		log.info("sellPageItemId---->:{}",paymentTransaction.getGroupBuySellPageItemId());
		
		SellPageItem sellPageItem = sellPageItemCacheById.getByKey("" + paymentTransaction.getGroupBuySellPageItemId());
		
		long groupBuyActivityId = 0L;
        //是否拼团商品,拼团商品
		if(sellPageItem.getIsGroupBuy() == CommodityTypeEnum.GroupBuy.getKey().intValue()){
			GroupBuyUser groupBuyUser = bigbayGroupBuyUserMapper.selectByPaymentOrderId(paymentOrderId);
			log.info("outTradeNo:{}--------groupBuyUser:{}", outTradeNo, groupBuyUser);
			if (groupBuyUser != null) {
                groupBuyActivityId = groupBuyUser.getAssembleActivityId();
                if (null != groupBuyUser.getNotifyQingAppSuccessTime()) {
					qingAppRespondedAt = groupBuyUser.getNotifyQingAppSuccessTime().getTime();
				}
            }
		}
		
		result.put("outTradeNo", outTradeNo);
        result.put("wechatTransactionId", wechatTransactionId);
		result.put("wechatNotifiedAt", wechatNotifiedAt);
		result.put("qingAppRespondedAt", qingAppRespondedAt);
		result.put("groupBuyActivityId", groupBuyActivityId);

		return Tools.s(result);
	}

	@GetMapping({
		"/mall",
		"/courses",
		"/qlessons",
		"/market",
		"/markets",
		"/shops",
		"/qshops",
		"/coupons",
		"/sales"
	})
	public String mall(HttpServletResponse response, HttpServletRequest request,
			@RequestParam("pageKey") String pageKey, ModelMap model) throws Exception {

		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, proxy-revalidate, max-age=0");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "0");
		
		if (request.getServerName().equals("bigbay-payment.jiguangdanci.com")) {
			if (StringUtils.isEmpty(request.getParameter("fromDistributorId"))) {
				String redirectTo = Tools.getRequestedUrl(request);
				redirectTo = redirectTo.replaceAll("//bigbay-payment.jiguangdanci.com", "//bigbay.qingclass.com");
				response.getWriter().println("<html><head><script type=\"text/javascript\">setTimeout(function () {location.replace(\"" + redirectTo + "\");}, 20);</script></head></html>");
				return "emptyTemplate";
			}
		}

		String scope = request.getParameter("scope");
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		if (sellPage == null) {
			throw new Exception("根据pageKey+" + pageKey + "没有取到SellPage");
		}
	
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());

		List<SellPageItem> sellPageItems = sellPageItemCacheBySellPageId.getListByKey(String.valueOf(sellPage.getId()));

		if ("2".equals(sellPage.getState()) || CollectionUtils.isEmpty(sellPageItems)) {
			model.addAttribute("message","对不起，该商品已下架！");
			return "sellPageInvalid";
		}
		//渠道无效的时候，提示该商品以下架
		String channelKey = request.getParameter("channelKey");
		if(org.apache.commons.lang3.StringUtils.isNotBlank(channelKey)) {
			Map<String, Object> map = bigbayChannelMapper.selectByChannelKey(channelKey);
			Integer state = (Integer) map.get("state");
			if(2 == state) {
				model.addAttribute("message","对不起，该商品已下架！");
				return "sellPageInvalid";
			}
		}
		
		request.getCookies();
		// 要继续渲染，需要满足三个条件中的至少一个（否则跳转Wechat Auth2）：
		// 1. cookie中有未过期的appId和openId(和被请求的sellPage所属的appId相匹配)
		// 2. url上携带code参数
		// 3. url上携带userOpenId参数
		String openIdFromCookie = getCookie(request, "openId");
		String appIdFromCookie = getCookie(request, "appId");
		String codeFromUrl = request.getParameter("code");
		String openIdFromUrl = request.getParameter("userOpenId");

		if (StringUtils.isEmpty(openIdFromCookie) || !bigbayApp.getWechatAppId().equals(appIdFromCookie)) {
			if (StringUtils.isEmpty(codeFromUrl) && StringUtils.isEmpty(openIdFromUrl)) {
				exipreAppIdOpenIdToCookie(response);
				String redirectTo = Tools.getRequestedUrl(request);
				redirectTo = URLEncoder.encode(redirectTo, "UTF-8");
				StringBuffer location = new StringBuffer(urlAuth2Invoke);
				location.append("?appid=").append(bigbayApp.getWechatAppId());
				location.append("&redirect_uri=").append(redirectTo);
				location.append("&response_type=code");
				if (!StringUtils.isEmpty(scope) && "base".equals(scope)) {
					location.append("&scope=snsapi_base");
				} else {
					location.append("&scope=snsapi_userinfo");
				}
				location.append("&component_appid=").append(wechatThirdAppId);
				location.append("#wechat_redirect");
				response.getWriter().println("<html><head><script type=\"text/javascript\">setTimeout(function () {location.replace(\"" + location.toString() + "\");}, 20);</script></head></html>");
				// response.sendRedirect(location.toString());
				return "emptyTemplate";
			}
		}

		if (null == openIdFromCookie) {
			openIdFromCookie = "";
		}

		// 将cookie有效期延长十分钟
		saveAppIdOpenIdToCookie(appIdFromCookie, openIdFromCookie, response);

		Map<String, Object> sellPageItemMap = new LinkedHashMap<>();
		for (SellPageItem sellPageItem : sellPageItems) {
			// Do not expose callbackConfig to front-end. It's only used for payment
			// callback.
			String sellPageName = sellPageItem.getName();
			Map<String, Object> itemMap = new HashMap<>();
			itemMap.put("sellPageItemId", sellPageItem.getId());
			itemMap.put("name", sellPageName);
			itemMap.put("price", sellPageItem.getPrice());
			itemMap.put("distributionDiscount", sellPageItem.getDistributionDiscount());
			itemMap.put("toUrl", sellPageItem.getToUrl());
			itemMap.put("distributionState", sellPageItem.getDistributionState());
			itemMap.put("distributionDiscountPrice", sellPageItem.getDistributionDiscountPrice());
			itemMap.put("distributionDiscountType", sellPageItem.getDistributionDiscountType());
			itemMap.put("sellPageItemConfig", sellPageItem.getSellPageItemConfig());
            itemMap.put("updatedAt",sellPageItem.getUpdatedAt().getTime());

			itemMap.put("isGroupBuy", sellPageItem.getIsGroupBuy());
			log.info("isGroupBuy:{}", sellPageItem.getIsGroupBuy());
			// 如果是拼团商品，返回该商品对应的状态为生效的所有拼团规则
			if (sellPageItem.getIsGroupBuy() == CommodityTypeEnum.GroupBuy.getKey().intValue()) {
				String beInvitedOpenId = "";
				if (!StringUtils.isEmpty(openIdFromCookie)) {
					beInvitedOpenId = openIdFromCookie;
				} else {
					beInvitedOpenId = openIdFromUrl + "";
				}
				List<Map<String,Object>> assembleRules = this.getAssembleRules(sellPageItem, beInvitedOpenId);

				itemMap.put("assembleRules", assembleRules);
//				itemMap.put("assembleRuleConfig", sellPageItem.getAssembleRuleConfig());
//				List<GroupBuyUser> userList = bigbayGroupBuyUserMapper.selectGroupBuyUserBySellPageItemId(sellPageItem.getId());
//				itemMap.put("userList", userList);
//				itemMap.put("count", bigbayGroupBuyUserMapper.selectGroupBuyUserCountBySellPageItemId(sellPageItem.getId()));
			}
			sellPageItemMap.put(sellPageName, itemMap);
		}
		String sellPageItemsJson = Tools.mapToJson(sellPageItemMap);

		//自定义购买页图片
		sellPageService.setCustomSellPageImage(sellPage);
		
		// 判断abtest是否开启。若开启根据权重随机选取一个购买页模版
		// 同一个人再次进入abtest购买页，选取的购买页模版同上次一样
		Integer sellPageTemplateId=0;
		if (SellPage.MULTITEMPENABLE.equals(sellPage.getMultiTempEnable())) {
			String sellPageTemplateIdFromCookie = getCookie(request, pageKey);
			SellPageTemplate sellPageTemplate=null;
			if (!StringUtils.isEmpty(sellPageTemplateIdFromCookie)) {
				sellPageTemplate = sellPageTemplateCacheById.getByKey(sellPageTemplateIdFromCookie);
				if(StringUtils.isEmpty(sellPageTemplate) || sellPage.getId()!=sellPageTemplate.getSellPageId()) {
					sellPageTemplate =getSellPageTemplate(sellPage.getId());
				}
			}else {
				sellPageTemplate =getSellPageTemplate(sellPage.getId());
			}
			sellPageTemplateId=sellPageTemplate.getId();
			copyProperty(sellPageTemplate,sellPage);
			saveSellPageTemplateIdToCookie(pageKey,sellPageTemplate.getId(), response);
		}
		
		// 销售页图片
		if(!StringUtils.isEmpty(sellPage.getImages())) {
			String[] split = sellPage.getImages().split(",");
			for (int i = 0; i < split.length; i++) {
				if (!split[i].startsWith("http") && !split[i].startsWith("https")) {
					split[i] = httpIp + split[i];
				}
			}
			log.info("loadImage:"+sellPage.isLoadSellPageImage());
			if(sellPage.isLoadSellPageImage()) {
				model.addAttribute("imageList", Arrays.asList(split));
			}else {
				model.addAttribute("imageList", new ArrayList<>());
				model.addAttribute("imageListJson", JSON.toJSONString(split));
			}
		}

		//代付id，如果url中包含该字段且值不为空，则不加载购买页图片
		String otherPayId = request.getParameter("otherPayId");
		if(!StringUtils.isEmpty(otherPayId)){
			model.addAttribute("imageList", new ArrayList<>());
			log.info("pageKey=【{}】======代付购买页不加载图片",pageKey);
		}
		
		// 分享图片
		String shareImage = sellPage.getShareImage();
		if (!shareImage.startsWith("http") && !shareImage.startsWith("https")) {
			shareImage = httpIp + shareImage;
		}
		model.addAttribute("sellPageItems", sellPageItemsJson);
		model.addAttribute("openIdFromCookie", openIdFromCookie);
		model.addAttribute("appId", bigbayApp.getWechatAppId());
		model.addAttribute("sellPageTitle", sellPage.getPageTitle());

		model.addAttribute("shareTitle", sellPage.getShareTitle());
		model.addAttribute("shareDesc", sellPage.getShareDesc());
		model.addAttribute("shareImage", shareImage);
		model.addAttribute("secondImages", sellPage.getSecondImages());

		//代付相关信息
		String payByAnotherShareImage = sellPage.getPayByAnotherShareImage();
		if (!payByAnotherShareImage.startsWith("http") && !payByAnotherShareImage.startsWith("https")) {
			payByAnotherShareImage = httpIp + payByAnotherShareImage;
		}
		model.addAttribute("enablePayByAnother", sellPage.getEnablePayByAnother());
		model.addAttribute("payByAnotherPayType", sellPage.getPayByAnotherPayType());
		model.addAttribute("payByAnotherShareTitle", sellPage.getPayByAnotherShareTitle());
		model.addAttribute("payByAnotherShareDesc", sellPage.getPayByAnotherShareDesc());
		model.addAttribute("payByAnotherShareImage", payByAnotherShareImage);

		String urlJsEntryToRender = request.getParameter("url-js-entry");
		if (StringUtils.isEmpty(urlJsEntryToRender)) {
			log.info("request url-js-entry is empty");
			String configJsAddr = sellPage.getConfigJsAddr();
			if(org.apache.commons.lang.StringUtils.isBlank(configJsAddr)) {
				urlJsEntryToRender = urlJsEntry;
				log.info("use env url-js-entry");
			}else {
				urlJsEntryToRender = sellPage.getConfigJsAddr();
				log.info("use db url-js-entry");
			}
			
		}
		model.addAttribute("urlJsEntry", urlJsEntryToRender);

		String starPaymentSdkJsUrl = request.getParameter("url-js-sdk-entry");
		if (StringUtils.isEmpty(starPaymentSdkJsUrl)) {
			log.info("request url-js-sdk-entry is empty");
			starPaymentSdkJsUrl = starPaymentSdkJs;
		}
		model.addAttribute("starPaymentSdkJsUrl", starPaymentSdkJsUrl);

		String customPageInfo = sellPage.getCustomPageInfo();
		if (StringUtils.isEmpty(customPageInfo)) {
			customPageInfo = "{}";
		}
		model.addAttribute("customPageInfo", customPageInfo);
		
		
		sellPageItemMap.clear();
		sellPageItemMap.put("purchasePageKey", sellPage.getPurchasePageKey());
		sellPageItemMap.put("multiTempEnable", sellPage.getMultiTempEnable());
		sellPageItemMap.put("sellPageTemplateId", sellPageTemplateId);
		sellPageItemMap.put("sellPageName", sellPage.getItemName());
		String sellPageVariableJson = Tools.mapToJson(sellPageItemMap);
		model.addAttribute("sellPageVariable", sellPageVariableJson);

		return "sellPage";
	}

	private void saveSellPageTemplateIdToCookie(String pageKey,Integer sellPageTemplateId, HttpServletResponse response) {
		Cookie cookiePageKeyTemplateId = new Cookie(pageKey, sellPageTemplateId+"");
		cookiePageKeyTemplateId.setHttpOnly(true);
		cookiePageKeyTemplateId.setMaxAge(2*365*24*60*6);
		response.addCookie(cookiePageKeyTemplateId);
	}

	private SellPage copyProperty(SellPageTemplate sellPageTemplate, SellPage sellPage) {
		// TODO Auto-generated method stub
		sellPage.setShareDesc(sellPageTemplate.getShareDesc());
		sellPage.setShareImage(sellPageTemplate.getShareImage());
		sellPage.setShareTitle(sellPageTemplate.getShareTitle());
		sellPage.setCustomPageInfo(sellPageTemplate.getCustomPageInfo());
		sellPage.setImages(sellPageTemplate.getImages());
		sellPage.setSecondImages(sellPageTemplate.getSecondImages());
		sellPage.setPurchasePageKey(sellPageTemplate.getTemplateKey());
		return sellPage;
	}



	private SellPageTemplate getSellPageTemplate(long id) throws Exception {
		List<SellPageTemplate> sellPageTemplates = sellPageTemplateCacheBySellPageId
				.getListByKey(id + "");
		if(CollectionUtils.isEmpty(sellPageTemplates)) 
			throw new Exception("ABTest sellPageTemplates is null");
		int weightSum = 0;
		SellPageTemplate sellPageTemplate=new SellPageTemplate();
		for (SellPageTemplate sellPageTemplateTemp : sellPageTemplates) {
			weightSum += sellPageTemplateTemp.getRenderRights();
		}
		int random = new Random().nextInt(weightSum);
		for (SellPageTemplate sellPageTemplateTemp : sellPageTemplates) {
			random -= sellPageTemplateTemp.getRenderRights();
			if (random < 0) {
				sellPageTemplate=sellPageTemplateTemp;
				break;
			}
		}
		return sellPageTemplate;
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/js-sdk-signature")
	@ResponseBody
	public Map<String, Object> generateJsSdkSignature(@RequestParam("noncestr") String noncestr,
			@RequestParam("timestamp") String timestamp, @RequestParam("url") String url,
			@RequestParam("appId") String appId) {
		HttpPost httpPost = new HttpPost(urlGenerateJsSdkSignature);
		Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("noncestr", noncestr);
		postParams.put("timestamp", timestamp);
		postParams.put("url", url);
		postParams.put("appId", appId);
		Tools.setHttpPostParameters(httpPost, postParams);
		HttpResponse response = null;
		String responseBody = null;
		try {
			Date d1 = new Date();
			response = httpClient.execute(httpPost);
			Date d2 = new Date();
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
			log.info("[it takes " + String.valueOf(d2.getTime() - d1.getTime()) + "] this is the body of js sdk signature: " + responseBody);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Object> ret = Tools.jsonToMap(responseBody);
		ret = (Map<String, Object>) ret.get("data");
		String signature = (String) ret.get("signature");
		Map<String, String> result = new HashMap<>();
		result.put("signature", signature);
		return Tools.s(result);
	}

	@PostMapping("/get-sellPage-url")
	@ResponseBody
	public Map<String, Object> getSellPageUrl(@RequestParam("pageKey") String pageKey) {

		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		if (sellPage == null) {
			Tools.f("根据pageKey+" + pageKey + "没有取到SellPage");
		}
		String url = sellPageDomain+"app/mall?pageKey="+pageKey;

		return Tools.s(url);
	}

	@GetMapping({
			"/app/mall",
	})
	public String appMall(HttpServletResponse response, HttpServletRequest request,
					   @RequestParam("pageKey") String pageKey, ModelMap model) throws Exception {
		log.info("======= pageKey={}", pageKey);
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, proxy-revalidate, max-age=0");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "0");

		if (request.getServerName().equals("bigbay-payment.jiguangdanci.com")) {
			if (StringUtils.isEmpty(request.getParameter("fromDistributorId"))) {
				String redirectTo = Tools.getRequestedUrl(request);
				redirectTo = redirectTo.replaceAll("//bigbay-payment.jiguangdanci.com", "//bigbay.qingclass.com");
				response.getWriter().println("<html><head><script type=\"text/javascript\">setTimeout(function () {location.replace(\"" + redirectTo + "\");}, 20);</script></head></html>");
				return "emptyTemplate";
			}
		}

		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		if (sellPage == null) {
			throw new Exception("根据pageKey+" + pageKey + "没有取到SellPage");
		}

		String unionIdFromUrl = request.getParameter("unionId");
		String openIdFromUrl = request.getParameter("openId");

		if (StringUtils.isEmpty(openIdFromUrl) || StringUtils.isEmpty(unionIdFromUrl)) {
			model.addAttribute("message","购买页加载失败!");
			return "sellPageInvalid";
		}

		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());

		if(!Constant.USER_NOT_SIGNIN.equals(openIdFromUrl) && !Constant.USER_NOT_SIGNIN.equals(unionIdFromUrl)) {
			//维护用户信息
			try {
				wechatUsersService.maintainUserInfo(bigbayApp, unionIdFromUrl, openIdFromUrl,null,null,null);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		

		List<SellPageItem> sellPageItems = sellPageItemCacheBySellPageId.getListByKey(String.valueOf(sellPage.getId()));

		if ("2".equals(sellPage.getState()) || CollectionUtils.isEmpty(sellPageItems)) {
			model.addAttribute("message","对不起，该商品已下架！");
			return "sellPageInvalid";
		}



		Map<String, Object> sellPageItemMap = new LinkedHashMap<>();
		for (SellPageItem sellPageItem : sellPageItems) {
			String sellPageName = sellPageItem.getName();
			Map<String, Object> itemMap = new HashMap<>();
			itemMap.put("sellPageItemId", sellPageItem.getId());
			itemMap.put("name", sellPageName);
			itemMap.put("price", sellPageItem.getPrice());
			itemMap.put("distributionDiscount", sellPageItem.getDistributionDiscount());
			itemMap.put("toUrl", sellPageItem.getToUrl());
			itemMap.put("distributionState", sellPageItem.getDistributionState());
			itemMap.put("distributionDiscountPrice", sellPageItem.getDistributionDiscountPrice());
			itemMap.put("distributionDiscountType", sellPageItem.getDistributionDiscountType());
			itemMap.put("sellPageItemConfig", sellPageItem.getSellPageItemConfig());
			itemMap.put("updatedAt",sellPageItem.getUpdatedAt().getTime());
			long id = sellPageItem.getId();
			
			List<BigbayIapItem> list = bigbayIapItemMapper.selectByBigbayItemId(id);
			if(list != null && list.size() > 0) {
				BigbayIapItem bigbayIapItem = list.get(0);
				Integer num = bigbayIapItem.getNum();
				Long iapItemId = bigbayIapItem.getIapItemId();
				itemMap.put("iapItemNum", num);
				itemMap.put("iapItemId", iapItemId);
			}
			
			sellPageItemMap.put(sellPageName, itemMap);
		}
		String sellPageItemsJson = Tools.mapToJson(sellPageItemMap);

		//自定义购买页图片
		sellPageService.setCustomSellPageImage(sellPage);
		
		// 判断abtest是否开启。若开启根据权重随机选取一个购买页模版
		// 同一个人再次进入abtest购买页，选取的购买页模版同上次一样
		Integer sellPageTemplateId=0;
		if (SellPage.MULTITEMPENABLE.equals(sellPage.getMultiTempEnable())) {
			String sellPageTemplateIdFromCookie = getCookie(request, pageKey);
			SellPageTemplate sellPageTemplate=null;
			if (!StringUtils.isEmpty(sellPageTemplateIdFromCookie)) {
				sellPageTemplate = sellPageTemplateCacheById.getByKey(sellPageTemplateIdFromCookie);
				if(StringUtils.isEmpty(sellPageTemplate) || sellPage.getId()!=sellPageTemplate.getSellPageId()) {
					sellPageTemplate =getSellPageTemplate(sellPage.getId());
				}
			}else {
				sellPageTemplate =getSellPageTemplate(sellPage.getId());
			}
			sellPageTemplateId=sellPageTemplate.getId();
			copyProperty(sellPageTemplate,sellPage);
			saveSellPageTemplateIdToCookie(pageKey,sellPageTemplate.getId(), response);
		}

		// 销售页图片
		if(!StringUtils.isEmpty(sellPage.getImages())) {
			String[] split = sellPage.getImages().split(",");
			for (int i = 0; i < split.length; i++) {
				if (!split[i].startsWith("http") && !split[i].startsWith("https")) {
					split[i] = httpIp + split[i];
				}
			}
			log.info("loadImage:"+sellPage.isLoadSellPageImage());
			if(sellPage.isLoadSellPageImage()) {
				model.addAttribute("imageList", Arrays.asList(split));
			}else {
				model.addAttribute("imageList", new ArrayList<>());
				model.addAttribute("imageListJson", JSON.toJSONString(split));
			}
		}

		//代付id，如果url中包含该字段且值不为空，则不加载购买页图片
		String otherPayId = request.getParameter("otherPayId");
		if(!StringUtils.isEmpty(otherPayId)){
			model.addAttribute("imageList", new ArrayList<>());
			log.info("pageKey=【{}】======代付购买页不加载图片",pageKey);
		}

		// 分享图片
		String shareImage = sellPage.getShareImage();
		if (!shareImage.startsWith("http") && !shareImage.startsWith("https")) {
			shareImage = httpIp + shareImage;
		}
		model.addAttribute("sellPageItems", sellPageItemsJson);
		model.addAttribute("openIdFromCookie", openIdFromUrl);
		model.addAttribute("appId", bigbayApp.getWechatAppId());
		model.addAttribute("sellPageTitle", sellPage.getPageTitle());

		model.addAttribute("shareTitle", sellPage.getShareTitle());
		model.addAttribute("shareDesc", sellPage.getShareDesc());
		model.addAttribute("shareImage", shareImage);
		model.addAttribute("secondImages", sellPage.getSecondImages());

		//代付相关信息
		String payByAnotherShareImage = sellPage.getPayByAnotherShareImage();
		if (!payByAnotherShareImage.startsWith("http") && !payByAnotherShareImage.startsWith("https")) {
			payByAnotherShareImage = httpIp + payByAnotherShareImage;
		}
		model.addAttribute("enablePayByAnother", sellPage.getEnablePayByAnother());
		model.addAttribute("payByAnotherPayType", sellPage.getPayByAnotherPayType());
		model.addAttribute("payByAnotherShareTitle", sellPage.getPayByAnotherShareTitle());
		model.addAttribute("payByAnotherShareDesc", sellPage.getPayByAnotherShareDesc());
		model.addAttribute("payByAnotherShareImage", payByAnotherShareImage);

		String urlJsEntryToRender = request.getParameter("url-js-entry");
		if (StringUtils.isEmpty(urlJsEntryToRender)) {
			log.info("request url-js-entry is empty");
			String configJsAddr = sellPage.getConfigJsAddr();
			if(org.apache.commons.lang.StringUtils.isBlank(configJsAddr)) {
				urlJsEntryToRender = urlJsEntry;
				log.info("use env url-js-entry");
			}else {
				urlJsEntryToRender = sellPage.getConfigJsAddr();
				log.info("use db url-js-entry");
			}

		}
		model.addAttribute("urlJsEntry", urlJsEntryToRender);

		String starPaymentSdkJsUrl = request.getParameter("url-js-sdk-entry");
		if (StringUtils.isEmpty(starPaymentSdkJsUrl)) {
			log.info("request url-js-sdk-entry is empty");
			starPaymentSdkJsUrl = starPaymentSdkJs;
		}
		model.addAttribute("starPaymentSdkJsUrl", starPaymentSdkJsUrl);

		String customPageInfo = sellPage.getCustomPageInfo();
		if (StringUtils.isEmpty(customPageInfo)) {
			customPageInfo = "{}";
		}
		model.addAttribute("customPageInfo", customPageInfo);


		sellPageItemMap.clear();
		sellPageItemMap.put("purchasePageKey", sellPage.getPurchasePageKey());
		sellPageItemMap.put("multiTempEnable", sellPage.getMultiTempEnable());
		sellPageItemMap.put("sellPageTemplateId", sellPageTemplateId);
		sellPageItemMap.put("sellPageName", sellPage.getItemName());
		String sellPageVariableJson = Tools.mapToJson(sellPageItemMap);
		model.addAttribute("sellPageVariable", sellPageVariableJson);

		return "sellPage";
	}


	@GetMapping({
			"/mall/transfer",
	})
	public String mallTransfer(HttpServletResponse response, HttpServletRequest request,
						  @RequestParam("pageKey") String pageKey, ModelMap model) throws Exception {
		log.info("======= pageKey={}", pageKey);
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, proxy-revalidate, max-age=0");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "0");

		if (request.getServerName().equals("bigbay-payment.jiguangdanci.com")) {
			if (StringUtils.isEmpty(request.getParameter("fromDistributorId"))) {
				String redirectTo = Tools.getRequestedUrl(request);
				redirectTo = redirectTo.replaceAll("//bigbay-payment.jiguangdanci.com", "//bigbay.qingclass.com");
				response.getWriter().println("<html><head><script type=\"text/javascript\">setTimeout(function () {location.replace(\"" + redirectTo + "\");}, 20);</script></head></html>");
				return "emptyTemplate";
			}
		}

		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		if (sellPage == null) {
			throw new Exception("根据pageKey:【" + pageKey + "】没有取到SellPage");
		}


		List<SellPageItem> sellPageItems = sellPageItemCacheBySellPageId.getListByKey(String.valueOf(sellPage.getId()));

		if ("2".equals(sellPage.getState()) || CollectionUtils.isEmpty(sellPageItems)) {
			model.addAttribute("message","对不起，该商品已下架！");
			return "sellPageInvalid";
		}

		String businessSellPageUrl = sellPage.getBusinessSellPageUrl();
		model.addAttribute("businessSellPageUrl", businessSellPageUrl);

		return "redirectBusinessPage";
	}

	/**
	 * 查询拼团商品有效的拼团规则
	 * @param sellPageItem
	 * @param beInvitedOpenId
	 * @return
	 */
	private List<Map<String,Object>> getAssembleRules (SellPageItem sellPageItem, String beInvitedOpenId){
		List<AssembleRule> assembleRuleList = assembleRuleCacheBySellPageItemId.getListByKey(sellPageItem.getId() + "");

		if (null == assembleRuleList || assembleRuleList.isEmpty()) {
		    return null;
        }
		List<Map<String,Object>> assembleRules = new ArrayList<>();
		boolean isActive = false;
		for (AssembleRule assembleRule : assembleRuleList) {
			if ("1".equals(assembleRule.getStatus() + "")) {
				Map<String,Object> assembleRuleMap = Maps.newHashMap();
				assembleRuleMap.put("id", assembleRule.getId());
				assembleRuleMap.put("activityName", assembleRule.getId());
				assembleRuleMap.put("peopleNumber", assembleRule.getPeopleNumber());
				assembleRuleMap.put("assembleCycle", assembleRule.getAssembleCycle());
				assembleRuleMap.put("assemblingRule", assembleRule.getAssemblingRule());
				assembleRuleMap.put("activityStartTime", assembleRule.getActivityStartTime().getTime());
				assembleRuleMap.put("activityEndTime", assembleRule.getActivityEndTime().getTime());
				assembleRuleMap.put("shareFriendCycleTitle", assembleRule.getShareFriendCycleTitle());
				assembleRuleMap.put("shareFriendDesc", assembleRule.getShareFriendDesc());
				assembleRuleMap.put("shareFriendTitle", assembleRule.getShareFriendTitle());
				assembleRuleMap.put("shareIcon", assembleRule.getShareIcon());
				assembleRuleMap.put("isActive", false);

				// 判断当前活跃的拼团规则,同一时刻只能有一个活跃的拼团规则
				if (!isActive) {
					long nowTime = System.currentTimeMillis();
					if ((nowTime + "").compareTo(assembleRule.getActivityStartTime().getTime() + "") >= 0 && (nowTime + "").compareTo(assembleRule.getActivityEndTime().getTime() + "") < 0) {
						isActive = true;
						assembleRuleMap.put("isActive", true);
					}
				}

				assembleRuleMap.put("customInfo", assembleRule.getCustomInfo());

				// 根据openid查询当前用户在这个拼团规则下的订单情况
				List<GroupBuyUser> groupBuyUserList =  bigbayGroupBuyUserMapper.selectByBeInvitedOpenIdAndAssembleRuleId(beInvitedOpenId,sellPageItem.getId(),assembleRule.getId());
				List<Map<String,Object>> activityList = new ArrayList<>();
				for (GroupBuyUser groupBuyUser : groupBuyUserList) {
					Map<String,Object> activityMap = Maps.newHashMap();
					long groupBuyActivityId = groupBuyUser.getAssembleActivityId();
					activityMap.put("groupBuyActivityId", groupBuyActivityId);

                    activityList.add(activityMap);
				}

				assembleRuleMap.put("groupBuyActivities", activityList);

				assembleRules.add(assembleRuleMap);
			}

		}

		return assembleRules;
	}


}
