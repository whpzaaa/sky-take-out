package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "管理端订单相关接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
    /**
     * 根据ID查询订单详情
     *
     * @return
     */
    @GetMapping("details/{id}")
    @ApiOperation("根据ID查询订单详情")
    public Result<OrderVO> getById(@PathVariable Long id) {
        log.info("根据ID查询订单详情");
        OrderVO orderVO = orderService.getOrderByOrderId(id);
        return Result.success(orderVO);
    }
    @PutMapping("confirm")
    @ApiOperation("接单")
    public Result confirm (@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }
    @PutMapping("rejection")
    @ApiOperation("拒单")
    public Result rejection (@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("cancel")
    @ApiOperation("取消订单")
    public Result rejection (@RequestBody OrdersCancelDTO ordersRejectionDTO){
        orderService.cancel(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery (@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }
    @PutMapping("complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }
}
