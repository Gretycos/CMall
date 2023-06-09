package com.tsong.cmall.controller.admin;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.TokenToAdminUser;
import com.tsong.cmall.controller.admin.param.AdminLoginParam;
import com.tsong.cmall.controller.admin.param.UpdateAdminNameParam;
import com.tsong.cmall.controller.admin.param.UpdateAdminPasswordParam;
import com.tsong.cmall.entity.AdminUser;
import com.tsong.cmall.entity.AdminUserToken;
import com.tsong.cmall.service.AdminUserService;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Tsong
 * @Date 2023/4/3 17:30
 */
@RestController
@Tag(name = "Admin User", description = "2-0.后台管理系统管理员模块接口")
@RequestMapping("/admin")
public class AdminUserAPI {
    @Autowired
    private AdminUserService adminUserService;

    private static final Logger logger = LoggerFactory.getLogger(AdminUserAPI.class);

    @PostMapping(value = "/adminUser/login")
    public Result<String> login(@RequestBody @Valid AdminLoginParam adminLoginParam) {
        String loginResult = adminUserService.login(adminLoginParam.getUserName(), adminLoginParam.getPasswordMd5());
        logger.info("manage login api,adminName={},loginResult={}", adminLoginParam.getUserName(), loginResult);

        //登录成功
        if (StringUtils.hasText(loginResult) && loginResult.length() == Constants.TOKEN_LENGTH) {
            Result result = ResultGenerator.genSuccessResult();
            result.setData(loginResult);
            return result;
        }
        //登录失败
        return ResultGenerator.genFailResult(loginResult);
    }

    @GetMapping(value = "/adminUser/profile")
    public Result profile(@TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        AdminUser adminUserFromDB = adminUserService.getUserDetailById(adminUser.getAdminUserId());
        if (adminUserFromDB != null) {
            adminUserFromDB.setLoginPassword("******");
            Result result = ResultGenerator.genSuccessResult();
            result.setData(adminUserFromDB);
            return result;
        }
        return ResultGenerator.genFailResult(ServiceResultEnum.DATA_NOT_EXIST.getResult());
    }

    @PutMapping(value = "/adminUser/password")
    public Result passwordUpdate(@RequestBody @Valid UpdateAdminPasswordParam adminPasswordParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (adminUserService.updatePassword(adminUser.getAdminUserId(), adminPasswordParam.getOriginalPassword(), adminPasswordParam.getNewPassword())) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(ServiceResultEnum.PASSWORD_INCORRECT.getResult());
        }
    }

    @PutMapping(value = "/adminUser/name")
    public Result nameUpdate(@RequestBody @Valid UpdateAdminNameParam adminNameParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (adminUserService.updateName(adminUser.getAdminUserId(), adminNameParam.getLoginUserName(), adminNameParam.getNickName())) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(ServiceResultEnum.DB_ERROR.getResult());
        }
    }

    @DeleteMapping(value = "/adminUser/logout")
    public Result logout(@TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        adminUserService.logout(adminUser.getAdminUserId());
        return ResultGenerator.genSuccessResult();
    }
}
