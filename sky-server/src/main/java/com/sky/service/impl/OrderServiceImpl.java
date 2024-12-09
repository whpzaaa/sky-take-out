package com.sky.service.impl;

import com.alibaba.druid.sql.visitor.functions.If;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
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
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        // 替代微信支付成功后的数据库订单状态更新，直接在这里更新了
        // 根据订单号查询当前用户的该订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(ordersPaymentDTO.getOrderNumber(), userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setPayStatus(Orders.PAID);
        orders.setCheckoutTime(LocalDateTime.now());
//        Orders orders = Orders.builder()
//                .id(ordersDB.getId())
//                .status(Orders.TO_BE_CONFIRMED) // 订单状态，待接单
//                .payStatus(Orders.PAID) // 支付状态，已支付
//                .checkoutTime(LocalDateTime.now()) // 更新支付时间
//                .build();

        orderMapper.update(orders);

        return vo;
    }


    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }


    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        //将dto中的page和size传给pagehelper进行分页查询，pagehelper会自动拼接limit的sql语句
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //调用page方法返回查询到的用户数据集合
        Page<Orders> page = orderMapper.page(ordersPageQueryDTO);
        //将orders对象封装为vo对象
        List<OrderVO> list = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }
        //创建pageresult对象将page的总条数和返回集合传入并返回
        PageResult pageResult = new PageResult(page.getTotal(),list);
        return pageResult;
    }


    public OrderVO getOrderByOrderId(Long id) {
        Orders orders = orderMapper.getByOrderId(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }


    public void cancelById(Long id) {
        //先根据id获得订单数据
        Orders order = orderMapper.getByOrderId(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //再根据订单的状态判断 若status大于2则无法取消 小于2则取消
        Integer status = order.getStatus();
        if (status > 2 ) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.CANCELLED);
        orderMapper.update(order);
    }

    public void repetition(Long id) {
        //先根据id获取当前的订单明细数据
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        //再讲订单的菜品套餐数据加入购物车中
        List<ShoppingCart> list = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(id);
            shoppingCart.setCreateTime(LocalDateTime.now());
            list.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(list);
    }


    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //先根据dto开启pagehelper
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        //再根据dto查询订单
        Page<Orders> page = orderMapper.page(ordersPageQueryDTO);
        //若为空则给出提示
        if (page == null || page.getTotal() == 0) {
            throw  new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //若不为空再查询明细表
        List<OrderVO> list = new ArrayList<>();
        for (Orders orders : page) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
            if (orderDetails == null || orderDetails.size() == 0) {
                throw new OrderBusinessException(MessageConstant.UNKNOWN_ERROR);
            }
            //明细若不为空则封装成字符串
            String detailString = orderDetails.stream()
                    .map(x -> x.getName() + "*" + x.getNumber())
                    .collect(Collectors.joining(";"));
            orderVO.setOrderDishes(detailString);
            list.add(orderVO);
        }
        return new PageResult(page.getTotal(),list);
    }

    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.statistics(Orders.TO_BE_CONFIRMED);
        Integer comfirmed = orderMapper.statistics(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.statistics(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(comfirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }


    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = orderMapper.getByOrderId(ordersConfirmDTO.getId());
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.CONFIRMED);
        orderMapper.update(order);
    }


    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders order = orderMapper.getByOrderId(ordersRejectionDTO.getId());
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.CANCELLED);
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(order);
    }


    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders order = orderMapper.getByOrderId(ordersCancelDTO.getId());
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS) || order.getStatus().equals(Orders.COMPLETED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    public void delivery(Long id) {
        Orders order = orderMapper.getByOrderId(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!order.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }


    public void complete(Long id) {
        Orders order = orderMapper.getByOrderId(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(order);
    }

}
