package com.qingclass.bigbay.controller;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qingclass.bigbay.annotation.SignatureVerify;
import com.qingclass.bigbay.service.SellConfigService;
import com.qingclass.bigbay.tool.Tools;


/**
 * @author lijiecai
 * @description: 外部调用接口修改购买页和商品基础信息
 * @date 2019-09-06 15:03
 */
@RestController
@RequestMapping("/outer")
public class SellConfigController {

	private static final Logger logger = LoggerFactory.getLogger(SellConfigController.class);
	
	

	@Autowired
	private SellConfigService sellConfigService;
	
	/**
	 * 
	 * 校验name是否重复
	 */
	@PostMapping("/item/checkName")
    @SignatureVerify
	public Object checkName(@RequestParam(value = "bigbayAppId") String bigbayAppId,
			@RequestParam(value = "content") String content, @RequestParam(value = "random") String random,
			@RequestParam(value = "timestamp") String timestamp, @RequestParam(value = "signature") String signature) {
		
		
		logger.info("【content={}】", content);
		
		JSONObject  jsonObject = JSONObject.parseObject(content);
	    //json对象转Map
	    Map<String,Object> map = (Map<String,Object>)jsonObject;
	    String name = (String)map.get("name");
		if(StringUtils.isEmpty(name)) {
			return Tools.f(null, 400, "name missing");
		}
		Integer id = (Integer) map.get("id");
	    int count = sellConfigService.checkItemName(name, id);
	    if(count >= 1) {
			return Tools.f(null, 400, "requestParam name repeat");
	    }
	    return Tools.s(null);
	}


	/**
	 * @author wms
	 * 
	 * 
	 * <br>添加商品</br>
	 * <b>给业务线添加商品的rpc接口</b>
	 * @return
	 * <li>401签名校验失败</li>
	 * <li>400缺失参数或者参数有错误</li>
	 * 
	 */
	@PostMapping("/item/saveItem")
    @SignatureVerify
	public Object saveItem(@RequestParam(value = "bigbayAppId") String bigbayAppId,
			@RequestParam(value = "content") String content, @RequestParam(value = "random") String random,
			@RequestParam(value = "timestamp") String timestamp, @RequestParam(value = "signature") String signature)
			throws Exception {

		
		JSONObject  jsonObject = JSONObject.parseObject(content);
	    //json对象转Map
	    Map<String,Object> map = (Map<String,Object>)jsonObject;

		return sellConfigService.saveItem(map);
	}
	
	/**
	 * 
	 * 
	 * <br>添加多个商品</br>
	 * 
	 */
	@PostMapping("/item/addItems")
    @SignatureVerify
	public Object addItems(@RequestParam(value = "bigbayAppId") String bigbayAppId,
			@RequestParam(value = "content") String content, @RequestParam(value = "random") String random,
			@RequestParam(value = "timestamp") String timestamp, @RequestParam(value = "signature") String signature)
			throws Exception {
		logger.info("======/item/addItems========content:{}", content);
		
		return sellConfigService.addItems(content);
	}
	
	
	
	/**
	 * @author wms
	 * 
	 * 
	 * <br>编辑商品</br>
	 * <b>给业务线编辑商品的rpc接口</b>
	 * @return
	 * <li>401签名校验失败</li>
	 * <li>400缺失参数或者参数有错误</li>
	 * 
	 */
	@PostMapping("/item/updateItem")
    @SignatureVerify
	public Object updateItem(@RequestParam(value = "bigbayAppId") String bigbayAppId,
			@RequestParam(value = "content") String content, @RequestParam(value = "random") String random,
			@RequestParam(value = "timestamp") String timestamp, @RequestParam(value = "signature") String signature)
			throws Exception {

		
		JSONObject  jsonObject = JSONObject.parseObject(content);
	    //json对象转Map
	    Map<String,Object> map = (Map<String,Object>)jsonObject;

		return sellConfigService.updateItem(map);
	}
	

	@PostMapping("/item/itemList")
    @SignatureVerify
	public Object itemList(@RequestParam(value = "bigbayAppId") String bigbayAppId,
			@RequestParam(value = "content") String content, @RequestParam(value = "random") String random,
			@RequestParam(value = "timestamp") String timestamp, @RequestParam(value = "signature") String signature) {
		
		
		logger.info("【content={}】", content);
		
		JSONObject  jsonObject = JSONObject.parseObject(content);
	    //json对象转Map
	    Map<String,Object> map = (Map<String,Object>)jsonObject;
	    Integer sellPageId = (Integer)map.get("sellPageId");
		if(null == sellPageId) {
			return Tools.f(null, 400, "sellPageId missing");
		}
		
		Integer pageSize = (Integer) map.get("pageSize");
        Integer pageNo = (Integer) map.get("pageNo");
	    return sellConfigService.itemList(sellPageId.longValue(), pageNo, pageSize);
	}


    /**
     * 新增购买页
     * @param bigbayAppId 业务端在海湾的项目id
     * @param content 请求的具体内容
     * @param random 随机字符串
     * @param timestamp 时间戳
     * @param signature 签名
     * @return
     */
    @PostMapping("/sell-page/add")
    @SignatureVerify
    public Object addSellPage(@RequestParam(value = "bigbayAppId")String bigbayAppId,
                              @RequestParam(value = "content")String content,
                              @RequestParam(value = "random")String random,
                              @RequestParam(value = "timestamp")String timestamp,
                              @RequestParam(value = "signature")String signature){
		logger.info("【content={}】", content);
        return sellConfigService.addSellPage(bigbayAppId,content);
    }

    /**
     * 编辑购买页
     * @param bigbayAppId 业务端在海湾的项目id
     * @param content 请求的具体内容
     * @param random 随机字符串
     * @param timestamp 时间戳
     * @param signature 签名
     * @return
     */
    @PostMapping("/sell-page/edit")
    @SignatureVerify
    public Object editSellPage(@RequestParam(value = "bigbayAppId")String bigbayAppId,
                              @RequestParam(value = "content")String content,
                              @RequestParam(value = "random")String random,
                              @RequestParam(value = "timestamp")String timestamp,
                              @RequestParam(value = "signature")String signature){
		logger.info("【content={}】", content);
        return sellConfigService.editSellPage(bigbayAppId,content);
    }

    /**
     * 购买页列表
     * @param bigbayAppId 业务端在海湾的项目id
     * @param content 请求的具体内容
     * @param random 随机字符串
     * @param timestamp 时间戳
     * @param signature 签名
     * @return
     */
    @PostMapping("/sell-page/list")
    @SignatureVerify
    @SuppressWarnings("unchecked")
    public Object listSellPage(@RequestParam(value = "bigbayAppId")String bigbayAppId,
                               @RequestParam(value = "content")String content,
                               @RequestParam(value = "random")String random,
                               @RequestParam(value = "timestamp")String timestamp,
                               @RequestParam(value = "signature")String signature){
		logger.info("【content={}】", content);
    	Map<String, Object> paramsMap =  (Map) JSON.parse(content);

        return sellConfigService.listSellPage(bigbayAppId,paramsMap);
    }

	/**
	 * 购买页信息
	 * @param bigbayAppId 业务端在海湾的项目id
	 * @param content 请求的具体内容
	 * @param random 随机字符串
	 * @param timestamp 时间戳
	 * @param signature 签名
	 * @return
	 */
	@PostMapping("/sell-page/info")
	@SignatureVerify
	@SuppressWarnings("unchecked")
	public Object sellPageInfo(@RequestParam(value = "bigbayAppId")String bigbayAppId,
							   @RequestParam(value = "content")String content,
							   @RequestParam(value = "random")String random,
							   @RequestParam(value = "timestamp")String timestamp,
							   @RequestParam(value = "signature")String signature){
		logger.info("【content={}】", content);
		Map<String, Object> paramsMap =  (Map) JSON.parse(content);

		return sellConfigService.sellPageInfo(paramsMap);
	}

	/**
	 * 建立学员与班长的分销关系
	 * @param bigbayAppId
	 * @param content
	 * @param random
	 * @param timestamp
	 * @param signature
	 * @return
	 */
	@PostMapping("/create-distribution-relation")
	@SignatureVerify
    public Object createDistributionRelation(@RequestParam(value = "bigbayAppId")String bigbayAppId,
											   @RequestParam(value = "content")String content,
											   @RequestParam(value = "random")String random,
											   @RequestParam(value = "timestamp")String timestamp,
											   @RequestParam(value = "signature")String signature) throws Exception {

		Map<String, Object> mapParams = (Map) JSON.parse(content);
		return sellConfigService.createDistributionRelation(bigbayAppId, mapParams);
	}

	/**
	 *
	 * 校验购买页名称是否重复
	 */
	@PostMapping("/sell-page/checkName")
	@SignatureVerify
	public Object checkSellPageName(@RequestParam(value = "bigbayAppId") String bigbayAppId,
							@RequestParam(value = "content") String content, @RequestParam(value = "random") String random,
							@RequestParam(value = "timestamp") String timestamp, @RequestParam(value = "signature") String signature) {


		logger.info("【content={}】", content);

		JSONObject  jsonObject = JSONObject.parseObject(content);
		//json对象转Map
		Map<String,Object> map = (Map<String,Object>)jsonObject;
		String sellPageName = (String)map.get("itemName");
		if(StringUtils.isEmpty(sellPageName)) {
			return Tools.f(null, 400, "itemName missing");
		}
		Integer id = (Integer) map.get("id");
		int count = sellConfigService.checkSellPageName(sellPageName, id);
		if(count >= 1) {
			return Tools.f(null, 400, "requestParam itemName repeat");
		}
		return Tools.s(null);
	}


	@PostMapping("/check-price")
	@SignatureVerify
	public Object checkPriceByPageKey(@RequestParam(value = "bigbayAppId") String bigbayAppId,
									  @RequestParam(value = "content") String content, @RequestParam(value = "random") String random,
									  @RequestParam(value = "timestamp") String timestamp, @RequestParam(value = "signature") String signature){

		logger.info("【content={}】", content);

		Map<String, Object> paramsMap = null;
		try {
			paramsMap = (Map) JSON.parse(content);
		}catch (Exception e){
			e.printStackTrace();
			Tools.f(null,400,"content格式不正确");
		}

		String pageKeys = (String) paramsMap.get("pageKeys");
		if(StringUtils.isEmpty(pageKeys)){
			return Tools.f(null,400,"pageKeys不能为空");
		}
		return sellConfigService.checkPriceByPageKey(pageKeys);


	}
	/**
	 * 查询商品详情
	 */
	@PostMapping("/item/getItemDetail")
    @SignatureVerify
	public Object getItemDetail(@RequestParam(value = "bigbayAppId") String bigbayAppId,
			@RequestParam(value = "content") String content, @RequestParam(value = "random") String random,
			@RequestParam(value = "timestamp") String timestamp, @RequestParam(value = "signature") String signature)
			throws Exception {

		
		JSONObject  jsonObject = JSONObject.parseObject(content);
	    //json对象转Map
	    Map<String,Object> map = (Map<String,Object>)jsonObject;

		return sellConfigService.getItemDetail(map);
	}

	
	
	
	
	

}
