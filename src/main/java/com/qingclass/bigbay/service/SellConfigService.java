package com.qingclass.bigbay.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Validator;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.SellPageItemCacheBySellPageId;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.entity.zebra.ZebraDistributors;
import com.qingclass.bigbay.exception.JsonErrException;
import com.qingclass.bigbay.exception.ParamMissException;
import com.qingclass.bigbay.mapper.config.SellPageItemMapper;
import com.qingclass.bigbay.mapper.config.SellPageMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.mapper.zebra.ZebraDistributorsMapper;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.price.SellPageItemPricePipeManager;
import com.qingclass.bigbay.tool.BeanValidators;
import com.qingclass.bigbay.tool.Tools;


@Service
public class SellConfigService {
	
	private static final Logger log = LoggerFactory.getLogger(SellConfigService.class); 
	@Autowired
	private Validator validator;

	@Autowired
	private SellPageItemMapper sellPageItemMapper;
	//@Autowired
	//private SellPageCacheById sellPageCacheById;

    @Autowired
    private SellPageMapper sellPageMapper;

    //@Autowired
    //private SellPageCacheByPageKey sellPageCacheByPageKey;

    @Autowired
    private SellPageItemCacheBySellPageId sellPageItemCacheBySellPageId;

	@Autowired
	private SellPageItemPricePipeManager sellPageItemPricePipeManager;


    @Autowired
    private WechatUsersService wechatUsersService;

    @Autowired
    private BigbayAppCacheById bigbayAppCacheById;


    @Autowired
    private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;

    @Autowired
    private BiabaySalesService biabaySalesService;

    @Autowired
    private ZebraDistributorsMapper zebraDistributorsMapper;



    /*
     * distributionDescribe 分销描述
     * distributionDiscountType 分销折扣类型0.比例 1.手动输入
     * distributionDiscount 分销折扣
     * distributionDiscountPrice 折扣价
     * distributionPercentage 分销提成比例
     * distributionState 分销状态,0代表失效，1代表有效
     * 
     */

	@Transactional("configTransactionManager")
	public Map<String, Object> saveItem(Map<String, Object> map) {

		// 必填参数校验
		String name = (String) map.get("name");
		if (StringUtils.isEmpty(name)) {
			return Tools.f(null, 400, "requestParam name missing");
		}
		//name 不能重复校验
		int countByName = sellPageItemMapper.countByName(name, null);
		if(countByName >= 1) {
			return Tools.f(null, 400, "requestParam name repeat");
		}
		String toUrl = (String) map.get("toUrl");
		if (StringUtils.isEmpty(toUrl)) {
			return Tools.f(null, 400, "requestParam toUrl missing");
		}
		
		String state = (String) map.get("state");
		if(StringUtils.isEmpty(state)) {
			return Tools.f(null, 400, "requestParam state missing");
		}
		
		Integer isHiddenOnZebra = (Integer) map.get("isHiddenOnZebra");
		if(null == isHiddenOnZebra) {
			return Tools.f(null, 400, "requestParam isHiddenOnZebra missing");
		}
		List<Integer> asList = Arrays.asList(0, 1);
		if(!asList.contains(isHiddenOnZebra)) {
			return Tools.f(null, 400, "requestParam isHiddenOnZebra err");
		}
		
		Integer sellPageId = (Integer) map.get("sellPageId");
		if (null == sellPageId) {
			return Tools.f(null, 400, "requestParam sellPageId missing");
		}
		String description = (String) map.get("description");
		if (StringUtils.isEmpty(description)) {
			return Tools.f(null, 400, "requestParam description missing");
		}
		String callbackConfig = (String) map.get("callbackConfig");
		if (StringUtils.isEmpty(callbackConfig)) {
			return Tools.f(null, 400, "requestParam callbackConfig missing");
		}
		Integer price = (Integer) map.get("price");
		if (null == price) {
			return Tools.f(null, 400, "requestParam price missing");
		}
		if(price < 0) {
			return Tools.f(null, 400, "requestParam price err");
		}
		
		Integer distributionDiscountType = (Integer) map.get("distributionDiscountType");
		if(distributionDiscountType != null) {
			if(!asList.contains(distributionDiscountType)) {
				return Tools.f(null, 400, "requestParam distributionDiscountType err");
			}
		}
		Integer distributionState = (Integer) map.get("distributionState");
		if(distributionState != null) {
			if(!asList.contains(distributionState)) {
				return Tools.f(null, 400, "requestParam distributionState err");
			}
		}
		Integer distributionDiscount = (Integer) map.get("distributionDiscount");
		if (distributionDiscount != null) {
			if(distributionDiscount < 0 || distributionDiscount > 100) {
				return Tools.f(null, 400, "requestParam distributionDiscount err");
			}
		}
		Integer distributionPercentage = (Integer) map.get("distributionPercentage");
		if (distributionPercentage != null) {
			if(distributionPercentage < 0 || distributionPercentage > 100) {
				return Tools.f(null, 400, "requestParam distributionPercentage err");
			}
		}
		Integer distributionDiscountPrice = (Integer) map.get("distributionDiscountPrice");
		if (distributionDiscountPrice != null) {
			if(distributionDiscountPrice < 0 ) {
				return Tools.f(null, 400, "requestParam distributionDiscountPrice err");
			}
			if (distributionDiscountPrice > price) {
				return Tools.f(null, 400, "requestParam distributionDiscountPrice err");
			}
		}

		SellPage sellPage = sellPageMapper.selectByPrimaryKey(sellPageId.longValue());
		//SellPage sellPage = sellPageCacheById.getByKey(sellPageId.toString());

		if (null == sellPage) {
			return Tools.f(null, 400, "sellpageid err");
		}

		map.put("itemBody", map.get("name"));// 补全数据

		sellPageItemMapper.insert(map);
		
		Long id = (Long) map.get("id");
		log.info("=========id is {}", id);
		Map<String, Object> data = Maps.newHashMap();
		data.put("bigbayItemId", id);
		return Tools.s(data);

	}

	@Transactional("configTransactionManager")
	public Map<String, Object> updateItem(Map<String, Object> map) throws Exception {

		// 必填参数校验
		String name = (String) map.get("name");
		if (StringUtils.isEmpty(name)) {
			return Tools.f(null, 400, "requestParam name missing");
		}
		
		String toUrl = (String) map.get("toUrl");
		if (StringUtils.isEmpty(toUrl)) {
			return Tools.f(null, 400, "requestParam toUrl missing");
		}
		Integer sellPageId = (Integer) map.get("sellPageId");
		if (null == sellPageId) {
			return Tools.f(null, 400, "requestParam sellPageId missing");
		}
		Integer id = (Integer) map.get("id");
		if (null == id) {
			return Tools.f(null, 400, "requestParam id missing");
		}
		//name 不能重复校验
		int countByName = sellPageItemMapper.countByName(name, id.longValue());
		if(countByName >= 1) {
			return Tools.f(null, 400, "requestParam name repeat");
		}
		String description = (String) map.get("description");
		if (StringUtils.isEmpty(description)) {
			return Tools.f(null, 400, "requestParam description missing");
		}
		String callbackConfig = (String) map.get("callbackConfig");
		if (StringUtils.isEmpty(callbackConfig)) {
			return Tools.f(null, 400, "requestParam callbackConfig missing");
		}
		Integer price = (Integer) map.get("price");
		if (null == price) {
			return Tools.f(null, 400, "requestParam price missing");
		}
		if(price < 0) {
			return Tools.f(null, 400, "requestParam price err");
		}
		
		String state = (String) map.get("state");
		if(StringUtils.isEmpty(state)) {
			return Tools.f(null, 400, "requestParam state missing");
		}
		
		Integer isHiddenOnZebra = (Integer) map.get("isHiddenOnZebra");
		if(null == isHiddenOnZebra) {
			return Tools.f(null, 400, "requestParam isHiddenOnZebra missing");
		}
		List<Integer> asList = Arrays.asList(0, 1);
		if(!asList.contains(isHiddenOnZebra)) {
			return Tools.f(null, 400, "requestParam isHiddenOnZebra err");
		}

		// 校验sellpageid是否正确
		SellPage sellPage = sellPageMapper.selectByPrimaryKey(sellPageId.longValue());
		if (null == sellPage) {
			return Tools.f(null, 400, "sellpageid err");
		}
		// 校验id是否正确
		SellPageItem sellPageItem = sellPageItemMapper.selectByPrimaryKey(id);
		if (null == sellPageItem) {
			return Tools.f(null, 400, "id err");
		}
		
		Integer distributionDiscountType = (Integer) map.get("distributionDiscountType");
		if(distributionDiscountType != null) {
			if(!asList.contains(distributionDiscountType)) {
				return Tools.f(null, 400, "requestParam distributionDiscountType err");
			}
		}
		Integer distributionState = (Integer) map.get("distributionState");
		if(distributionState != null) {
			if(!asList.contains(distributionState)) {
				return Tools.f(null, 400, "requestParam distributionState err");
			}
		}
		Integer distributionDiscount = (Integer) map.get("distributionDiscount");
		if (distributionDiscount != null) {
			if(distributionDiscount < 0 || distributionDiscount > 100) {
				return Tools.f(null, 400, "requestParam distributionDiscount err");
			}
		}
		Integer distributionPercentage = (Integer) map.get("distributionPercentage");
		if (distributionPercentage != null) {
			if(distributionPercentage < 0 || distributionPercentage > 100) {
				return Tools.f(null, 400, "requestParam distributionPercentage err");
			}
		}
		Integer distributionDiscountPrice = (Integer) map.get("distributionDiscountPrice");
		if (distributionDiscountPrice != null) {
			if(distributionDiscountPrice < 0 ) {
				return Tools.f(null, 400, "requestParam distributionDiscountPrice err");
			}
			if (distributionDiscountPrice > price) {
				return Tools.f(null, 400, "requestParam distributionDiscountPrice err");
			}
		}

		sellPageItem.setItemBody(name);
		sellPageItem.setName(name);
		sellPageItem.setPrice(price);
		sellPageItem.setDescription(description);
		sellPageItem.setState(state);
		sellPageItem.setToUrl(toUrl);
		sellPageItem.setCallbackConfig(callbackConfig);
		sellPageItem.setSellPageId(sellPageId);
		sellPageItem.setRemark((String) map.get("remark"));
		sellPageItem.setIsHiddenOnZebra(isHiddenOnZebra);
		sellPageItem.setSellPageItemConfig((String) map.get("sellPageItemConfig"));
		sellPageItem.setHuabeiPaySuccessToUrl((String) map.get("huabeiPaySuccessToUrl"));
		Map<String, Object> params = PropertyUtils.describe(sellPageItem);
		//设置分销信息
		params.put("distributionDescribe", (String)map.get("distributionDescribe"));//分销描述
		params.put("distributionDiscountType", (Integer)map.get("distributionDiscountType"));
		params.put("distributionDiscount", (Integer)map.get("distributionDiscount"));
		params.put("distributionDiscountPrice", (Integer)map.get("distributionDiscountPrice"));
		params.put("distributionPercentage", (Integer)map.get("distributionPercentage"));
		params.put("distributionState", (Integer)map.get("distributionState"));
		
		
		
		sellPageItemMapper.update(params);
		Map<String, Object> data = Maps.newHashMap();
		data.put("bigbayItemId", id);
		return Tools.s(data);
	}
	
	

	public Map<String, Object> itemList(Long sellPageId, Integer pageNo, Integer pageSize) {
		Integer startRow = null;
		Integer endRow = null;
		if(pageNo != null && pageSize != null) {
			startRow = (pageNo - 1) * pageSize;
			endRow = pageNo * pageSize;
		}
		
		List<SellPageItem> sellPageItems = sellPageItemMapper.selectBySellPageIdWithPage(sellPageId, startRow, endRow);
		
		return Tools.s(sellPageItems);
	}

    /**
     * 新增购买页信息
     * @param bigbayAppId 业务端在海湾的项目id
     * @param content 海湾接收到业务端传递的参数
     * @return
     */
    public Map<String,Object> addSellPage(String bigbayAppId, String content){
        SellPage sellPage = JSON.parseObject(content, SellPage.class);
        Map<String, Object> data = Maps.newHashMap();
        try {
            Map<String, Object> verifyMap = verifySellPageParams(content,null);
            if ("true".equals(verifyMap.get("success").toString())) {
                if (StringUtils.isEmpty(sellPage.getCustomPageInfo())) {
                    sellPage.setCustomPageInfo("{}");
                }
                sellPage.setBigbayAppId(Long.parseLong(bigbayAppId));
                sellPage.setPageKey(getRandomString(8));


                sellPage.setMultiTempEnable(0);
                sellPage.setSellPageType(100);
                sellPage.setCategory("");
                sellPage.setDefaultPlan(0);

                if(sellPage.getShareImage() == null){
                    sellPage.setShareImage("");
                }
                if(sellPage.getShareDesc() == null){
                    sellPage.setShareDesc("");
                }
                if(sellPage.getShareTitle() == null){
                    sellPage.setShareTitle("");
                }

                if(sellPage.getImages() == null){
                	sellPage.setImages("");
				}

                if(sellPage.getIsLoadByHaiwan() == null){
                    sellPage.setIsLoadByHaiwan(true);
                }

				if(sellPage.getBusinessSellPageUrl() == null){
					sellPage.setBusinessSellPageUrl("");
				}


                sellPageMapper.insert(sellPage);
                data.put("id", sellPage.getId());
                data.put("pageKey", sellPage.getPageKey());
            } else {
                return verifyMap;
            }
        }catch (Exception e){
            e.printStackTrace();
            return Tools.f(null,999, "添加失败");
        }

        return Tools.s(data);
    }

    /**
     * 编辑购买页信息
     * @param bigbayAppId 业务端在海湾的项目id
     * @param content 海湾接收到业务端传递的参数
     * @return
     */
    public Map<String,Object> editSellPage(String bigbayAppId, String content){
        SellPage sellPage = JSON.parseObject(content, SellPage.class);
        Map<String, Object> data = Maps.newHashMap();
        if( 0 == sellPage.getId()){
            return Tools.f(null, 400,"购买页id不能为空");
        }

        try {
            Map<String, Object> verifyMap = verifySellPageParams(content,Long.valueOf(sellPage.getId()));
            if ("true".equals(verifyMap.get("success").toString())) {
                if (StringUtils.isEmpty(sellPage.getCustomPageInfo())) {
                    sellPage.setCustomPageInfo("{}");
                }
                sellPage.setBigbayAppId(Long.parseLong(bigbayAppId));

                sellPage.setMultiTempEnable(0);
                sellPage.setSellPageType(100);
                sellPage.setCategory("");
                sellPage.setDefaultPlan(0);

                if(sellPage.getShareImage() == null){
                    sellPage.setShareImage("");
                }
                if(sellPage.getShareDesc() == null){
                    sellPage.setShareDesc("");
                }
                if(sellPage.getShareTitle() == null){
                    sellPage.setShareTitle("");
                }

				if(sellPage.getImages() == null){
					sellPage.setImages("");
				}

                if(sellPage.getIsLoadByHaiwan() == null){
                    sellPage.setIsLoadByHaiwan(true);
                }

				if(sellPage.getBusinessSellPageUrl() == null){
					sellPage.setBusinessSellPageUrl("");
				}

                sellPageMapper.update(sellPage);

                data.put("id", sellPage.getId());
                sellPage = sellPageMapper.selectByPrimaryKey(sellPage.getId());
                data.put("pageKey", sellPage.getPageKey());
            } else {
                return verifyMap;
            }
        }catch (Exception e){
            e.printStackTrace();
            return Tools.f(null,999, "编辑失败");
        }

        return Tools.s(data);
    }

    /**
     * 查询bigbayAppId对应的购买页信息
     * @param bigbayAppId 业务端在海湾的项目id
     * @param mapParams 海湾接收到业务端传递的参数
     * @return
     */
    public Map<String, Object> listSellPage(String bigbayAppId, Map<String,Object> mapParams){

        try {
            Integer pageSize = mapParams.get("pageSize") == null ? 10 : (Integer) mapParams.get("pageSize");
            Integer startNo = mapParams.get("pageNo") ==null ? 0 : ((Integer) mapParams.get("pageNo")-1)*pageSize;

            List<SellPage> sellPageList = sellPageMapper.selectByBigbayAppId(Long.parseLong(bigbayAppId), startNo, pageSize);

            return Tools.s(sellPageList);
        } catch (Exception e) {
            e.printStackTrace();
            return Tools.f(null,999, "查询失败");
        }
    }

	public Map<String, Object> sellPageInfo(Map<String,Object> mapParams){

		try {
			Integer sellPageId = (Integer)mapParams.get("id");
			String pageKey = (String)mapParams.get("pageKey");

			if(sellPageId == null && StringUtils.isEmpty(pageKey)){
				String message = "购买页id和pageKey不能同时为空";
				return Tools.f(null, 400, message);
			}

			SellPage sellPageInfo = sellPageMapper.selectByIdAndPageKey(sellPageId == null ? null : Long.valueOf(sellPageId), pageKey);

			return Tools.s(sellPageInfo);
		} catch (Exception e) {
			e.printStackTrace();
			return Tools.f(null,999, "查询失败");
		}
	}

    private Map<String,Object> verifySellPageParams(String content, Long id){
        Map<String,Object> mapParams = (Map<String, Object>) JSON.parse(content);

        String message = "";
        if(StringUtils.isEmpty(mapParams.get("pageTitle"))){
            message = "购买页title不能为空";
            return Tools.f(null, 400, message);
        }

        if(StringUtils.isEmpty(mapParams.get("itemName"))){
            message = "购买页名称不能为空";
            return Tools.f(null, 400, message);
        }

        //name 不能重复校验
        int countByName = sellPageMapper.countByName((String)mapParams.get("itemName"), id);
        if(countByName >= 1) {
            return Tools.f(null, 400, "requestParam itemName repeat");
        }

        if(StringUtils.isEmpty(mapParams.get("description"))){
            message = "购买页描述不能为空";
            return Tools.f(null, 400, message);
        }

        if(StringUtils.isEmpty(mapParams.get("purchasePageKey"))){
            message = "购买页模板不能为空";
            return Tools.f(null, 400, message);
        }

        if(StringUtils.isEmpty(mapParams.get("state"))){
            message = "购买页状态不能为空";
            return Tools.f(null, 400, message);
        }

        if(StringUtils.isEmpty(mapParams.get("isLoadSellPageImage"))){
            message = "是否后端渲染图片参数不能为空";
            return Tools.f(null, 400, message);
        }

		Integer isLoadByHaiwan = null;
        try {
			isLoadByHaiwan = (Integer) mapParams.get("isLoadByHaiwan");
		}catch (Exception e){
			message = "购买页是否由海湾加载参数格式不正确";
			return Tools.f(null, 400, message);
		}

        if(null != isLoadByHaiwan) {
//            message = "购买页是否由海湾加载参数不能为空";
//            return Tools.f(null, 400, message);


            List<Integer> asList = Arrays.asList(0, 1);
            if (!asList.contains(isLoadByHaiwan)) {
                message = "购买页是否由海湾加载参数不正确";
                return Tools.f(null, 400, message);
            }
        }

        return Tools.s("success");
    }

    private String getRandomString(int length) {
        String string = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuffer sb = new StringBuffer();
        int len = string.length();
        for (int i = 0; i < length; i++) {
            sb.append(string.charAt(getRandom(len - 1)));
        }
        return sb.toString();
    }

    private int getRandom(int count) {
        return (int) Math.round(Math.random() * (count));
    }



    /**
     * 建立学员与班长的分销关系
     * @param bigbayAppId
     * @param mapParams
     */
    public Map<String, Object> createDistributionRelation(String bigbayAppId, Map<String,Object> mapParams){
        log.info("outer create distribution relation ...[bigbayAppId={}]",bigbayAppId);
        String message = "";
        String pageKey = (String) mapParams.get("pageKey");
        String unionId = (String) mapParams.get("unionId");
        String openId = (String) mapParams.get("openId");
        Integer distributorId = (Integer) mapParams.get("distributorId");

        String nickName = (String) mapParams.get("nickName");
        String headImgUrl = (String) mapParams.get("headImgUrl");
        Integer sex = (Integer) mapParams.get("sex");

        if(StringUtils.isEmpty(pageKey)){
            message = "pageKey不能为空";
            return Tools.f(null, 400, message);
        }

        if( null == distributorId){
            message = "distributorId不能为空";
            return Tools.f(null, 400, message);
        }

        if(StringUtils.isEmpty(openId)){
            message = "openId不能为空";
            return Tools.f(null, 400, message);
        }

        if(StringUtils.isEmpty(unionId)){
            message = "unionId不能为空";
            return Tools.f(null, 400, message);
        }


       // SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
        SellPage sellPage = sellPageMapper.selectByIdAndPageKey(null, pageKey);
        if(sellPage == null){
            message = "pageKey不存在";
            return Tools.f(null, 400, message);
        }

		ZebraDistributors zebraDistributors = zebraDistributorsMapper.selectByPrimaryKey(distributorId);
		if(zebraDistributors == null){
			message = "distributorId不存在";
			return Tools.f(null, 400, message);
		}

        BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
        
        try {
            //1、维护用户信息
            wechatUsersService.maintainUserInfo(bigbayApp,unionId,openId,nickName,headImgUrl,sex);

            //2、建立活跃的分销关系
            BigbaySimpleUsers bigbaySimpleUsers = bigbaySimpleUsersMapper.getUser(sellPage.getBigbayAppId(), openId);


            log.info("====openId=" + openId + ",biabayappid :"+sellPage.getBigbayAppId()+" get bigbaySimpleUsers is=" + bigbaySimpleUsers);


            Map<String,Object> returnMap = new HashMap<>();

            biabaySalesService.distributonRelation(bigbaySimpleUsers, sellPage, distributorId, returnMap);
        }catch (Exception e){
            e.printStackTrace();
            return Tools.f(null,500,"失败");
        }

        return Tools.s(null);

    }

	public int checkItemName(String name, Integer id) {
		
		return sellPageItemMapper.countByName(name, id == null ? null : Long.valueOf(id));
	}
	

    public int checkSellPageName(String sellPageName, Integer id) {
        return sellPageMapper.countByName(sellPageName, id == null ? null : Long.valueOf(id));
    }

    public Map<String,Object> checkPriceByPageKey(String pageKeys){
		String[] pageKeyArr = pageKeys.split(",");
		SellPageItemPricePipeContext context = new SellPageItemPricePipeContext();
		JSONArray retJson = new JSONArray();
		for(int i=0; i<pageKeyArr.length; i++){
			String pageKey = pageKeyArr[i];


			JSONObject sellPageJson = new JSONObject();
			retJson.add(sellPageJson);

			sellPageJson.put("pageKey",pageKey);

			JSONArray sellPageItemJsonArr = new JSONArray();
			sellPageJson.put("sellPageItems",sellPageItemJsonArr);
			
			SellPage sellPage = null;
			if(!StringUtils.isEmpty(pageKey)) {
				sellPage = sellPageMapper.selectByIdAndPageKey(null, pageKey);
			}
					
			if(sellPage == null){
				continue;
			}

			long sellPageId = sellPage.getId();
			List<SellPageItem> sellPageItemList = sellPageItemCacheBySellPageId.getListByKey(sellPageId + "");
			if(sellPageItemList == null || sellPageItemList.size() == 0){
				continue;
			}

			for(SellPageItem sellPageItem : sellPageItemList){
				JSONObject sellPageItemJson = new JSONObject();
				context.setSellPageItemIds(Arrays.asList(String.valueOf(sellPageItem.getId())));
				int price = sellPageItemPricePipeManager.getPriceForItem(context);
				sellPageItemJson.put("id",sellPageItem.getId());
				sellPageItemJson.put("name",sellPageItem.getName());
				sellPageItemJson.put("price",price + "");
				JSONObject sellPageItemConfig = null;
				try {
					sellPageItemConfig = JSONObject.parseObject(sellPageItem.getCallbackConfig());
					sellPageItemJson.put("sellPageItemConfig",sellPageItemConfig);
				}catch (Exception e){
					e.printStackTrace();
				}

				sellPageItemJsonArr.add(sellPageItemJson);
			}

		}

		return Tools.s(retJson);
}
    /**添加多个商品*/
    @Transactional("configTransactionManager")
	public Object addItems(String content) throws Exception {
		
		List<SellPageItem> list = null;
		try {
			list = new Gson().fromJson(content, new TypeToken<List<SellPageItem>>() {}.getType());
			//list = GsonUtil.getList(content, SellPageItem.class);
		} catch (Exception e) {
			log.error(" ==== content param err , {} === ", content);
			throw new JsonErrException(" ==== content param err , %s === ", content) ;
		}
		if(list == null || list.size() == 0) {
			return Tools.f(null, 403, "商品为null");
		}
		for (SellPageItem sellPageItem : list) {
			
			BeanValidators.validateWithException(validator, sellPageItem);//校验必填字段
			//校验商品名称是否重复
			String name = sellPageItem.getName();
			int countByName = sellPageItemMapper.countByName(name, null);
			if(countByName >= 1) {
				Map<String,Object> map = Maps.newHashMap();
				map.put("field", "name");
				map.put("value", name);
				return Tools.f(map, 401, "商品名称重复");
			}
			//sellPageId是否存在校验
			long sellPageId = sellPageItem.getSellPageId();
			SellPage sellPage = sellPageMapper.selectByPrimaryKey(sellPageId);
			if (null == sellPage) {
				Map<String,Object> map = Maps.newHashMap();
				map.put("field", "sellPageId");
				map.put("value", sellPageId);
				return Tools.f(map, 402, "购买页id不存在");
			}
			
		}
		Set<String> set = list.stream().map(item -> item.getName()).collect(Collectors.toSet());
		if(set.size() != list.size()) {
			return Tools.f(set, 402, "商品名称重复");
		}
		List<Object> data = Lists.newArrayList();
		
		//保存数据
		for (SellPageItem sellPageItem : list) {
			Map<String, Object> params = PropertyUtils.describe(sellPageItem);
			params.put("itemBody", params.get("name"));// 补全数据
			sellPageItemMapper.insert(params);
			Long id = (Long) params.get("id");
			data.add(id);
		}
		return Tools.s(data);
	}

	public Object getItemDetail(Map<String, Object> map) throws Exception {
		
		Integer itemId = (Integer) map.get("id");
		log.info("===== getItemDetail param: id={}", itemId);
		if(itemId == null) {
			log.error("===== getItemDetail param: id is null");
			throw new ParamMissException("param %s miss", "id");
		}
		
		SellPageItem sellPageItem = sellPageItemMapper.selectByPrimaryKey(itemId.longValue());
		return Tools.s(sellPageItem);
		
	}



}
