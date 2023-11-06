package com.sky.service;

import com.sky.vo.*;

import java.time.LocalDate;

public interface ReportService {

    TurnoverReportVO getTurnoverStatistic(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistic(LocalDate begin, LocalDate end);

    OrderReportVO getOrdersStatistic(LocalDate begin, LocalDate end);

    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);
}
