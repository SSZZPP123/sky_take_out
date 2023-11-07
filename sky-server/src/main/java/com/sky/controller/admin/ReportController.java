package com.sky.controller.admin;

import com.sky.result.Result;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/**
 * @ClassName ReportController
 * @Desccription
 * @Author SongZiPeng
 * @Date 2023-11-06 19:58
 **/

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "数据统计相关接口")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额数据统计：日期:{}到{}", begin, end);
        TurnoverReportVO turnoverStatistic = reportService.getTurnoverStatistic(begin, end);
        return Result.success(turnoverStatistic);
    }


    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userstatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("用户统计：日期:{}到{}", begin, end);
        UserReportVO userStatistic = reportService.getUserStatistic(begin, end);
        return Result.success(userStatistic);
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("用户统计：日期:{}到{}", begin, end);
        OrderReportVO orderStatisticsVO = reportService.getOrdersStatistic(begin, end);
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/top10")
    @ApiOperation("销量前10统计")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("用户统计：日期:{}到{}", begin, end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.top10(begin, end);
        return Result.success(salesTop10ReportVO);
    }

    @GetMapping("/export")
    @ApiOperation("导出数据报表")
    public void export(HttpServletResponse response) throws IOException {
        reportService.export(response);
    }

}
