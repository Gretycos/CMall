package com.tsong.cmall.dao;

import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.util.PageQueryUtil;

import java.util.List;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface MallUserMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(MallUser row);

    int insertSelective(MallUser row);

    MallUser selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(MallUser row);

    int updateByPrimaryKey(MallUser row);

    MallUser selectByLoginName(String loginName);

    MallUser selectByLoginNameAndPasswd(String loginName, String password);

    List<MallUser> findMallUserList(PageQueryUtil pageUtil);

    int getTotalMallUsers(PageQueryUtil pageUtil);

    int lockUserBatch(Integer[] ids, int lockStatus);
}