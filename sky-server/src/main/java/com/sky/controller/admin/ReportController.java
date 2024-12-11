package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.TurnoverReportVO;
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
    private OrderService orderService;
    @GetMapping("turnoverStatistics")
    @ApiOperation("统计营业额")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end ){
        log.info("统计营业额：{}，{}",begin,end);
        TurnoverReportVO turnoverReport = orderService.turnoverStatistics(begin,end);
        return Result.success(turnoverReport);
    }
}
