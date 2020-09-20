package com.qingclass.bigbay.mapper;

import java.util.List;

public interface BigbayCacheableMapper<T> {
	List<T> selectAll();
}
