package com.qingclass.bigbay.mapper.config;

import com.qingclass.bigbay.entity.config.AssembleRule;
import com.qingclass.bigbay.mapper.BigbayCacheableMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author ljc
 * @description: TODO
 * @date 2019-12-10 14:44
 */
@Repository
public interface AssembleRuleMapper extends BigbayCacheableMapper<AssembleRule> {

    @Select("select * from assemble_rules order by activityStartTime, id asc")
    List<AssembleRule> selectAll();
}
