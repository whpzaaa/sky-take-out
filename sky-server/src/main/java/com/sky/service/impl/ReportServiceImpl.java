package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //先根据begin end 获取需要展示的日期并拼接成字符串
        long between = ChronoUnit.DAYS.between(begin, end);
        List<LocalDate> datelist = new ArrayList<>();
        for (int i = 0; i < between; i++) {
            LocalDate date = begin.plusDays(i);
            datelist.add(date);
        }
        datelist.add(end);
        String datelistString = StringUtils.join(datelist, ",");
        TurnoverReportVO reportVO = new TurnoverReportVO();
        reportVO.setDateList(datelistString);
        //再根据日期查询当天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : datelist) {
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            Double turnover = orderMapper.getSumByTimeAndStatus(beginTime,endTime, Orders.COMPLETED);
            if (turnover == null ){
                turnover = 0.0;
            }
            turnoverList.add(turnover);
        }

        String turnoverListString = StringUtils.join(turnoverList, ",");
        reportVO.setTurnoverList(turnoverListString);
        return reportVO;
    }
}
