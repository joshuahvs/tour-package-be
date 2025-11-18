package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;

public interface StatisticsRestService {
    RevenueStatisticsResponseDTO getRevenueStatistics(Integer year, Integer month);
}
