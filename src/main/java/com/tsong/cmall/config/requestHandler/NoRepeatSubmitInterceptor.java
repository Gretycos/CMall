package com.tsong.cmall.config.requestHandler;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.config.annotation.NoRepeatSubmit;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.redis.RedisCache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * @Author Tsong
 * @Date 2023/4/11 00:09
 */
@Component
public class NoRepeatSubmitInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisCache redisCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) {
        if (o instanceof HandlerMethod handlerMethod){
            NoRepeatSubmit noRepeatSubmit = handlerMethod.getMethodAnnotation(NoRepeatSubmit.class);
            if (noRepeatSubmit == null){
                return true;
            }
            String token = request.getHeader("token");
            String url = request.getRequestURL().toString();
            if (redisCache.containsCacheSet(Constants.REQUEST_KEY + token, url)){
                CMallException.fail("请勿频繁提交");
            }
            redisCache.setCacheSet(Constants.REQUEST_KEY + token, url);
            redisCache.expire(Constants.REQUEST_KEY + token, noRepeatSubmit.lockTime(), TimeUnit.SECONDS);
            return true;
        }
        return true;
    }
}
