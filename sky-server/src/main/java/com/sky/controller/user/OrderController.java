package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Employee;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.events.Event;

import java.util.HashMap;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户端订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private WebSocketServer webSocketServer;
    @PostMapping("submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }
    /**
    * 历史订单查询
    *
    * @param ordersPageQueryDTO
    * @return
    */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    //由于是param形式传参，直接用对象接受 dto中有name page pagesize
    public Result<PageResult> page (OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单查询");
        //pageresult是一个要给前端返回的对象 对象中有总记录数和需要返回的用户数据集合
        PageResult pageResult = orderService.page(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 根据ID查询订单详情
     *
     * @return
     */
    @GetMapping("orderDetail/{id}")
    @ApiOperation("根据ID查询订单详情")
    public Result<OrderVO> getById(@PathVariable Long id) {
        log.info("根据ID查询订单详情");
        OrderVO orderVO = orderService.getOrderByOrderId(id);
        return Result.success(orderVO);
    }
    /**
     * 根据ID取消订单
     *
     * @return
     */
    @PutMapping("cancel/{id}")
    @ApiOperation("根据id取消订单")
    public Result cancelById(@PathVariable Long id){
        log.info("根据id取消订单,{}",id);
        orderService.cancelById(id);
        return Result.success();
    }
    /**
     * 再来一单
     *
     * @return
     */
    @PostMapping("repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        log.info("再来一单,{}",id);
        orderService.repetition(id);
        return Result.success();
    }
    /**
     * 模拟微信支付成功后订单来单提醒
     * @param orderId
     */
    @ApiOperation("订单来单提醒接口")
    @GetMapping("/orderAdvice")
    public void orderAdvice(Long orderId){
        //根据订单ID查询数据
        Orders orders = orderService.getOrderByOrderId(orderId);
        //封装数据
        HashMap<String, Object> map = new HashMap<>();
        map.put("type",1);
        map.put("orderId",orderId);
        map.put("content","订单号"+orders.getNumber());
        //将封装好的数据转为JSON使用wenSocket技术发送
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }
    @GetMapping("reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Long id){
        orderService.reminder(id);
        return Result.success();
    }
}
