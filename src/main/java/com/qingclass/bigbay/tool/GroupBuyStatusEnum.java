package com.qingclass.bigbay.tool;



public enum GroupBuyStatusEnum { 

	Ing(0, "拼团中"),
	Success(1, "拼团成功"),
	Failure(2, "拼团失败");

	private Integer key;
	private String value;

	private GroupBuyStatusEnum(Integer key, String value) {
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
		for (GroupBuyStatusEnum st : GroupBuyStatusEnum.values()) {
			if (key.equals(st.key)) {
				return st.value;
			}
		}
		return "";
	}
}
