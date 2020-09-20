package com.qingclass.bigbay.tool;


import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class GsonUtil {

  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private static class SingletonHolder {
    private SingletonHolder() {}

    private final static Gson INSTANCE = new GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .setDateFormat(DATE_FORMAT)
        .create();
  }

  public static Gson getPrototype() {
    return new GsonBuilder().create();
  }

  private static Gson getInstance() {
    return SingletonHolder.INSTANCE;
  }

  public static Gson getSingleton() {
    return getInstance();
  }

  /**
   * 对象转换为字符串
   */
  public static <T> String getJson(T t) {
        return getInstance().toJson(t);
    }
  
   /**
     * 将字符串转化为对象
     *
     */
    public static <T> T getObject(String jsonString, Class<T> clazz) {
        return getInstance().fromJson(jsonString, clazz);
    }
    
    /**
     * 将字符串转化为对象
     */
    public static <T> List<T> getObject(String jsonString, TypeToken<T> typeToken) {
        return getInstance().fromJson(jsonString, typeToken.getType());
    }
    
    /**
     * 将字符串转化为数组
     *
     */
    public static <T> T[] getArray(String jsonString, Class<T> tClass) {
        return getInstance().fromJson(jsonString, TypeToken.getArray(tClass).getType());
    }
    
    /**
     * 将字符串转化为列表
     */
    public static <T> List<T> getList(String jsonString, Class<T> tClass) {
      //  T[] ts = getInstance().fromJson(jsonString, TypeToken.getArray(tClass).getType());
       // return ts == null ? null : Arrays.asList(ts);
      return getInstance().fromJson(jsonString, new TypeToken<List<T>>() {}.getType());
    }

  /**
   * 字符串转成list中有map的
   */
  public static <T> List<Map<String, T>> getListMaps(String gsonString) {
    return getInstance().fromJson(gsonString, new TypeToken<List<Map<String, T>>>() {}.getType());
  }

  /**
   * json字符串转成map的
   * 
   */
  public static <T> Map<String, T> getMaps(String gsonString) {
    return getInstance().fromJson(gsonString, new TypeToken<Map<String, T>>() {}.getType());
  }
}
