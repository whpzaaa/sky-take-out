package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void saveBatch(List<OrderDetail> orderDetails);
    @Select("select * from sky_take_out.order_detail where order_id = #{id}")
    List<OrderDetail> getByOrderId(Long id);
}
