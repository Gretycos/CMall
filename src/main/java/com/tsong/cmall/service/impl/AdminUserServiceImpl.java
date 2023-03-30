package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.dao.AdminUserMapper;
import com.tsong.cmall.dao.AdminUserTokenMapper;
import com.tsong.cmall.entity.AdminUser;
import com.tsong.cmall.entity.AdminUserToken;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.AdminUserService;
import com.tsong.cmall.util.MD5Util;
import com.tsong.cmall.util.NumberUtil;
import com.tsong.cmall.util.SystemUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    @Autowired
    private AdminUserMapper adminUserMapper;
    @Autowired
    private AdminUserTokenMapper adminUserTokenMapper;

    @Override
    public String login(String userName, String password) {
        AdminUser loginAdminUser = adminUserMapper.login(userName, password);
        if (loginAdminUser != null) {
            //登录后即执行修改token的操作
            String token = getNewToken(System.currentTimeMillis() + "", loginAdminUser.getAdminUserId());
            AdminUserToken adminUserToken = adminUserTokenMapper.selectByPrimaryKey(loginAdminUser.getAdminUserId());
            //当前时间
            Date now = new Date();
            //过期时间
            Date expireTime = new Date(now.getTime() + 2 * 24 * 3600 * 1000); // 过期时间 48 小时
            if (adminUserToken == null) {
                adminUserToken = AdminUserToken.builder()
                        .adminUserId(loginAdminUser.getAdminUserId())
                        .token(token)
                        .updateTime(now)
                        .expireTime(expireTime)
                        .build();
                //新增一条token数据
                if (adminUserTokenMapper.insertSelective(adminUserToken) > 0) {
                    //新增成功后返回
                    return token;
                }
            } else {
                adminUserToken.setToken(token);
                adminUserToken.setUpdateTime(now);
                adminUserToken.setExpireTime(expireTime);
                //更新
                if (adminUserTokenMapper.updateByPrimaryKeySelective(adminUserToken) > 0) {
                    //修改成功后返回
                    return token;
                }
            }

        }
        return ServiceResultEnum.LOGIN_ERROR.getResult();
    }

    /**
     * @Description 获取token值
     * @Param [timeStr, userId]
     * @Return java.lang.String
     */
    private String getNewToken(String timeStr, Long userId) {
        String src = timeStr + userId + NumberUtil.genRandomNum(6);
        return SystemUtil.genToken(src);
    }

    @Override
    public AdminUser getUserDetailById(Long loginUserId) {
        return adminUserMapper.selectByPrimaryKey(loginUserId);
    }

    @Override
    @Transactional
    public Boolean updatePassword(Long loginUserId, String originalPassword, String newPassword) {
        AdminUser adminUser = adminUserMapper.selectByPrimaryKey(loginUserId);
        if (adminUser == null){
            CMallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        // 当前用户非空才可以进行更改
        String originalPasswordMD5 = MD5Util.MD5Encode(originalPassword, Constants.UTF_ENCODING);
        String newPasswordMD5 = MD5Util.MD5Encode(newPassword, Constants.UTF_ENCODING);
        // 比较原密码是否正确
        if (originalPasswordMD5.equals(adminUser.getLoginPassword())) {
            // 设置新密码并修改
            adminUser.setLoginPassword(newPasswordMD5);
            // 修改成功则清空token并返回true
            if (adminUserMapper.updateByPrimaryKeySelective(adminUser) <= 0){
                CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
            }
            if (!logout(loginUserId)){
                CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean updateName(Long loginUserId, String loginUserName, String nickName) {
        AdminUser adminUser = adminUserMapper.selectByPrimaryKey(loginUserId);
        //当前用户非空才可以进行更改
        if (adminUser != null) {
            //设置新名称并修改
            adminUser.setLoginUserName(loginUserName);
            adminUser.setNickName(nickName);
            //修改成功则返回true
            return adminUserMapper.updateByPrimaryKeySelective(adminUser) > 0;
        }
        return false;
    }

    @Override
    public Boolean logout(Long adminUserId) {
        return adminUserTokenMapper.deleteByPrimaryKey(adminUserId) > 0;
    }
}
