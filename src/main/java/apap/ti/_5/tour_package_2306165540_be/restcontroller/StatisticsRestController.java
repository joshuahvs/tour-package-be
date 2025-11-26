package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.MonthlyRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.StatisticsRestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsRestController {

    private final StatisticsRestService statisticsRestService;

    public StatisticsRestController(StatisticsRestService statisticsRestService) {
        this.statisticsRestService = statisticsRestService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<BaseResponseDTO<RevenueStatisticsResponseDTO>> getRevenueStatistics(
            @RequestParam(required = true) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            RevenueStatisticsResponseDTO statistics = statisticsRestService.getRevenueStatistics(year, month);

            BaseResponseDTO<RevenueStatisticsResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved revenue statistics");
            response.setData(statistics);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<RevenueStatisticsResponseDTO> response = new BaseResponseDTO<>();

            if (e.getMessage().contains("Only Superadmin")) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setMessage(e.getMessage());
                response.setData(null);
                response.setTimestamp(new Date());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<RevenueStatisticsResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve statistics: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/revenue/yearly/{year}")
    public ResponseEntity<BaseResponseDTO<List<MonthlyRevenueDTO>>> getYearlyRevenue(@PathVariable Integer year) {
        try {
            List<MonthlyRevenueDTO> statistics = statisticsRestService.getYearlyRevenue(year);

            BaseResponseDTO<List<MonthlyRevenueDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved yearly revenue statistics");
            response.setData(statistics);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<List<MonthlyRevenueDTO>> response = new BaseResponseDTO<>();

            if (e.getMessage().contains("Only Superadmin")) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setMessage(e.getMessage());
                response.setData(null);
                response.setTimestamp(new Date());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<List<MonthlyRevenueDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve statistics: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/revenue/monthly/{year}/{month}")
    public ResponseEntity<BaseResponseDTO<RevenueStatisticsResponseDTO>> getMonthlyRevenue(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        try {
            RevenueStatisticsResponseDTO statistics = statisticsRestService.getMonthlyRevenue(year, month);

            BaseResponseDTO<RevenueStatisticsResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved monthly revenue statistics");
            response.setData(statistics);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<RevenueStatisticsResponseDTO> response = new BaseResponseDTO<>();

            if (e.getMessage().contains("Only Superadmin")) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setMessage(e.getMessage());
                response.setData(null);
                response.setTimestamp(new Date());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<RevenueStatisticsResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve statistics: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
