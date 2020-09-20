package com.qingclass.bigbay.constant;

/**
 * @author lijiecai
 * @description: 支付类型
 * @date 2019-08-20 16:58
 */
public enum TradeType {


    JSAPI("JSAPI","微信公众号内使用微信支付"),WXAPP("WX-APP","app微信支付"),
    ALIAPP("ALI-APP","app支付宝支付"),ALIH5("ALI-H5","微信公众号内使用支付宝支付"),
    JDH5("JDH5","微信公众号内使用京东支付"),IAP("IAP"," 苹果内购支付"),
    JDAPP("JDAPP","APP内使用京东支付"),
    EMPTY("","");

    private String key;
    private String name;


    private TradeType(String key, String name){
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static TradeType getByKey(String key){
        for(TradeType tradeType : values()){
            if(tradeType.getKey().equals(key)){
                return tradeType;
            }
        }
        return EMPTY;
    }

}
