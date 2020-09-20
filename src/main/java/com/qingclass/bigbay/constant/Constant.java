package com.qingclass.bigbay.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 常量类
 */
public class Constant {
	//分销日志金额操作状态
	/**冻结金额*/
	public static final Integer FREEZE = 1;
	/**可提现金额*/
	public static final Integer CASH= 2;
	/**总收入*/
	public static final Integer TOTALMONEY= 3;
	
	/**花呗全额退款*/
	public static final String HUABEI_FULL_REFUND= "花呗退款";
	/**花呗部分退款*/
	public static final String HUABEI_SECTION_REFUND= "花呗部分退款";
	
	/**渠道新建*/
	public static final Integer CHANNEL_INSERT = 100;
	/**渠道删除*/
	public static final Integer CHANNEL_DELETE = 101;
	/**渠道覆盖*/
	public static final Integer CHANNEL_COEVER = 102;
	
	/**分销新建*/
	public static final Integer DISTRIBUTION_INSERT = 200;
	/**分销删除*/
	public static final Integer DISTRIBUTION_DELETE = 201;
	/**分销覆盖*/
	public static final Integer DISTRIBUTION_COEVER = 202;
	
	/**100:打开页面 101:点击支付按钮 */
	public static final Integer INIT = 100;
	public static final Integer BUY_CLICK = 101;
	
	/**加载购买页图片 */
	public static final Integer LoadImage = 1;
	
	public static final String PANDORANATIONWIDEDISTRIBUTION = "pandoraRecommend";
	
	public static final String READINVITATIONPRIZE = "readInvitationPrize";
	
	public static final String SQUIRRELINVITEPRIZE = "squirrelInvitationPrize";
	
	public static final List<String> sourceList = new ArrayList<>();  
    static  
    {  
    	sourceList.add(SQUIRRELINVITEPRIZE);
    }  
	 
    public static final List<String> bigbayAppSourceList = new ArrayList<>();  
    static  
    {  
    	bigbayAppSourceList.add(PANDORANATIONWIDEDISTRIBUTION);
    	bigbayAppSourceList.add(READINVITATIONPRIZE);
    }  
    
    public static final List<String> invitationPrizeList = new ArrayList<>();  
    static  
    {  
    	invitationPrizeList.add(READINVITATIONPRIZE);
    	invitationPrizeList.add(SQUIRRELINVITEPRIZE);
    }  
    
    public static final Map<String,String> payTypeMap = new HashMap<>();
    static  
    {  
    	payTypeMap.put("JSAPI", "wechat");
    	payTypeMap.put("JDH5", "jdH5");
    	payTypeMap.put("JDAPP", "jdApp");

		payTypeMap.put("WX-APP","wxApp");
		payTypeMap.put("ALI-APP","aliApp");
		payTypeMap.put("ALI-H5","aliH5");
		payTypeMap.put("IAP","iap");
    }  
}
 