package main.controllers;

import main.dto.ResultStatisticDto;
import main.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<ResultStatisticDto> getStatistics() {
        return dashboardService.calculateStatistics();
    }
}
