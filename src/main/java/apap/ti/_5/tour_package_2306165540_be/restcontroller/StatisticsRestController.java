package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.StatisticsRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsRestController {

    private final StatisticsRestService statisticsRestService;

    public StatisticsRestController(StatisticsRestService statisticsRestService){
        this.statisticsRestService = statisticsRestService;
    }

    @GetMapping
    public ResponseEntity<BaseResponseDTO<RevenueStatisticsResponseDTO>> getRevenueStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            RevenueStatisticsResponseDTO statistics = statisticsRestService.getRevenueStatistics(year, month);

            BaseResponseDTO<RevenueStatisticsResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved revenue statistics");
            response.setData(statistics);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
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
