package com.qingclass.bigbay.constant;

/**
 * zebra_distributor_logs表中type字段的值
 */
public class DistributorLogsType {
    //分销日志金额操作状态
    /**新订单-冻结金额*/
    public static final Integer INIT_FREEZE = 100;
    /**自动解冻-冻结金额*/
    public static final Integer AUTO_FREEZE = 101;
    /**手动关闭-冻结金额*/
    public static final Integer CLOSE_FREEZE = 102;
    /**手动开启-冻结金额*/
    public static final Integer OPEN_FREEZE = 103;
    /**退款-冻结金额*/
    public static final Integer REFUND_FREEZE = 104;
    /**提现-冻结金额*/
    public static final Integer CASH_FREEZE = 105;

    /**新订单-可提现金额*/
    public static final Integer INIT_CASH= 200;
    /**自动解冻-可提现金额*/
    public static final Integer AUTO_CASH= 201;
    /**手动关闭-可提现金额*/
    public static final Integer CLOSE_CASH= 202;
    /**手动开启-可提现金额*/
    public static final Integer OPEN_CASH= 203;
    /**退款-可提现金额*/
    public static final Integer REFUND_CASH= 204;
    /**提现-可提现金额*/
    public static final Integer CASH_CASH= 205;
    /**手动操作-可提现金额*/
    public static final Integer MANUALLY_CASH = 299;

    /**新订单-总收入*/
    public static final Integer INIT_TOTALMONEY= 300;
    /**自动解冻-总收入*/
    public static final Integer AUTO_TOTALMONEY= 301;
    /**手动关闭-总收入*/
    public static final Integer CLOSE_TOTALMONEY= 302;
    /**手动开启-总收入*/
    public static final Integer OPEN_TOTALMONEY= 303;
    /**退款-总收入*/
    public static final Integer REFUND_TOTALMONEY= 304;
    /**提现-待审核金额*/
    public static final Integer CASH_WAITAUDITMONEY= 305;
    /**手动操作-总收入*/
    public static final Integer MANUALLY_TOTALMONEY = 399;

    /**新订单-可用积分*/
    public static final Integer INIT_AVAILABLE_POINTS = 400;
    /**自动解冻-可用积分*/
    public static final Integer AUTO_AVAILABLE_POINTS = 401;
    /**手动关闭-可用积分*/
    public static final Integer CLOSE_AVAILABLE_POINTS = 402;
    /**手动开启-可用积分*/
    public static final Integer OPEN_AVAILABLE_POINTS = 403;
    /**加分-可用积分*/
    public static final Integer ADD_AVAILABLE_POINTS = 404;
    /**扣分-可用积分*/
    public static final Integer DEDUCT_AVAILABLE_POINTS = 405;
    /**清零-可用积分*/
    public static final Integer CLEAR_AVAILABLE_POINTS = 406;
    /**签到-可用积分*/
    public static final Integer ATTENDANCE_AVAILABLE_POINTS = 407;
    /**退款-可用积分*/
    public static final Integer REFUND_AVAILABLE_POINTS = 408;
    /**兑换资源-可用积分*/
    public static final Integer EXCHANGE_RESOURCE_AVAILABLE_POINTS = 409;
    /**分享购买页-可用积分*/
    public static final Integer SELLPAGE_SHARE_AVAILABLE_POINTS = 410;


    /**新订单-总积分*/
    public static final Integer INIT_TOTAL_POINTS = 500;
    /**自动解冻-总积分*/
    public static final Integer AUTO_TOTAL_POINTS = 501;
    /**手动关闭-总积分*/
    public static final Integer CLOSE_TOTAL_POINTS = 502;
    /**手动开启-总积分*/
    public static final Integer OPEN_TOTAL_POINTS = 503;
    /**加分-总积分*/
    public static final Integer ADD_TOTAL_POINTS = 504;
    /**扣分-总积分*/
    public static final Integer DEDUCT_TOTAL_POINTS = 505;
    /**签到-总积分*/
    public static final Integer ATTENDANCE_TOTAL_POINTS = 507;
    /**退款-总积分*/
    public static final Integer REFUND_TOTAL_POINTS = 508;
    /**分享购买页-总积分*/
    public static final Integer SELLPAGE_SHARE_TOTAL_POINTS = 510;

    /**升级*/
    public static final Integer UP_LEVEL = 601;
    /**降级*/
    public static final Integer DOWN_LEVEL = 602;
}
