package com.tsong.cmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 全局配置
 * @Author Tsong
 * @Date 2023/3/24 19:15
 */
@Component
public class ProjectConfig {
    /**
     * 项目名称
     */
    private static String name;
    /**
     * 订单超期未支付时间，单位秒
     */
    private static Integer orderUnpaidOverTime;

    /**
     * 秒杀订单超期未支付时间，单位秒
     */
    private static Integer seckillOrderUnpaidOverTime;

    public static String getName() {
        return name;
    }

    @Value("${project.name}")
    public void setName(String name) {
        ProjectConfig.name = name;
    }

    public static Integer getOrderUnpaidOverTime() {
        return orderUnpaidOverTime;
    }

    @Value("${project.orderUnpaidOverTime}")
    public void setOrderUnpaidOverTime(Integer orderUnpaidOverTime) {
        ProjectConfig.orderUnpaidOverTime = orderUnpaidOverTime;
    }

    public static Integer getSeckillOrderUnpaidOverTime(){
        return ProjectConfig.seckillOrderUnpaidOverTime;
    }

    @Value("${project.seckillOrderUnpaidOverTime}")
    public void setSeckillOrderUnpaidOverTime(Integer seckillOrderUnpaidOverTime) {
        ProjectConfig.seckillOrderUnpaidOverTime = seckillOrderUnpaidOverTime;
    }
}
