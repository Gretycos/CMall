package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.mall.param.MallUserPasswordParam;
import com.tsong.cmall.controller.mall.param.MallUserUpdateParam;
import com.tsong.cmall.dao.CouponMapper;
import com.tsong.cmall.dao.MallUserMapper;
import com.tsong.cmall.dao.UserCouponRecordMapper;
import com.tsong.cmall.dao.UserTokenMapper;
import com.tsong.cmall.entity.Coupon;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.entity.UserCouponRecord;
import com.tsong.cmall.entity.UserToken;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.MallUserService;
import com.tsong.cmall.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/3/24 14:57
 */
@Service
public class MallUserServiceImpl implements MallUserService {
    @Autowired
    private MallUserMapper mallUserMapper;

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponRecordMapper userCouponRecordMapper;

    @Override
    public PageResult getMallUsersPage(PageQueryUtil pageUtil) {
        List<MallUser> mallUsers = mallUserMapper.findMallUserList(pageUtil);
        int total = mallUserMapper.getTotalMallUsers(pageUtil);
        return new PageResult(mallUsers, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String register(String loginName, String password) {
        if (mallUserMapper.selectByLoginName(loginName) != null) {
            return ServiceResultEnum.SAME_LOGIN_NAME_EXIST.getResult();
        }
        String passwordMD5 = MD5Util.MD5Encode(password, Constants.UTF_ENCODING);

        MallUser registerUser = MallUser.builder()
                .loginName(loginName)
                .nickName(loginName)
                .passwordMd5(passwordMD5)
                .build();
        if (mallUserMapper.insertSelective(registerUser) <= 0) {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        // 添加注册赠券
        List<Coupon> coupons = couponMapper.selectAvailableGivenCoupon();
        // 添加领券记录
        for (Coupon coupon : coupons) {
            UserCouponRecord userCouponRecord = UserCouponRecord.builder()
                    .userId(registerUser.getUserId())
                    .couponId(coupon.getCouponId())
                    .build();
            if (userCouponRecordMapper.insertSelective(userCouponRecord) <= 0){
                CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
            }
        }
        return ServiceResultEnum.SUCCESS.getResult();
    }

    @Override
    public String login(String loginName, String passwordMD5) {
        MallUser user = mallUserMapper.selectByLoginNameAndPasswd(loginName, passwordMD5);
        if (user != null) {
            if (user.getLockedFlag() == 1) {
                return ServiceResultEnum.LOGIN_USER_LOCKED_ERROR.getResult();
            }
            // 新token
            String token = getNewToken(System.currentTimeMillis() + "", user.getUserId());
            // 当前时间
            Date now = new Date();
            // 过期时间
            Date expireTime = new Date(now.getTime() + 2 * 24 * 3600 * 1000); // 过期时间 48 小时

            UserToken userToken = userTokenMapper.selectByPrimaryKey(user.getUserId());
            if (userToken == null) {
                // 用户没登录过
                userToken = UserToken.builder()
                        .userId(user.getUserId())
                        .token(token)
                        .updateTime(now)
                        .expireTime(expireTime)
                        .build();
                //新增一条token数据
                if (userTokenMapper.insertSelective(userToken) > 0) {
                    //新增成功后返回
                    return token;
                }
            } else {
                // 用户登录过，修改token
                userToken.setToken(token);
                userToken.setUpdateTime(now);
                userToken.setExpireTime(expireTime);
                //更新
                if (userTokenMapper.updateByPrimaryKeySelective(userToken) > 0) {
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
        String src = timeStr + userId + NumberUtil.genRandomNum(4);
        return SystemUtil.genToken(src);
    }

    @Override
    public Boolean updateUserInfo(MallUserUpdateParam mallUserUpdateParam, Long userId) {
        MallUser user = mallUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            CMallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        user.setNickName(mallUserUpdateParam.getNickName());
        user.setIntroduceSign(mallUserUpdateParam.getIntroduceSign());
        return mallUserMapper.updateByPrimaryKeySelective(user) > 0;
    }

    @Override
    @Transactional
    public Boolean updateUserPassword(MallUserPasswordParam mallUserPasswordParam, Long loginUserId) {
        MallUser user = mallUserMapper.selectByPrimaryKey(loginUserId);
        if (user == null){
            CMallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        String originalPassword = mallUserPasswordParam.getOriginalPassword();
        String newPassword = mallUserPasswordParam.getNewPassword();

        String originalPasswordMD5 = MD5Util.MD5Encode(originalPassword, Constants.UTF_ENCODING);
        String newPasswordMD5 = MD5Util.MD5Encode(newPassword, Constants.UTF_ENCODING);
        if (originalPasswordMD5.equals(user.getPasswordMd5())){
            user.setPasswordMd5(newPasswordMD5);
            if (mallUserMapper.updateByPrimaryKeySelective(user) <= 0){
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
    public Boolean logout(Long userId) {
        return userTokenMapper.deleteByPrimaryKey(userId) > 0;
    }

    @Override
    public Boolean lockUsers(Long[] ids, int lockStatus) {
        if (ids.length < 1) {
            return false;
        }
        return mallUserMapper.lockUserBatch(ids, lockStatus) > 0;
    }
}
