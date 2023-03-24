package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.vo.MallUserVO;
import com.tsong.cmall.dao.CouponMapper;
import com.tsong.cmall.dao.MallUserMapper;
import com.tsong.cmall.dao.UserCouponRecordMapper;
import com.tsong.cmall.entity.Coupon;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.entity.UserCouponRecord;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.MallUserService;
import com.tsong.cmall.util.*;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        MallUser registerUser = new MallUser();
        registerUser.setLoginName(loginName);
        registerUser.setNickName(loginName);
        String passwordMD5 = MD5Util.MD5Encode(password, Constants.UTF_ENCODING);
        registerUser.setPasswordMd5(passwordMD5);
        if (mallUserMapper.insertSelective(registerUser) <= 0) {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        // 添加注册赠券
        List<Coupon> coupons = couponMapper.selectAvailableGivenCoupon();
        // 添加领券记录
        for (Coupon coupon : coupons) {
            UserCouponRecord userCouponRecord = new UserCouponRecord();
            userCouponRecord.setUserId(registerUser.getUserId());
            userCouponRecord.setCouponId(coupon.getCouponId());
            userCouponRecordMapper.insertSelective(userCouponRecord);
        }
        return ServiceResultEnum.SUCCESS.getResult();
    }

    @Override
    public String login(String loginName, String passwordMD5, HttpSession httpSession) {
        MallUser user = mallUserMapper.selectByLoginNameAndPasswd(loginName, passwordMD5);
        if (user != null && httpSession != null) {
            if (user.getLockedFlag() == 1) {
                return ServiceResultEnum.LOGIN_USER_LOCKED.getResult();
            }
            // 昵称太长 影响页面展示
            if (user.getNickName() != null && user.getNickName().length() > 7) {
                String tempNickName = user.getNickName().substring(0, 7) + "..";
                user.setNickName(tempNickName);
            }
            MallUserVO newBeeMallUserVO = new MallUserVO();
            BeanUtil.copyProperties(user, newBeeMallUserVO);
            // 设置购物车中的数量
            httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, newBeeMallUserVO);
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.LOGIN_ERROR.getResult();
    }

    @Override
    public MallUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession) {
        MallUserVO userSignedIn = (MallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        MallUser userFromDB = mallUserMapper.selectByPrimaryKey(userSignedIn.getUserId());
        if (userFromDB == null) {
            return null;
        }
        if (StringUtils.equals(mallUser.getNickName(), userFromDB.getNickName())
                && StringUtils.equals(mallUser.getAddress(), userFromDB.getAddress())
                && StringUtils.equals(mallUser.getIntroduceSign(), userFromDB.getIntroduceSign())) {
            throw new CMallException("个人信息无变更！");
        }

        if (StringUtils.equals(mallUser.getAddress(), userFromDB.getAddress())
                && mallUser.getNickName() == null
                && mallUser.getIntroduceSign() == null) {
            throw new CMallException("个人信息无变更！");
        }

        if (!StringUtils.isEmpty(mallUser.getNickName())) {
            userFromDB.setNickName(MallUtils.cleanString(mallUser.getNickName()));
        }
        if (!StringUtils.isEmpty(mallUser.getAddress())) {
            userFromDB.setAddress(MallUtils.cleanString(mallUser.getAddress()));
        }
        if (!StringUtils.isEmpty(mallUser.getIntroduceSign())) {
            userFromDB.setIntroduceSign(MallUtils.cleanString(mallUser.getIntroduceSign()));
        }
        if (mallUserMapper.updateByPrimaryKeySelective(userFromDB) > 0) {
            MallUserVO mallUserVO = new MallUserVO();
            BeanUtil.copyProperties(userFromDB, mallUserVO);
            httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, mallUserVO);
            return mallUserVO;
        }
        return null;
    }

    @Override
    public Boolean lockUsers(Integer[] ids, int lockStatus) {
        if (ids.length < 1) {
            return false;
        }
        return mallUserMapper.lockUserBatch(ids, lockStatus) > 0;
    }
}
