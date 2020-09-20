package com.qingclass.bigbay.controller;

import com.qingclass.bigbay.entity.wechatUsers.BigbayReceiverAddress;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayReceiverAddressMapper;
import com.qingclass.bigbay.service.BigbayReceiverAddressService;
import com.qingclass.bigbay.tool.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReceiverAddressController {
    @Autowired
    BigbayReceiverAddressMapper bigbayReceiverAddressMapper;
    @Autowired
    BigbayReceiverAddressService bigbayReceiverAddressService;

    @PostMapping("/my-address/add")
    @ResponseBody
    public Map<String,Object> addMyAddress(@RequestParam("unionId") String unionId, @RequestParam("name") String name,
                                           @RequestParam("mobile") String mobile, @RequestParam("province") String province,
                                           @RequestParam("city") String city, @RequestParam("district") String district,
                                           @RequestParam("detail") String detail, @RequestParam("isDefault") int isDefault){

        BigbayReceiverAddress bigbayReceiverAddress = new BigbayReceiverAddress();
        bigbayReceiverAddress.setUnionId(unionId);
        bigbayReceiverAddress.setName(name);
        bigbayReceiverAddress.setMobile(mobile);
        bigbayReceiverAddress.setProvince(province);
        bigbayReceiverAddress.setCity(city);
        bigbayReceiverAddress.setDistrict(district);
        bigbayReceiverAddress.setDetail(detail);
        bigbayReceiverAddress.setIsDefault(isDefault);

        try {
          return  bigbayReceiverAddressService.add(bigbayReceiverAddress);
        }catch (Exception e){
            e.printStackTrace();
            return Tools.f(new HashMap<>());
        }
    }

    @PostMapping("/my-address/update")
    @ResponseBody
    public Map<String,Object> updateMyAddress(@RequestParam("id") long id, @RequestParam("unionId") String unionId, @RequestParam("name") String name,
                                              @RequestParam("mobile") String mobile, @RequestParam("province") String province,
                                              @RequestParam("city") String city, @RequestParam("district") String district,
                                              @RequestParam("detail") String detail, @RequestParam("isDefault") int isDefault){

        BigbayReceiverAddress bigbayReceiverAddress = new BigbayReceiverAddress();
        bigbayReceiverAddress.setId(id);
        bigbayReceiverAddress.setUnionId(unionId);
        bigbayReceiverAddress.setName(name);
        bigbayReceiverAddress.setMobile(mobile);
        bigbayReceiverAddress.setProvince(province);
        bigbayReceiverAddress.setCity(city);
        bigbayReceiverAddress.setDistrict(district);
        bigbayReceiverAddress.setDetail(detail);
        bigbayReceiverAddress.setIsDefault(isDefault);

        try {
            return  bigbayReceiverAddressService.update(bigbayReceiverAddress);
        }catch (Exception e){
            e.printStackTrace();
            return Tools.f(new HashMap<>());
        }
    }

    @GetMapping("/my-address/list")
    @ResponseBody
    public Map<String,Object> getMyAddress(@RequestParam("unionId") String unionId){
        try {
            List<Map<String, Object>> addressList = bigbayReceiverAddressMapper.selectByUnionId(unionId);
            Map<String, Object> result = new HashMap<>();
            result.put("addressList",addressList);
            return Tools.s(result);
        }catch (Exception e){
            e.printStackTrace();
            return Tools.f(new HashMap<>());
        }
    }

    @GetMapping("/my-address/list-count")
    @ResponseBody
    public Map<String,Object> getMyAddressCount(@RequestParam("unionId") String unionId){
        try {
            int count = bigbayReceiverAddressMapper.selectCountByUnionId(unionId);
            Map<String, Object> result = new HashMap<>();
            result.put("count",count);
            return Tools.s(result);
        }catch (Exception e){
            e.printStackTrace();
            return Tools.f(new HashMap<>());
        }

    }

    @PostMapping("/my-address/delete")
    @ResponseBody
    public Map<String,Object> deleteMyAddress(@RequestParam("id") Long id, @RequestParam("unionId") String unionId){
        try {
            bigbayReceiverAddressMapper.delete(id, unionId);
            return Tools.s(new HashMap<>());
        }catch (Exception e){
            e.printStackTrace();
            return  Tools.f(new HashMap<>());
        }
    }

}
