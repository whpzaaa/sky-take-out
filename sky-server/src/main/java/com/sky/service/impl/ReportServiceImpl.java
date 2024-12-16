package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
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
            Map map = new HashMap<>();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.getSumByTimeAndStatus(map);
            if (turnover == null ){
                turnover = 0.0;
            }
            turnoverList.add(turnover);
        }

        String turnoverListString = StringUtils.join(turnoverList, ",");
        reportVO.setTurnoverList(turnoverListString);
        return reportVO;
    }


    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //先根据begin end 获取需要展示的日期并拼接成字符串
        long between = ChronoUnit.DAYS.between(begin, end);
        List<LocalDate> datelist = new ArrayList<>();
        for (int i = 0; i < between; i++) {
            LocalDate date = begin.plusDays(i);
            datelist.add(date);
        }
        datelist.add(end);
        String datelistString = StringUtils.join(datelist, ",");
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(datelistString);
        //根据end时间获取总用户数
        //再根据begin end获取新用户数
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : datelist) {
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            Map map = new HashMap<>();
            map.put("endTime",endTime);
            Integer total = userMapper.countByTime(map);
            totalUserList.add(total);
            map.put("beginTime",beginTime);
            Integer newUser = userMapper.countByTime(map);
            newUserList.add(newUser);
        }
        String newUserListString = StringUtils.join(newUserList, ",");
        String totalUserListString = StringUtils.join(totalUserList, ",");
        userReportVO.setNewUserList(newUserListString);
        userReportVO.setTotalUserList(totalUserListString);
        return userReportVO;
    }


    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //先根据begin end 获取需要展示的日期并拼接成字符串
        long between = ChronoUnit.DAYS.between(begin, end);
        List<LocalDate> datelist = new ArrayList<>();
        for (int i = 0; i < between; i++) {
            LocalDate date = begin.plusDays(i);
            datelist.add(date);
        }
        datelist.add(end);
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        //根据begin end获取当天的订单数量
        //再加上status获已完成订单
        for (LocalDate date : datelist) {
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            Map map = new HashMap<>();
            map.put("endTime",endTime);
            map.put("beginTime",beginTime);
            Integer orderCount = orderMapper.getOrderCount(map);
            orderCountList.add(orderCount);
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.getOrderCount(map);
            validOrderCountList.add(validOrderCount);
        }
        Integer totalOrderCount = orderCountList.stream().reduce(0, Integer::sum);
        Integer validOrderCount = validOrderCountList.stream().reduce(0, Integer::sum);
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(datelist,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        //根据begin end统计范围内的商品名和数量
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getTop10(beginTime,endTime);
        //再将对象转成两个集合返回
        List<String> nameList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }


    public void export(HttpServletResponse response) throws IOException {
        //先根据模板创建输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //再创建excel对象
        XSSFWorkbook excel = new XSSFWorkbook(inputStream);
        //查询概览数据
        LocalDate beginDay = LocalDate.now().minusDays(30);
        LocalDate endDay = LocalDate.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTime.of(beginDay, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(endDay, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);
        //往excel中写入概览数据
        XSSFSheet sheet = excel.getSheetAt(0);
        XSSFRow row = sheet.getRow(1);
        row.getCell(1).setCellValue("时间：" + beginDay + "至" + endDay);
        row = sheet.getRow(3);
        row.getCell(2).setCellValue(businessData.getTurnover());
        row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
        row.getCell(6).setCellValue(businessData.getNewUsers());
        row = sheet.getRow(4);
        row.getCell(2).setCellValue(businessData.getValidOrderCount());
        row.getCell(4).setCellValue(businessData.getUnitPrice());
        //再分别查30天的数据
        for (int i = 0; i < 30; i++) {
            LocalDate date = beginDay.plusDays(i);
            BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
            //往excel中填入数据
            row = sheet.getRow(7 + i);
            row.getCell(1).setCellValue(date.toString());
            row.getCell(2).setCellValue(data.getTurnover());
            row.getCell(3).setCellValue(data.getValidOrderCount());
            row.getCell(4).setCellValue(data.getOrderCompletionRate());
            row.getCell(5).setCellValue(data.getUnitPrice());
            row.getCell(6).setCellValue(data.getNewUsers());
        }
        //再通过response获取输出流
        ServletOutputStream out = response.getOutputStream();
        //通过此输出流向前端返回文件
        excel.write(out);
        //关闭资源
        excel.close();
        out.close();
        inputStream.close();
    }
}
