package com.tsong.cmall.config.datasource;

import com.tsong.cmall.config.enums.DBTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadLocal定义数据源切换，通过ThreadLocal将数据源绑定到每个线程上下文中
 * @Author Tsong
 * @Date 2023/6/27 16:42
 */
public class DBContextHolder {
    private static final ThreadLocal<DBTypeEnum> contextHolder = new ThreadLocal<>();
//    private static final Logger logger = LoggerFactory.getLogger(DBContextHolder.class);
    public static void set(DBTypeEnum dbType) {
        contextHolder.set(dbType);
    }

    public static DBTypeEnum get() {
        return contextHolder.get();
    }

    public static void remove() {
//        logger.info("datasource {} removed", contextHolder.get().getName());
        contextHolder.remove();
    }

    public static void master() {
        set(DBTypeEnum.MASTER);
//        logger.info("datasource changes to master");
    }

    public static void slave() {
        set(DBTypeEnum.SLAVE);
//        logger.info("datasource changes to slave");
    }
}
