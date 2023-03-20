package com.tsong.cmall.service;

import com.tsong.cmall.controller.vo.MallUserVO;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import jakarta.servlet.http.HttpSession;

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
    String login(String loginName, String passwordMD5, HttpSession httpSession);

    /**
     * @Description 用户信息修改并返回最新的用户信息
     * @Param [mallUser, httpSession]
     * @Return com.tsong.cmall.controller.vo.MallUserVO
     */
    MallUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession);

    /**
     * @Description 用户禁用与解除禁用(0-未锁定 1-已锁定)
     * @Param [ids, lockStatus]
     * @Return java.lang.Boolean
     */
    Boolean lockUsers(Integer[] ids, int lockStatus);
}
