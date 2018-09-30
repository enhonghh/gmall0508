package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  UserInfoMapper userInfoMapper;

  @Autowired
  UserAddressMapper userAddressMapper;




    @Override
    public List<UserInfo> getUserList() {

      List<UserInfo> userInfos = userInfoMapper.selectAll();

      return userInfos;
    }

  @Override
  public List<UserAddress> getUserAddress() {
    List<UserAddress> userAddresses = userAddressMapper.selectAll();

    return userAddresses;
  }

      @Override
     public List<UserInfo> getUserId(String id){
       //通用mapper不能直接传入id，要先用对象获取id，然后传入对象
       UserInfo userInfo = new UserInfo();
       userInfo.setId(id);

       List<UserInfo> select = userInfoMapper.select(userInfo);

       return select;

     }

}
