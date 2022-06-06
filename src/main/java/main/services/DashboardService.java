package main.services;

import main.dto.DetailedStatisticsDto;
import main.dto.ResultStatisticDto;
import main.dto.StatisticsDto;
import main.dto.TotalDto;
import main.model.Site;
import main.repositories.LemmaRepository;
import main.repositories.PageRepository;
import main.repositories.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IsIndexingService isIndexingService;
    @Autowired
    public DashboardService(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IsIndexingService isIndexingService) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.isIndexingService = isIndexingService;
    }
    public ResponseEntity<ResultStatisticDto> calculateStatistics() {
        ResultStatisticDto result = new ResultStatisticDto();
        StatisticsDto statistics = new StatisticsDto();
        TotalDto total = new TotalDto();

        total.sites = siteRepository.count();
        total.isIndexing = isIndexingService.indexingCheck();
        total.lemmas = lemmaRepository.count();
        total.pages = pageRepository.count();

        statistics.detailed = detailedStatistics();
        statistics.total = total;

        result.result = true;
        result.statistics = statistics;

        return ResponseEntity.ok(result);
    }
    private List<DetailedStatisticsDto> detailedStatistics() {
        DetailedStatisticsDto siteStatistics;
        List<DetailedStatisticsDto> detailedStatistics = new ArrayList<>();
        Iterable<Site> siteIterable = siteRepository.findAll();
        ArrayList<Site> sites = new ArrayList<>();

        for (Site site : siteIterable) {
            sites.add(site);
        }
        for (Site site : sites) {
            siteStatistics = new DetailedStatisticsDto();

            siteStatistics.url = site.getUrl();
            siteStatistics.name = site.getName();
            siteStatistics.status = site.getStatus();
            siteStatistics.statusTime = site.getStatusTime();
            siteStatistics.error = site.getLastError();
            siteStatistics.lemmas = lemmaRepository.countBysiteId(site.getId());
            siteStatistics.pages = pageRepository.countBysiteId(site.getId());
            detailedStatistics.add(siteStatistics);
        }
        return detailedStatistics;
    }
}
