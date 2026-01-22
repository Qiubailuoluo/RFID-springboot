package com.zebrarfid.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zebrarfid.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 标记为MyBatis的Mapper接口
public interface UserMapper extends BaseMapper<User> {
    // MyBatis-Plus自带CRUD方法，无需手动写SQL
    // 这里我们需要根据用户名查用户，BaseMapper的selectOne方法即可满足
}
