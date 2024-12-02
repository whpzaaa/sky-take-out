package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //先排除异常情况 地址簿和购物车的信息是否存在
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw  new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向order表中插入一个订单数据
        Orders orders =  new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        //根据时间戳设置订单号
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        //设置订单状态为待支付
        orders.setStatus(Orders.PENDING_PAYMENT);
        //设置支付状态为未支付
        orders.setPayStatus(Orders.UN_PAID);
        //设置用户id
        orders.setUserId(BaseContext.getCurrentId());
        //设置下单时间
        orders.setOrderTime(LocalDateTime.now());
        //根据地址簿设置用户电话
        orders.setPhone(addressBook.getPhone());
        //设置地址
        orders.setAddress(addressBook.getDetail());
        //设置收货人
        orders.setConsignee(addressBook.getConsignee());
        orderMapper.save(orders);
        //再向detail表中插入几条菜品或套餐的明细数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.saveBatch(orderDetails);
        //清空购物车
        shoppingCartMapper.cleanShoppingCart(BaseContext.getCurrentId());
        //返回vo结果
        return new OrderSubmitVO(orders.getId(),orders.getNumber(),orders.getAmount(),orders.getOrderTime());
    }
}
