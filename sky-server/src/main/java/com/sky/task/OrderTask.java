package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName OrderTask
 * @Desccription
 * @Author SongZiPeng
 * @Date 2023-11-01 20:48
 **/

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     * 每分钟一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        log.info("定时处理超时订单, {}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, localDateTime);
        if(ordersList != null && ordersList.size() > 0){
            for(Orders item:ordersList){
                item.setStatus(Orders.CANCELLED);
                item.setCancelReason("订单超时，自动取消");
                item.setCancelTime(LocalDateTime.now());
                orderMapper.update(item);
            }
        }
    }


    /**
     * 处理一直“派送中”的订单
     * 每天凌晨一点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("处理一直处于派送中的订单,{}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, localDateTime);
        if(ordersList != null && ordersList.size() > 0){
            for(Orders item:ordersList){
                item.setStatus(Orders.COMPLETED);
                orderMapper.update(item);
            }
        }
    }

}
