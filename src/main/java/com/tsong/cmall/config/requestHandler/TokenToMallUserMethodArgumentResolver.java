package com.tsong.cmall.config.requestHandler;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.Slave;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.dao.MallUserMapper;
import com.tsong.cmall.dao.UserTokenMapper;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.entity.UserToken;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.concurrent.TimeUnit;

/**
 * @Author Tsong
 * @Date 2023/4/3 18:06
 */
@Component
public class TokenToMallUserMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private MallUserMapper mallUserMapper;
    @Autowired
    private UserTokenMapper userTokenMapper;

    @Autowired
    private RedisCache redisCache;

    public TokenToMallUserMethodArgumentResolver() {
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TokenToMallUser.class);
    }

    @Override
    @Slave
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        if (parameter.getParameterAnnotation(TokenToMallUser.class) instanceof TokenToMallUser) {
            MallUser mallUser = null;
            String token = webRequest.getHeader("token");
            if (null != token && !"".equals(token) && token.length() == Constants.TOKEN_LENGTH) {
                mallUser = redisCache.getCacheObject(Constants.MALL_USER_TOKEN_KEY + token);
                if (mallUser == null){
                    UserToken mallUserToken = userTokenMapper.selectByToken(token);
                    long curTime = System.currentTimeMillis();
                    if (mallUserToken == null || mallUserToken.getExpireTime().getTime() <= curTime) {
                        CMallException.fail(ServiceResultEnum.TOKEN_EXPIRE_ERROR.getResult());
                    }
                    mallUser = mallUserMapper.selectByPrimaryKey(mallUserToken.getUserId());
                    if (mallUser == null) {
                        CMallException.fail(ServiceResultEnum.USER_NULL_ERROR.getResult());
                    }
                    if (mallUser.getLockedFlag().intValue() == 1) {
                        CMallException.fail(ServiceResultEnum.LOGIN_USER_LOCKED_ERROR.getResult());
                    }
                    redisCache.setCacheObject(Constants.MALL_USER_TOKEN_KEY + token, mallUser,
                            mallUserToken.getExpireTime().getTime() - curTime, TimeUnit.MILLISECONDS);
                }
                return mallUser;
            } else {
                CMallException.fail(ServiceResultEnum.NOT_LOGIN_ERROR.getResult());
            }
        }
        return null;
    }
}
