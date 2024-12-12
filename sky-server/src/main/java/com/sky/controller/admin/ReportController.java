package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Api(tags = "数据统计相关接口")
@RequestMapping("admin/report")
@Slf4j
public class ReportController {
    @Autowired
    private ReportService reportService;
    @GetMapping("turnoverStatistics")
    @ApiOperation("统计营业额")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end ){
        log.info("统计营业额：{}，{}",begin,end);
        TurnoverReportVO turnoverReport = reportService.turnoverStatistics(begin,end);
        return Result.success(turnoverReport);
    }
    @GetMapping("userStatistics")
    @ApiOperation("统计用户")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end ){
        log.info("统计用户：{}，{}",begin,end);
        UserReportVO userReportVO = reportService.userStatistics(begin,end);
        return Result.success(userReportVO);
    }
    @GetMapping("ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end ){
        log.info("订单统计：{}，{}",begin,end);
        OrderReportVO orderReportVO = reportService.ordersStatistics(begin,end);
        return Result.success(orderReportVO);
    }
    @GetMapping("top10")
    @ApiOperation("查询销量排名top10接口")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end ){
        log.info("查询销量排名top10接口：{}，{}",begin,end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.top10(begin,end);
        return Result.success(salesTop10ReportVO);
    }
}
