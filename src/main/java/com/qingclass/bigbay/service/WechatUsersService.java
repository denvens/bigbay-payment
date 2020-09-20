package com.qingclass.bigbay.service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.qingclass.bigbay.common.WeChartUser;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.wechatUsers.BigbayFullUsers;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayFullUsersMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;

@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class WechatUsersService {
	
	@Autowired
	private BigbayFullUsersMapper bigbayFullUsersMapper;
	@Autowired
	private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;
	
	private static Logger log = LoggerFactory.getLogger(WechatUsersService.class);
	
	/**
	 * 维护微信用户信息
	 * @param scope
	 * @param bigbayApp
	 * @param retData
	 */
	public void maintainUserInfo(String scope, BigbayApp bigbayApp, Map<String, String> retData) {
		log.info("codeToOpenId====maintainUserInfo===scope="+scope+",bigbayApp="+JSON.toJSONString(bigbayApp)+",retData="+JSON.toJSONString(retData));
		try {
			if(!StringUtils.isEmpty(scope) && "base".equals(scope)) {
				try {
					String openId = retData.get("openId");
					BigbaySimpleUsers bigbaySimpleUsers = new BigbaySimpleUsers();
					bigbaySimpleUsers.setBigbayAppId(bigbayApp.getId());
					bigbaySimpleUsers.setBigbayFullUserId(0);
					bigbaySimpleUsers.setOpenId(openId);
					bigbaySimpleUsersMapper.insert(bigbaySimpleUsers);
					log.info("bigbaySimpleUsersMapper.insert======");
				} catch (Exception e) {
				}
			
			}else {
				String unionId = retData.get("unionId");
				String openId = retData.get("openId");
				log.info("maintainUserInfo ==unionId======"+unionId+" ,openId======"+openId);
				BigbayFullUsers bigbayFullUsers2 = bigbayFullUsersMapper.getUser(unionId);
				if(bigbayFullUsers2==null) {
					log.info("bigbayFullUsers2 is null");
					BigbayFullUsers bigbayFullUsers = new BigbayFullUsers();
					bigbayFullUsers.setHeadImgUrl(retData.get("headImgUrl"));
					bigbayFullUsers.setNickName(retData.get("nickName"));
					bigbayFullUsers.setSex(Integer.parseInt(retData.get("sex")));
					bigbayFullUsers.setUnionId(retData.get("unionId"));
					bigbayFullUsersMapper.insert(bigbayFullUsers);
					log.info("bigbayFullUsersMapper.insert====bigbayFullUsers is null====");
					log.info("bigbayFullUsers.getid====="+bigbayFullUsers.getId()+" bigappid====="+bigbayApp.getId());
					//维护simpleusers
					maintainsimpleusers(bigbayFullUsers.getId(), bigbayFullUsers, openId, bigbayApp);
				}else {
					bigbayFullUsers2.setHeadImgUrl(retData.get("headImgUrl"));
					bigbayFullUsers2.setNickName(retData.get("nickName"));
					bigbayFullUsers2.setSex(Integer.parseInt(retData.get("sex")));
					bigbayFullUsers2.setUnionId(retData.get("unionId"));
					bigbayFullUsersMapper.updateByPrimaryKey(bigbayFullUsers2);
					log.info("bigbayFullUsersMapper.updateByPrimaryKey====bigbayFullUsers is not null");
					log.info("bigbayFullUsers2.getid====="+bigbayFullUsers2.getId()+" bigappid====="+bigbayApp.getId());
					//维护simpleusers 
					maintainsimpleusers(bigbayFullUsers2.getId(), bigbayFullUsers2, openId, bigbayApp);
				}
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	

	private void maintainsimpleusers(Integer id,BigbayFullUsers bigbayFullUsers,String openId,BigbayApp bigbayApp) {
		BigbaySimpleUsers bigbaySimpleUsers1 = bigbaySimpleUsersMapper.getUser(bigbayApp.getId(),openId);
		if(bigbaySimpleUsers1==null) {
			BigbaySimpleUsers bigbaySimpleUsers = new BigbaySimpleUsers();
			bigbaySimpleUsers.setBigbayAppId(bigbayApp.getId());
			bigbaySimpleUsers.setBigbayFullUserId(bigbayFullUsers.getId());
			bigbaySimpleUsers.setOpenId(openId);
			bigbaySimpleUsersMapper.insert(bigbaySimpleUsers);
			log.info("maintainsimpleusers===bigbaySimpleUsersMapper.insert====bigbaySimpleUsers1 is null");
		}else {
			bigbaySimpleUsers1.setBigbayAppId(bigbayApp.getId());
			bigbaySimpleUsers1.setBigbayFullUserId(bigbayFullUsers.getId());
			bigbaySimpleUsers1.setOpenId(openId);
			bigbaySimpleUsersMapper.updateByPrimaryKey(bigbaySimpleUsers1);
			log.info("maintainsimpleusers===bigbaySimpleUsersMapper.updateByPrimaryKey====bigbaySimpleUsers1 is not null");
		}
	}


	public Map<String, Object> getUserInfo(BigbaySimpleUsers user) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
        if(user!=null && user.getOpenId()!=null) {
        	try {
        		BigbayFullUsers bigbayFullUsers = bigbayFullUsersMapper.selectByPrimaryKey(user.getBigbayFullUserId());
        		if(bigbayFullUsers!=null) {
        			log.info("getUserInfo==="+bigbayFullUsers.getNickName());
        			returnMap.put("unionid", bigbayFullUsers.getUnionId());
        			returnMap.put("nickname", bigbayFullUsers.getNickName());
        			returnMap.put("sex", bigbayFullUsers.getSex());
        			returnMap.put("headimgurl", bigbayFullUsers.getHeadImgUrl());
        		}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returnMap;
	}

	/**
	 * 进入app购买页后维护用户信息
	 * @param bigbayApp  购买页所属项目
	 * @param unionId 从url中接收到的unionId
	 * @param openId 从url中接收到的openId
	 */
	public void maintainUserInfo(BigbayApp bigbayApp, String unionId, String openId, String nickName, String headImgUrl, Integer sex) {

		if(StringUtils.isEmpty(nickName)){
			nickName = "海湾用户";
		}

		if(StringUtils.isEmpty(headImgUrl)){
			headImgUrl = "http://bigbay-dev.oss-cn-beijing.aliyuncs.com/bigbayadmin/upload/20190802/image/8cdc2d7ec4f64990999bff03fe659546.png";
		}

		if(null == sex) {
			sex = 1;
		}

		try {
				log.info("app sellpage maintainUserInfo ==unionId======"+unionId+" ,openId======"+openId);
				BigbayFullUsers bigbayFullUser = bigbayFullUsersMapper.getUser(unionId);
				if(bigbayFullUser==null) {
					log.info("bigbayFullUser is null");
					BigbayFullUsers bigbayFullUsers = new BigbayFullUsers();
					bigbayFullUsers.setHeadImgUrl(headImgUrl);
					bigbayFullUsers.setNickName(nickName);
					bigbayFullUsers.setSex(sex);
					bigbayFullUsers.setUnionId(unionId);
					bigbayFullUsersMapper.insert(bigbayFullUsers);
					log.info("bigbayFullUsersMapper.insert====bigbayFullUsers is null====");
					log.info("bigbayFullUsers.getid====="+bigbayFullUsers.getId()+" bigappid====="+bigbayApp.getId());
					//维护simpleusers
					maintainsimpleusers(bigbayFullUsers.getId(), bigbayFullUsers, openId, bigbayApp);
				}else {
					bigbayFullUser.setHeadImgUrl(headImgUrl);
					bigbayFullUser.setNickName(nickName);
					bigbayFullUser.setSex(sex);
					bigbayFullUser.setUnionId(unionId);
					bigbayFullUsersMapper.updateByPrimaryKey(bigbayFullUser);
					log.info("bigbayFullUsersMapper.updateByPrimaryKey====bigbayFullUsers is not null");
					log.info("bigbayFullUsers2.getid====="+bigbayFullUser.getId()+" bigappid====="+bigbayApp.getId());
					//维护simpleusers
					maintainsimpleusers(bigbayFullUser.getId(), bigbayFullUser, openId, bigbayApp);
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public WeChartUser getWeChartUser(Long bigbayAppId, String openId) {
		if(null == bigbayAppId || StringUtils.isEmpty(openId)) {
			return null;
		}
		WeChartUser weChartUser = new WeChartUser();
		weChartUser.setOpenId(openId);
		BigbaySimpleUsers bigbaySimpleUsers = bigbaySimpleUsersMapper.getUser(bigbayAppId, openId);
		Integer bigbayFullUserId = bigbaySimpleUsers.getBigbayFullUserId();
		if(null != bigbayFullUserId && 0 != bigbayFullUserId) {
			BigbayFullUsers bigbayFullUsers = bigbayFullUsersMapper.selectByPrimaryKey(bigbayFullUserId);
			
			try {
				PropertyUtils.copyProperties(weChartUser, bigbayFullUsers);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		return weChartUser;
	}
	
	 
}
