package com.qingclass.bigbay.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * 返回前端结果集工具类
 * 
 * @date 2020-04-10
 * @author sss
 *
 */
public class ResultTool {

	private ResultTool() {
	}

	/**
	 * 如果是返回列表 返回records ，total即可.data返回map
	 * 
	 * @param success
	 * @param data
	 * @param code
	 * @param message
	 * @return
	 */
	public static Map<String, Object> wrapReturn(boolean success, Object data, int code, String message) {
		Map<String, Object> template = new HashMap<String, Object>(7);

		Map map = null;
		if (data instanceof Map) {
			map = (Map) data;
			Object total = map.get("total");
			if (total != null) {
				template.put("total", total);
				map.remove("total");
				data = map;
			}
		}

		template.put("success", success);
		template.put("result", data);
		template.put("code", code);
		template.put("message", message);

		return template;
	}

	public static Map<String, Object> s(Object data) {
		return wrapReturn(true, data, 0, "");
	}

	public static Map<String, Object> f() {
		return wrapReturn(false, new Object(), -1, "");
	}

	public static Map<String, Object> f(String message) {
		return wrapReturn(false, new Object(), -1, message);
	}

	public static Map<String, Object> f(Object data) {
		return wrapReturn(false, data, -1, "");
	}

	public static Map<String, Object> f(Object data, int code) {
		return wrapReturn(false, data, code, "");
	}

	public static Map<String, Object> f(Object data, int code, String message) {
		return wrapReturn(false, data, code, message);
	}

	public static void main(String[] args) {
		List<String> temp = new ArrayList<String>();
		temp.add("1");
		Map<String, Object> map = new HashMap<String, Object>(7);
		map.put("records", temp);
		map.put("total", 1);
		System.out.println(ResultTool.s(map));
	}

}
