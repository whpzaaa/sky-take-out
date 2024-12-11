package com.sky.mapper;

import com.sky.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from sky_take_out.user where openid = #{openid}")
    public User getByOpenid(String openid);
    public void save(User user);
    @Select("select * from sky_take_out.user where id = #{userId}")
    User getById(Long userId);

    Integer countByTime(Map map);
}
