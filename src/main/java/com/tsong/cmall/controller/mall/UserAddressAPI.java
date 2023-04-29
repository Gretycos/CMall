package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.controller.mall.param.SaveUserAddressParam;
import com.tsong.cmall.controller.mall.param.UpdateUserAddressParam;
import com.tsong.cmall.controller.vo.UserAddressVO;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.entity.UserAddress;
import com.tsong.cmall.service.UserAddressService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/4/2 23:38
 */
@RestController
@Tag(name = "User Address", description = "1-6.商城个人地址相关接口")
@RequestMapping("/api")
public class UserAddressAPI {
    @Autowired
    private UserAddressService userAddressService;

    @GetMapping("/address")
    @Operation(summary = "我的收货地址列表", description = "")
    public Result<List<UserAddressVO>> addressList(@TokenToMallUser MallUser loginMallUser) {
        return ResultGenerator.genSuccessResult(userAddressService.getMyAddresses(loginMallUser.getUserId()));
    }

    @PostMapping("/address")
    @Operation(summary = "添加地址", description = "")
    public Result<Boolean> saveUserAddress(@Parameter(name = "新增地址参数")@RequestBody @Valid SaveUserAddressParam saveUserAddressParam,
                                           @TokenToMallUser MallUser loginMallUser) {
        UserAddress userAddress = new UserAddress();
        BeanUtil.copyProperties(saveUserAddressParam, userAddress);
        userAddress.setUserId(loginMallUser.getUserId());
        Boolean saveResult = userAddressService.saveUserAddress(userAddress);
        //添加成功
        if (saveResult) {
            return ResultGenerator.genSuccessResult();
        }
        //添加失败
        return ResultGenerator.genFailResult("添加失败");
    }

    @PutMapping("/address")
    @Operation(summary = "修改地址", description = "")
    public Result<Boolean> updateUserAddress(@Parameter(name = "修改地址参数")@RequestBody @Valid UpdateUserAddressParam updateUserAddressParam,
                                             @TokenToMallUser MallUser loginMallUser) {
        UserAddress userAddressFromDB = userAddressService.getUserAddressById(updateUserAddressParam.getAddressId());
        if (!loginMallUser.getUserId().equals(userAddressFromDB.getUserId())) {
            return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        UserAddress userAddress = new UserAddress();
        BeanUtil.copyProperties(updateUserAddressParam, userAddress);
        userAddress.setUserId(loginMallUser.getUserId());
        Boolean updateResult = userAddressService.updateUserAddress(userAddress);
        //修改成功
        if (updateResult) {
            return ResultGenerator.genSuccessResult();
        }
        //修改失败
        return ResultGenerator.genFailResult("修改失败");
    }

    @GetMapping("/address/{addressId}")
    @Operation(summary = "获取收货地址详情", description = "传参为地址id")
    public Result<UserAddressVO> getUserAddress(@Parameter(name = "地址id")@PathVariable("addressId") Long addressId,
                                                @TokenToMallUser MallUser loginMallUser) {
        UserAddress userAddress = userAddressService.getUserAddressById(addressId);
        UserAddressVO userAddressVO = new UserAddressVO();
        BeanUtil.copyProperties(userAddress, userAddressVO);
        if (!loginMallUser.getUserId().equals(userAddress.getUserId())) {
            return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        return ResultGenerator.genSuccessResult(userAddressVO);
    }

    @GetMapping("/address/default")
    @Operation(summary = "获取默认收货地址", description = "无传参")
    public Result getDefaultUserAddress(@TokenToMallUser MallUser loginMallUser) {
        UserAddress mallUserAddressById = userAddressService.getMyDefaultAddressByUserId(loginMallUser.getUserId());
        return ResultGenerator.genSuccessResult(mallUserAddressById);
    }

    @DeleteMapping("/address/{addressId}")
    @Operation(summary = "删除收货地址", description = "传参为地址id")
    public Result deleteAddress(@Parameter(name = "地址id") @PathVariable("addressId") Long addressId,
                                @TokenToMallUser MallUser loginMallUser) {
        UserAddress mallUserAddressById = userAddressService.getUserAddressById(addressId);
        if (!loginMallUser.getUserId().equals(mallUserAddressById.getUserId())) {
            return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        Boolean deleteResult = userAddressService.deleteById(addressId);
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }
}
