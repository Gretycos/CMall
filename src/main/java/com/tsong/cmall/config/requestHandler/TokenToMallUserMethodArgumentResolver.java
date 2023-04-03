package com.tsong.cmall.config.requestHandler;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.dao.MallUserMapper;
import com.tsong.cmall.dao.UserTokenMapper;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.entity.UserToken;
import com.tsong.cmall.exception.CMallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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

    public TokenToMallUserMethodArgumentResolver() {
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TokenToMallUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        if (parameter.getParameterAnnotation(TokenToMallUser.class) instanceof TokenToMallUser) {
            MallUser mallUser = null;
            String token = webRequest.getHeader("token");
            if (null != token && !"".equals(token) && token.length() == Constants.TOKEN_LENGTH) {
                UserToken mallUserToken = userTokenMapper.selectByToken(token);
                if (mallUserToken == null || mallUserToken.getExpireTime().getTime() <= System.currentTimeMillis()) {
                    CMallException.fail(ServiceResultEnum.TOKEN_EXPIRE_ERROR.getResult());
                }
                mallUser = mallUserMapper.selectByPrimaryKey(mallUserToken.getUserId());
                if (mallUser == null) {
                    CMallException.fail(ServiceResultEnum.USER_NULL_ERROR.getResult());
                }
                if (mallUser.getLockedFlag().intValue() == 1) {
                    CMallException.fail(ServiceResultEnum.LOGIN_USER_LOCKED_ERROR.getResult());
                }
                return mallUser;
            } else {
                CMallException.fail(ServiceResultEnum.NOT_LOGIN_ERROR.getResult());
            }
        }
        return null;
    }
}
