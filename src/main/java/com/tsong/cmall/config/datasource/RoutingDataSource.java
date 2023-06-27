package com.tsong.cmall.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;

/**
 * 路由选择类
 * @Author Tsong
 * @Date 2023/6/27 16:39
 */
public class RoutingDataSource extends AbstractRoutingDataSource {
    /**
     * determineCurrentLookupKey()方法决定使用哪个数据源
     * 根据Key获取数据源的信息，上层抽象函数的钩子
     */
    @Nullable
    @Override
    protected Object determineCurrentLookupKey(){
        return DBContextHolder.get();
    }
}
