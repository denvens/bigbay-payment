package com.qingclass.bigbay.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.qingclass.bigbay.entity.config.SellPage;

/**
 * @author sss
 * @date 2019年11月18日 下午4:01:06
 */
@Service
public class SellPageService {
	private static Logger log = LoggerFactory.getLogger(SellPageService.class);

	/**
	 * 设置自定义购买页图片
	 * 
	 * @param sellPage
	 */
	public void setCustomSellPageImage(SellPage sellPage) {
		String sellPageImageConfig = sellPage.getSellPageImageConfig();
		if (!sellPage.getIsCustomSellPageValid()) {
			return;
		}
		try {
			if (!StringUtils.isEmpty(sellPageImageConfig) && !"null".equals(sellPageImageConfig)) {
				List<Map<String, Object>> imageList = (List) JSONArray.parseArray(sellPageImageConfig);
				long nowTime = System.currentTimeMillis();
				List<Long> effectTimes = new ArrayList<Long>();
				long effectTime = 0L;
				String images = "";
				Map<Long, String> effectTimeMap = new HashMap<>();
				for (Map<String, Object> imageMap : imageList) {
					if (StringUtils.isEmpty(imageMap.get("effectTime"))) {
						continue;
					}
					effectTime = Long.parseLong(imageMap.get("effectTime") + "");
					if (nowTime > effectTime) {
						images = imageMap.get("imageList") + "";
						effectTimeMap.put(effectTime, images);
						effectTimes.add(effectTime);
					}
				}
				if (!CollectionUtils.isEmpty(effectTimes)) {
					Collections.sort(effectTimes, new Comparator<Long>() {
						@Override
						public int compare(Long o1, Long o2) {
							return o2.compareTo(o1);
						}
					});
					images = effectTimeMap.get(effectTimes.get(0));
					if (StringUtils.isEmpty(images)) {
						return;
					}
					sellPage.setImages(images);
				}
			}
		} catch (Exception e) {
			log.error(JSON.toJSONString(sellPage) + "_" + e.getMessage(), e);
		}
	}

}
