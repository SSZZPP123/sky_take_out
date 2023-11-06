package com.sky.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
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
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MIN);
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

}
