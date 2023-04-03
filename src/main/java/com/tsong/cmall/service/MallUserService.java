package com.tsong.cmall.service;

import com.tsong.cmall.controller.mall.param.MallUserPasswordParam;
import com.tsong.cmall.controller.mall.param.MallUserUpdateParam;
import com.tsong.cmall.controller.mall.param.UserInfoUpdateParam;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;

public interface MallUserService {
    PageResult getMallUsersPage(PageQueryUtil pageUtil);

    /**
     * @Description 用户注册
     * @Param [loginName, password]
     * @Return java.lang.String
     */
    String register(String loginName, String password);

    /**
     * @Description 登录
     * @Param [loginName, passwordMD5, httpSession]
     * @Return java.lang.String
     */
    String login(String loginName, String passwordMD5);

    /**
     * @Description 用户信息修改并返回最新的用户信息
     * @Param [userId, newNickName, newIntroduceSign]
     * @Return java.lang.Boolean
     */
    Boolean updateUserInfo(MallUserUpdateParam mallUserUpdateParam, Long userId);

    /**
     * @Description 用户修改密码
     * @Param [loginUserId, originalPassword, newPassword]
     * @Return java.lang.Boolean
     */
    Boolean updateUserPassword(MallUserPasswordParam mallUserPasswordParam, Long loginUserId);

    /**
     * @Description 登出接口
     * @Param [userId]
     * @Return java.lang.Boolean
     */
    Boolean logout(Long userId);

    /**
     * @Description 用户禁用与解除禁用(0-未锁定 1-已锁定)
     * @Param [ids, lockStatus]
     * @Return java.lang.Boolean
     */
    Boolean lockUsers(Long[] ids, int lockStatus);
}
