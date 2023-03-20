package com.tsong.cmall.service;

import com.tsong.cmall.entity.AdminUser;

public interface AdminUserService {
    /**
     * @Description 登录
     * @Param [userName, password]
     * @Return com.tsong.cmall.entity.AdminUser
     */
    AdminUser login(String userName, String password);

    /**
     * @Description 获得用户信息
     * @Param [loginUserId]
     * @Return com.tsong.cmall.entity.AdminUser
     */
    AdminUser getUserDetailById(Integer loginUserId);

    /**
     * @Description 修改密码
     * @Param [loginUserId, originalPassword, newPassword]
     * @Return java.lang.Boolean
     */
    Boolean updatePassword(Integer loginUserId, String originalPassword, String newPassword);

    /**
     * @Description 修改昵称
     * @Param [loginUserId, loginUserName, nickName]
     * @Return java.lang.Boolean
     */
    Boolean updateName(Integer loginUserId, String loginUserName, String nickName);

}
