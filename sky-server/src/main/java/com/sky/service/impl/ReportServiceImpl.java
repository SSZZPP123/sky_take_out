package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private WorkspaceService workspaceService;

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


    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        //要查order_detail（销售份数）和订单表（订单状态为完成）
        /**
         * select od.name, od.sum(number) number from order_detail od, orders o where od.order_id = o.id and o.status = 5
         * where o.order_time > ? and o.order_time < ?
         * order by number desc
         * limit 0,10
         */
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> saleTop10 = orderMapper.getSaleTop10(beginTime, endTime);
        List<String> names = new ArrayList<>();
        List<Integer> number = new ArrayList<>();
        if(saleTop10 != null && saleTop10.size() > 0 && saleTop10.get(0) != null){
            names = saleTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
            number = saleTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        }
        SalesTop10ReportVO result = SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(number, ","))
                .build();
        return result;
    }



    @Override
    public void export(HttpServletResponse response) throws IOException {
        //1、获取营业数据（近30天，注意今天不算，因为今天可能还没有完结）
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);

        //2、通过POI写入excel文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook excel = new XSSFWorkbook(in);
        XSSFSheet sheet = excel.getSheetAt(0);
        //时间数据
        sheet.getRow(1).getCell(1).setCellValue("时间：" + beginTime + " 至 " + endTime);
        //概览数据
        sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
        sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
        sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
        sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
        sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());
        //明细数据
        for(int i = 0; i < 30; i++){
            LocalDate date = begin.plusDays(i);
            BusinessDataVO oneDayBusiness = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN),
                    LocalDateTime.of(begin, LocalTime.MAX));
            XSSFRow row = sheet.getRow(7 + i);
            row.getCell(1).setCellValue(date.toString());
            row.getCell(2).setCellValue(oneDayBusiness.getTurnover());
            row.getCell(3).setCellValue(oneDayBusiness.getValidOrderCount());
            row.getCell(4).setCellValue(oneDayBusiness.getOrderCompletionRate());
            row.getCell(5).setCellValue(oneDayBusiness.getUnitPrice());
            row.getCell(6).setCellValue(oneDayBusiness.getNewUsers());
        }

        //3、通过输出流将excel下载到客户端浏览器
        ServletOutputStream outputStream = response.getOutputStream();
        excel.write(outputStream);
        outputStream.close();
        excel.close();

    }


    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

}
