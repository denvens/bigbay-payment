package com.qingclass.bigbay.controller;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUMain {
    public static void main(String [] flag) {

        LRUCache<String, Integer> lruCache = new LRUCache<String,Integer>(10);

        for(int i=0; i<10; ++i) {
            System.out.println("put "+i);
            lruCache.put(String.valueOf(i), i);
        }

        for(int i=9; i>=0; --i) {
            System.out.println("get "+i);
            lruCache.get(String.valueOf(i));
        }

        // 触发
        System.out.println("put "+ "chufa");
        lruCache.put("chufa", 0);
        lruCache.put("chufa", 5);

        Iterator<Map.Entry<String, Integer>> iterator= lruCache.entrySet().iterator();

        while(iterator.hasNext())
        {
            Map.Entry entry = iterator.next();
            System.out.println(entry.getKey()+":"+entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : lruCache.entrySet()) {
            System.out.println(entry.getKey()+":"+entry.getValue());
        }

        System.out.println();

    }

    private static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private int CACHE_SIZE;

        public LRUCache(int cashSize) {
            super(cashSize*2, 0.75f, true);
            CACHE_SIZE = cashSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            System.out.println("可能触发删除 - " + eldest.getKey()+":"+eldest.getValue());
            System.out.println("可能触发删除 - " + size() + "  " + CACHE_SIZE);
            if(size() > CACHE_SIZE) {
                System.out.println("触发删除 " + size() + "  " + CACHE_SIZE);
            }
            return size() > CACHE_SIZE;
        }
    }

}