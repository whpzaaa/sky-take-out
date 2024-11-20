package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    public void save(List<DishFlavor> flavors);

    void deleteByDishIds(List<Long> ids);
    @Select("select * from sky_take_out.dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
    @Delete("delete from sky_take_out.dish_flavor where dish_flavor.dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
}
