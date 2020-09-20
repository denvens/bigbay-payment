package com.qingclass.bigbay.service;

import com.qingclass.bigbay.entity.wechatUsers.BigbayReceiverAddress;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayReceiverAddressMapper;
import com.qingclass.bigbay.tool.Tools;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class BigbayReceiverAddressService {
    @Autowired
    BigbayReceiverAddressMapper bigbayReceiverAddressMapper;

    @Transactional(value = "wechatUsersTransactionManager",propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Map<String,Object> add(BigbayReceiverAddress bigbayReceiverAddress) throws Exception{
        int isDefault = bigbayReceiverAddress.getIsDefault();
        if(isDefault==0){
            bigbayReceiverAddressMapper.insert(bigbayReceiverAddress);
        }else if(isDefault == 1){
            bigbayReceiverAddressMapper.updateIsDefault(bigbayReceiverAddress.getUnionId());
            bigbayReceiverAddressMapper.insert(bigbayReceiverAddress);
        }


        BeanMap result = new BeanMap(bigbayReceiverAddress);
        return Tools.s(result);
    }


    @Transactional(value = "wechatUsersTransactionManager",propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Map<String,Object> update(BigbayReceiverAddress bigbayReceiverAddress) throws Exception{
        int isDefault = bigbayReceiverAddress.getIsDefault();
        if(isDefault==0){
            bigbayReceiverAddressMapper.update(bigbayReceiverAddress);
        }else if(isDefault == 1){
            bigbayReceiverAddressMapper.updateIsDefault(bigbayReceiverAddress.getUnionId());
            bigbayReceiverAddressMapper.update(bigbayReceiverAddress);
        }

        BeanMap result = new BeanMap(bigbayReceiverAddress);
        return Tools.s(result);
    }

}
