package com.tsong.cmall.config.aop;

import com.tsong.cmall.config.annotation.Master;
import com.tsong.cmall.config.annotation.Slave;
import com.tsong.cmall.config.datasource.DBContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @Author Tsong
 * @Date 2023/6/27 16:57
 */
@Aspect
@Component
public class DataSourceAop {
    @Pointcut("@annotation(com.tsong.cmall.config.annotation.Master) " +
            "|| (execution(* com.tsong.cmall.service.*.update*(..))) " +
            "|| (execution(* com.tsong.cmall.service.*.batchUpdate*(..))) " +
            "|| (execution(* com.tsong.cmall.service.*.insert*(..))) " +
            "|| (execution(* com.tsong.cmall.service.*.save*(..))) " +
            "|| (execution(* com.tsong.cmall.service.*.batchSave*(..))) " +
            "|| (execution(* com.tsong.cmall.service.*.delete*(..))) ")
    public void writePointCut(){

    }

    @Pointcut("@annotation(com.tsong.cmall.config.annotation.Slave) " +
            "|| (execution(* com.tsong.cmall.service.*.get*(..))) " +
            "|| (execution(* com.tsong.cmall.service.*.select*(..))) " +
            "|| (execution(* com.tsong.cmall.service.*.search*(..))) ")
    public void readPointCut(){

    }

    @Before("writePointCut()")
    public void write(JoinPoint jp){
        //获取当前的方法信息
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        boolean isSlave = method.isAnnotationPresent(Slave.class);
        if (isSlave){
            DBContextHolder.slave();
        }else{
            DBContextHolder.master();
        }
    }

    @Before("readPointCut()")
    public void slave(JoinPoint jp){
        //获取当前的方法信息
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        boolean isMaster = method.isAnnotationPresent(Master.class);
        if (isMaster){
            DBContextHolder.master();
        }else{
            DBContextHolder.slave();
        }
    }

    @After("writePointCut() || readPointCut()")
    public void afterSwitchDS(){
        DBContextHolder.remove();
    }

}
