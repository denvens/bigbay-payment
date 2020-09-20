package com.qingclass.bigbay.tool;



public enum GroupBuyMemberTypeEnum { 

	Head(1, "团长"),
	Member(2, "团员");

	private Integer key;
	private String value;

	private GroupBuyMemberTypeEnum(Integer key, String value) {
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
		for (GroupBuyMemberTypeEnum st : GroupBuyMemberTypeEnum.values()) {
			if (key.equals(st.key)) {
				return st.value;
			}
		}
		return "";
	}
}
