package com.tsong.cmall.service;

import com.tsong.cmall.entity.AdminUser;

public interface AdminUserService {
    /**
     * @Description 登录
     * @Param [userName, password]
     * @Return java.lang.String
     */
    String login(String userName, String password);

    /**
     * @Description 获得用户信息
     * @Param [loginUserId]
     * @Return com.tsong.cmall.entity.AdminUser
     */
    AdminUser getUserDetailById(Long loginUserId);

    /**
     * @Description 修改密码
     * @Param [loginUserId, originalPassword, newPassword]
     * @Return java.lang.Boolean
     */
    Boolean updatePassword(Long loginUserId, String originalPassword, String newPassword);

    /**
     * @Description 修改昵称
     * @Param [loginUserId, loginUserName, nickName]
     * @Return java.lang.Boolean
     */
    Boolean updateName(Long loginUserId, String loginUserName, String nickName);

    /**
     * @Description 登出接口
     * @Param [adminUserId]
     * @Return java.lang.Boolean
     */
    Boolean logout(Long adminUserId);
}
