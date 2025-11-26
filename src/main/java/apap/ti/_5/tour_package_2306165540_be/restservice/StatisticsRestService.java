package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.restdto.response.MonthlyRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;

import java.util.List;

public interface StatisticsRestService {
    RevenueStatisticsResponseDTO getRevenueStatistics(Integer year, Integer month);

    List<MonthlyRevenueDTO> getYearlyRevenue(Integer year);

    RevenueStatisticsResponseDTO getMonthlyRevenue(Integer year, Integer month);
}
