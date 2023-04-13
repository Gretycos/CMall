package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.controller.mall.param.MallUserLoginParam;
import com.tsong.cmall.controller.mall.param.MallUserPasswordParam;
import com.tsong.cmall.controller.mall.param.MallUserRegisterParam;
import com.tsong.cmall.controller.mall.param.MallUserUpdateParam;
import com.tsong.cmall.controller.vo.MallUserVO;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.service.MallUserService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.NumberUtil;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Tsong
 * @Date 2023/4/1 17:08
 */
@RestController
@Tag(name = "Mall User", description = "1-2.商城用户操作相关接口")
@RequestMapping("/api")
public class MallUserAPI {
    @Autowired
    private MallUserService userService;

    private static final Logger logger = LoggerFactory.getLogger(MallUserAPI.class);

    @PostMapping("/user/login")
    @Operation(summary = "登录接口", description = "返回token")
    public Result<String> login(@RequestBody @Valid MallUserLoginParam mallUserLoginParam) {
        if (!NumberUtil.isPhone(mallUserLoginParam.getLoginName())){
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_NAME_IS_NOT_PHONE.getResult());
        }
        String loginResult = userService.login(mallUserLoginParam.getLoginName(), mallUserLoginParam.getPasswordMd5());

        logger.info("login api,loginName={},loginResult={}", mallUserLoginParam.getLoginName(), loginResult);

        //登录成功
        if (StringUtils.hasText(loginResult) && loginResult.length() == Constants.TOKEN_LENGTH) {
            Result result = ResultGenerator.genSuccessResult();
            result.setData(loginResult);
            return result;
        }
        //登录失败
        return ResultGenerator.genFailResult(loginResult);
    }


    @PostMapping("/user/logout")
    @Operation(summary = "登出接口", description = "清除token")
    public Result<String> logout(@TokenToMallUser MallUser loginMallUser) {
        Boolean logoutResult = userService.logout(loginMallUser.getUserId());
        logger.info("logout api,loginMallUser={}", loginMallUser.getUserId());

        //登出成功
        if (logoutResult) {
            return ResultGenerator.genSuccessResult();
        }
        //登出失败
        return ResultGenerator.genFailResult("logout error");
    }


    @PostMapping("/user/register")
    @Operation(summary = "用户注册", description = "")
    public Result register(@RequestBody @Valid MallUserRegisterParam mallUserRegisterParam) {
        if (!NumberUtil.isPhone(mallUserRegisterParam.getLoginName())){
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_NAME_IS_NOT_PHONE.getResult());
        }
        String registerResult = userService.register(mallUserRegisterParam.getLoginName(), mallUserRegisterParam.getPassword());

        logger.info("register api,loginName={},loginResult={}", mallUserRegisterParam.getLoginName(), registerResult);

        //注册成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(registerResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //注册失败
        return ResultGenerator.genFailResult(registerResult);
    }

    @PutMapping("/user/info")
    @Operation(summary = "修改用户信息", description = "")
    public Result updateInfo(@RequestBody @Parameter(name = "用户信息") @Valid MallUserUpdateParam mallUserUpdateParam,
                             @TokenToMallUser MallUser loginMallUser) {
        Boolean flag = userService.updateUserInfo(mallUserUpdateParam, loginMallUser.getUserId());
        if (flag) {
            //返回成功
            return ResultGenerator.genSuccessResult();
        } else {
            //返回失败
            return ResultGenerator.genFailResult("修改信息失败");
        }
    }

    @PutMapping("/user/password")
    @Operation(summary = "修改用户密码", description = "")
    public Result updatePassword(@RequestBody @Parameter(name = "用户密码") @Valid MallUserPasswordParam mallUserPasswordParam,
                                 @TokenToMallUser MallUser loginMallUser) {
        Boolean flag = userService.updateUserPassword(mallUserPasswordParam, loginMallUser.getUserId());
        if (flag) {
            //返回成功
            return ResultGenerator.genSuccessResult();
        } else {
            //返回失败
            return ResultGenerator.genFailResult("修改密码失败");
        }
    }

    @GetMapping("/user/info")
    @Operation(summary = "获取用户信息", description = "")
    public Result<MallUserVO> getUserDetail(@TokenToMallUser MallUser loginMallUser) {
        //已登录则直接返回
        MallUserVO mallUserVO = new MallUserVO();
        BeanUtil.copyProperties(loginMallUser, mallUserVO);
        return ResultGenerator.genSuccessResult(mallUserVO);
    }
}
