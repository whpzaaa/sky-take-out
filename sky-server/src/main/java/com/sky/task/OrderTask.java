package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron = "0 * * * * *")
    public void processTimeOutOrder(){
        log.info("定时处理超时订单: {}", LocalDateTime.now());
        LocalDateTime orderTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT,orderTime);
        if (list != null && list.size() > 0 ) {
            for (Orders orders : list) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }
    @Scheduled(cron = "0 0 1 * * *")
    public void processDeliveryOrder(){
        log.info("处理派送中订单：{}", LocalDateTime.now());
        LocalDateTime orderTime = LocalDateTime.now().plusMinutes(-60);
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,orderTime);
        if (list != null && list.size() > 0 ) {
            for (Orders orders : list) {
                orders.setStatus(Orders.COMPLETED);
                orders.setDeliveryTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }
}
