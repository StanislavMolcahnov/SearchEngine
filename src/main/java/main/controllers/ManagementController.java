package main.controllers;

import main.services.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManagementController {
    private final ManagementService managementService;

    @Autowired
    public ManagementController(ManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping("/startIndexing")
    public StringBuilder startIndexing() {
        return managementService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public StringBuilder stopIndexing() {
        return managementService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public synchronized StringBuilder indexPage(String url) {
        return managementService.updateUrl(url);
    }
}
