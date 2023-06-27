package com.tsong.cmall.config.datasource;

import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 配置Mybatis指定数据源:SqlSessionFactory和事务管理器
 * @Author Tsong
 * @Date 2023/6/27 16:51
 */
@Configuration
@EnableTransactionManagement
public class MyBatisConfig {
    @Resource(name = "routingDataSource")
    private DataSource routingDataSource;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(routingDataSource);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    /**
     * @Description 事务管理器，不写则事务不生效，事务需要知道当前使用的是哪个数据源才能进行事务处理
     * @Param []
     * @Return org.springframework.transaction.PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager platformTransactionManager() {
        return new DataSourceTransactionManager(routingDataSource);
    }
}
