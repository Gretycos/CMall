package com.tsong.cmall.controller.admin;

import com.tsong.cmall.config.annotation.TokenToAdminUser;
import com.tsong.cmall.controller.admin.param.BatchIdParam;
import com.tsong.cmall.entity.AdminUserToken;
import com.tsong.cmall.service.MallUserService;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Tsong
 * @Date 2023/4/3 17:48
 */
@RestController
@Tag(name = "Admin Mall User", description = "2-6.后台管理系统注册用户模块接口")
@RequestMapping("/admin")
public class AdminMallUserAPI {
    private static final Logger logger = LoggerFactory.getLogger(AdminMallUserAPI.class);

    @Autowired
    private MallUserService userService;

    /**
     * 列表
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @Operation(summary = "商城注册用户列表", description = "商城注册用户列表")
    public Result list(@RequestParam(required = false) @Parameter(name = "页码") Integer pageNumber,
                       @RequestParam(required = false) @Parameter(name = "每页条数") Integer pageSize,
                       @RequestParam(required = false) @Parameter(name = "用户状态") Integer lockStatus,
                       @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (pageNumber == null || pageNumber < 1 || pageSize == null || pageSize < 10) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        Map<String, Object> params = new HashMap<>(8);
        params.put("page", pageNumber);
        params.put("limit", pageSize);
        if (lockStatus != null) {
            params.put("orderStatus", lockStatus);
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(userService.getMallUsersPage(pageUtil));
    }

    /**
     * 用户禁用与解除禁用(0-未锁定 1-已锁定)
     */
    @RequestMapping(value = "/users/{lockStatus}", method = RequestMethod.PUT)
    @Operation(summary = "修改用户状态", description = "批量修改，用户禁用与解除禁用(0-未锁定 1-已锁定)")
    public Result lockUser(@RequestBody BatchIdParam batchIdParam, @PathVariable int lockStatus, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (batchIdParam==null||batchIdParam.getIds().length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if (lockStatus != 0 && lockStatus != 1) {
            return ResultGenerator.genFailResult("操作非法！");
        }
        if (userService.lockUsers(batchIdParam.getIds(), lockStatus)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("禁用失败");
        }
    }
}
