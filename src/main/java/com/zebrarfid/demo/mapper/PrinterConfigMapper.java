package com.zebrarfid.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zebrarfid.demo.entity.PrinterConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PrinterConfigMapper extends BaseMapper<PrinterConfig> {
    // 根据用户ID查询保存的配置（MyBatis-Plus自动生成SQL）
    @Select("SELECT * FROM printer_config WHERE user_id = #{userId}")
    List<PrinterConfig> selectByUserId(Long userId);
}
