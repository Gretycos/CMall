package com.tsong.cmall.service;

import com.tsong.cmall.controller.vo.SeckillGoodsVO;
import com.tsong.cmall.controller.vo.SeckillSuccessVO;
import com.tsong.cmall.controller.vo.SeckillVO;
import com.tsong.cmall.controller.vo.UrlExposerVO;
import com.tsong.cmall.entity.Seckill;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;

import java.util.List;

public interface SeckillService {
    PageResult getSeckillPage(PageQueryUtil pageUtil);

    SeckillVO getSeckillVOById(Long id);

    boolean saveSeckill(Seckill seckill);

    boolean updateSeckill(Seckill seckill);

    Seckill getSeckillById(Long id);

    boolean deleteSeckillById(Long id);

    List<Seckill> getHomePageSeckill();

    UrlExposerVO exposeUrl(Long seckillId);

    SeckillSuccessVO executeSeckill(Long seckillId, Long userId);

    SeckillGoodsVO getSeckillGoodsDetail(Seckill seckill);

    List<SeckillGoodsVO> getSeckillGoodsList();
}
