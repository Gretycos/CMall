package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.vo.UserAddressVO;
import com.tsong.cmall.dao.UserAddressMapper;
import com.tsong.cmall.entity.UserAddress;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.UserAddressService;
import com.tsong.cmall.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/3/29 23:02
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddressVO> getMyAddresses(Long userId) {
        List<UserAddress> myAddressList = userAddressMapper.findMyAddressList(userId);
        List<UserAddressVO> userAddressVOList = BeanUtil.copyList(myAddressList, UserAddressVO.class);
        return userAddressVOList;
    }

    @Override
    @Transactional
    public Boolean saveUserAddress(UserAddress userAddress) {
        Date now = new Date();
        if (userAddress.getDefaultFlag().intValue() == 1) {
            // 如果添加地址设置为默认地址，需要将原有的默认地址修改掉
            UserAddress defaultAddress = userAddressMapper.getMyDefaultAddress(userAddress.getUserId());
            if (defaultAddress != null) {
                defaultAddress.setDefaultFlag((byte) 0);
                defaultAddress.setUpdateTime(now);
                if (userAddressMapper.updateByPrimaryKeySelective(defaultAddress) <= 0) {
                    //未更新成功
                    CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
                }
            }
        }
        return userAddressMapper.insertSelective(userAddress) > 0;
    }

    @Override
    public Boolean updateUserAddress(UserAddress userAddress) {
        UserAddress tempAddress = getUserAddressById(userAddress.getAddressId());
        Date now = new Date();
        if (userAddress.getDefaultFlag().intValue() == 1) {
            //修改为默认地址，需要将原有的默认地址修改掉
            UserAddress defaultAddress = userAddressMapper.getMyDefaultAddress(userAddress.getUserId());
            if (defaultAddress != null && !defaultAddress.getAddressId().equals(tempAddress.getAddressId())) {
                // 存在默认地址且默认地址并不是当前修改的地址
                defaultAddress.setDefaultFlag((byte) 0);
                defaultAddress.setUpdateTime(now);
                if (userAddressMapper.updateByPrimaryKeySelective(defaultAddress) <= 0) {
                    //未更新成功
                    CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
                }
            }
        }
        userAddress.setUpdateTime(now);
        return userAddressMapper.updateByPrimaryKeySelective(userAddress) > 0;
    }

    @Override
    public UserAddress getUserAddressById(Long addressId) {
        UserAddress userAddress = userAddressMapper.selectByPrimaryKey(addressId);
        if (userAddress == null) {
            CMallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        return userAddress;
    }

    @Override
    public UserAddress getMyDefaultAddressByUserId(Long userId) {
        return userAddressMapper.getMyDefaultAddress(userId);
    }

    @Override
    public Boolean deleteById(Long addressId) {
        return userAddressMapper.deleteByPrimaryKey(addressId) > 0;
    }
}