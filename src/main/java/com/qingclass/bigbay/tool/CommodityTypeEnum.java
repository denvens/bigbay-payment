package com.qingclass.bigbay.tool;



public enum CommodityTypeEnum { 

	General(0, "一般商品"),
	GroupBuy(1, "拼团商品");

	private Integer key;
	private String value;

	private CommodityTypeEnum(Integer key, String value) {
		this.key = key;
		this.value = value;
	}

	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static String getValue(String key) {
		for (CommodityTypeEnum st : CommodityTypeEnum.values()) {
			if (key.equals(st.key)) {
				return st.value;
			}
		}
		return "";
	}
}
