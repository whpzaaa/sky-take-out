package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void save(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    Page<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);
    @Select("select * from sky_take_out.orders where id = #{id}")
    Orders getByOrderId(Long id);
    @Select("select count(*) from sky_take_out.orders where status = #{status}")
    Integer statistics(Integer status);
    @Select("select * from sky_take_out.orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);
    
    Double getSumByTimeAndStatus(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    Double getSumByTimeAndStatus(Map map);

    Integer getOrderCount(Map map);

    List<GoodsSalesDTO> getTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
