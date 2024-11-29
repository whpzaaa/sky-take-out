package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> list(ShoppingCart shoppingCart);
    @Update("update sky_take_out.shopping_cart set number = #{number} where id = #{id} ")
    void updateById(ShoppingCart shoppingCart);
    @Insert("insert into sky_take_out.shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time,number) " +
            "VALUES (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime},#{number})")
    void insert(ShoppingCart shoppingCart);
    @Delete("delete from sky_take_out.shopping_cart where user_id = #{userId}")
    void cleanShoppingCart(Long userId);
    @Delete("delete from sky_take_out.shopping_cart where id = #{id}")
    void deleteById(Long id);
}
