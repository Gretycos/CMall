package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.vo.HomePageConfigGoodsVO;
import com.tsong.cmall.dao.GoodsInfoMapper;
import com.tsong.cmall.dao.HomePageConfigMapper;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.entity.HomePageConfig;
import com.tsong.cmall.service.HomePageConfigService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Tsong
 * @Date 2023/3/24 13:44
 */
@Service
public class HomePageConfigServiceImpl implements HomePageConfigService {
    @Autowired
    private HomePageConfigMapper homePageConfigMapper;

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Override
    public PageResult getConfigsPage(PageQueryUtil pageUtil) {
        List<HomePageConfig> indexConfigs = homePageConfigMapper.findHomePageConfigList(pageUtil);
        int total = homePageConfigMapper.getTotalHomePageConfigs(pageUtil);
        return new PageResult(indexConfigs, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String saveHomePageConfig(HomePageConfig homePageConfig) {
        if (goodsInfoMapper.selectByPrimaryKey(homePageConfig.getGoodsId()) == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        if (homePageConfigMapper.selectByTypeAndGoodsId(
                homePageConfig.getConfigType(), homePageConfig.getGoodsId()) != null) {
            return ServiceResultEnum.SAME_HOME_PAGE_CONFIG_EXIST.getResult();
        }
        if (homePageConfigMapper.insertSelective(homePageConfig) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateHomePageConfig(HomePageConfig homePageConfig) {
        if (goodsInfoMapper.selectByPrimaryKey(homePageConfig.getGoodsId()) == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        HomePageConfig temp = homePageConfigMapper.selectByPrimaryKey(homePageConfig.getConfigId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        HomePageConfig temp2 = homePageConfigMapper.selectByTypeAndGoodsId(
                homePageConfig.getConfigType(), homePageConfig.getGoodsId());
        if (temp2 != null && !temp2.getConfigId().equals(homePageConfig.getConfigId())) {
            //goodsId相同且不同id 不能继续修改
            return ServiceResultEnum.SAME_HOME_PAGE_CONFIG_EXIST.getResult();
        }
        homePageConfig.setUpdateTime(new Date());
        if (homePageConfigMapper.updateByPrimaryKeySelective(homePageConfig) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public HomePageConfig getHomePageConfigById(Long id) {
        return homePageConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<HomePageConfigGoodsVO> getConfigGoodsForHomePage(int configType, int number) {
        List<HomePageConfigGoodsVO> homePageConfigGoodsVOList = new ArrayList<>(number);
        List<HomePageConfig> homePageConfigList = homePageConfigMapper.findHomePageConfigsByTypeAndNum(configType, number);
        if (!CollectionUtils.isEmpty(homePageConfigList)) {
            //取出所有的goodsId
            List<Long> goodsIds = homePageConfigList.stream()
                    .map(HomePageConfig::getGoodsId).collect(Collectors.toList());
            List<GoodsInfo> goodsInfoList = goodsInfoMapper.selectByPrimaryKeys(goodsIds);
            homePageConfigGoodsVOList = BeanUtil.copyList(goodsInfoList, HomePageConfigGoodsVO.class);
            for (HomePageConfigGoodsVO homePageConfigGoodsVO : homePageConfigGoodsVOList) {
                String goodsName = homePageConfigGoodsVO.getGoodsName();
                String goodsIntro = homePageConfigGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 30) {
                    goodsName = goodsName.substring(0, 30) + "...";
                    homePageConfigGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 22) {
                    goodsIntro = goodsIntro.substring(0, 22) + "...";
                    homePageConfigGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        return homePageConfigGoodsVOList;
    }

    @Override
    public Boolean deleteBatch(Long[] ids) {
        if (ids.length < 1) {
            return false;
        }
        //删除数据
        return homePageConfigMapper.deleteBatch(ids) > 0;
    }
}
