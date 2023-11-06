package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @ClassName ReportServiceImpl
 * @Desccription
 * @Author SongZiPeng
 * @Date 2023-11-06 20:04
 **/

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistic(LocalDate begin, LocalDate end) {
        //存放开始到结束的每一天日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //获取每一天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date:dateList){
            //select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            //由于前端传至是只有年月日的，时分秒需要后端获取，这里的MIN和MAX可以获取一天的最大&最小值，也就是00:00:00和23:59:59
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            //如果当天没有订单，这里会返回为空，应该转为0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        TurnoverReportVO result = TurnoverReportVO.builder()
                .dateList(org.apache.commons.lang3.StringUtils.join(dateList, ","))
                .turnoverList(org.apache.commons.lang3.StringUtils.join(turnoverList, ","))
                .build();

        return result;
    }


    @Override
    public UserReportVO getUserStatistic(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //select count(id) from user where creata_time < ? and create_time > ?
        List<Integer> newUserList = new ArrayList<>();
        //select count(id) from user where create_time > ?
        List<Integer> totalUserList = new ArrayList<>();
        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            //总用户
            Integer total = userMapper.countByMap(map);
            totalUserList.add(total);

            //新增用户
            map.put("begin", beginTime);
            Integer add = userMapper.countByMap(map);
            newUserList.add(add);
        }
        return UserReportVO.builder()
                .dateList(org.apache.commons.lang3.StringUtils.join(dateList, ","))
                .newUserList(org.apache.commons.lang3.StringUtils.join(newUserList, ","))
                .totalUserList(org.apache.commons.lang3.StringUtils.join(totalUserList, ","))
                .build();
    }


    @Override
    public OrderReportVO getOrdersStatistic(LocalDate begin, LocalDate end) {
        //需要准备6项数据
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();

        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //每天订单总数
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            orderCountList.add(orderCount);
            //每天订单有效数据
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            validOrderList.add(validOrderCount);
        }

        //订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer totalValitCount = validOrderList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = totalOrderCount == 0 ? 0.0 :
                totalValitCount.doubleValue() / totalOrderCount.doubleValue();

        OrderReportVO result = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValitCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
        return result;
    }


    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

}
