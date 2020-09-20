package com.qingclass.bigbay.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BigbayTableCacheByIndex<T> {
	
	private Map<String, T> cacheMap = null;
	private Map<String, List<T>> cacheList = null;
	
	public BigbayTableCacheByIndex() {
		super();
	}
	
	abstract public String getKey(T t);
	
	abstract public boolean isKeyDuplicable();
	
	public T getByKey(String key) {
		if (cacheMap == null ) {
			return null;
		}
		return cacheMap.get(key);
	}
	
	public List<T> getListByKey(String key) {
		if (cacheList == null) {
			return null;
		}
		return cacheList.get(key);
	}
	
	protected Map<String, T> getCacheMap() {
		return cacheMap;
	}

	protected Map<String, List<T>> getCacheList() {
		return cacheList;
	}

	public void refresh(List<T> list) {
		if (isKeyDuplicable()) {
			refreshMapList(list);
		} else {
			refreshMapItem(list);
		}
	}
	
	protected void refreshMapItem(List<T> list) {
		Map<String, T> cacheMap = new HashMap<String, T>();
		for (T item : list) {
			String key = getKey(item);
			cacheMap.put(key, item);
		}
		this.cacheMap = cacheMap;
	}
	
	private void refreshMapList(List<T> list) {
		Map<String, List<T>> cacheList = new HashMap<String, List<T>>();
		
		for (T item : list) {
			String key = getKey(item);
			List<T> entities = cacheList.get(key);
			if (null == entities) {
				entities = new ArrayList<T>();
				cacheList.put(key, entities);
			}
			entities.add(item);
		}
		this.cacheList = cacheList;
	}

}
