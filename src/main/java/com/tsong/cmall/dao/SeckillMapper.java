package com.tsong.cmall.dao;

import com.tsong.cmall.entity.Seckill;
import com.tsong.cmall.util.PageQueryUtil;

import java.util.List;
import java.util.Map;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface SeckillMapper {
    int deleteByPrimaryKey(Long seckillId);

    int insert(Seckill row);

    int insertSelective(Seckill row);

    Seckill selectByPrimaryKey(Long seckillId);

    int updateByPrimaryKeySelective(Seckill row);

    int updateByPrimaryKey(Seckill row);

    List<Seckill> findSeckillList(PageQueryUtil pageUtil);

    int getTotalSeckills(PageQueryUtil pageUtil);

    List<Seckill> findHomePageSeckillList();

    int getHomePageTotalSeckills(PageQueryUtil pageUtil);

    void killByProcedure(Map<String, Object> map);

    boolean addStock(Long seckillId);
}